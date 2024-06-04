import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "1.9.22"
    id("maven-publish")
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("org.screamingsandals.nms-mapper") version "1.4.6"
    id("xyz.jpenilla.run-paper") version "2.2.2"
}

val versionString: String = findProperty("version")!! as String

val slug = "goodbyegonepoof"
group = "net.minelucky.vanish"
version = versionString
description = "Modern vanish system"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.maven.apache.org/maven2/")

    // Velocity-API / PaperLib / Folia
    maven("https://repo.papermc.io/repository/maven-public/")

    maven("https://repo.spongepowered.org/maven")

    // AdventureAPI/MiniMessage
    maven("https://oss.sonatype.org/content/repositories/snapshots/")

    // Spigot
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")

    // ProtocolLib
    maven("https://repo.dmulloy2.net/repository/public/")

    // PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    // Mojang
    maven("https://repo.aikar.co/nexus/content/repositories/aikar-release/")

    // Cloud SNAPSHOT (Dev repository)
    maven("https://repo.masmc05.dev/repository/maven-snapshots/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    compileOnly("org.spigotmc:pandaspigot-server:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("org.spongepowered:configurate-yaml:4.2.0-SNAPSHOT")
    compileOnly("com.mojang:authlib:1.11")
    compileOnly("io.netty:netty-all:4.1.104.Final")

    implementation("io.papermc:paperlib:1.0.8")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("commons-io:commons-io:2.16.1")

    implementation("com.github.cryptomorin:XSeries:9.8.1") { isTransitive = false }

    implementation("net.kyori:adventure-api:4.15.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    implementation("net.kyori:adventure-text-minimessage:4.15.0")

    implementation("cloud.commandframework:cloud-paper:tooltips-SNAPSHOT")
    implementation("cloud.commandframework:cloud-minecraft-extras:tooltips-SNAPSHOT")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "net.minelucky.vanish"
            artifactId = "GoodbyeGonePoof"
            version = "3.27.2-SNAPSHOT"

            from(components["java"])
        }
    }

    publishing {
        repositories {
            maven {
                name = "minelucky-snapshots"
                url = uri("https://nexus.minelucky.net/repository/maven-snapshots/")
                credentials {
                    val username = System.getenv("MINELUCKY_REPO_USERNAME")
                    val password = System.getenv("MINELUCKY_REPO_PASSWORD")

                    if (username == null || password == null) {
                        println("MINELUCKY_REPO_USERNAME or MINELUCKY_REPO_PASSWORD environment variables are not set.")
                    }

                    this.username = username
                    this.password = password
                }
            }
        }
    }

    tasks.withType<PublishToMavenLocal> {
        dependsOn(tasks.shadowJar)
    }
}

tasks {
    runServer {
        minecraftVersion("1.8.8")
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        dependsOn(clean)
    }

    processResources {
        filesMatching(listOf("**plugin.yml", "**plugin.json")) {
            expand(
                "version" to project.version as String,
                "slug" to slug,
                "name" to rootProject.name,
                "description" to project.description
            )
        }
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}_${project.version}.jar")
        archiveClassifier.set("shadowJar")
        exclude("META-INF/**")
        from("LICENSE")
        minimize()

        relocate("io.papermc.lib", "net.minelucky.vanish.dependencies.io.papermc.lib")
        relocate("io.leangen", "net.minelucky.vanish.dependencies.io.leangen")
        relocate("com.google.gson", "net.minelucky.vanish.dependencies.com.google.gson")
        relocate("com.cryptomorin", "net.minelucky.vanish.dependencies.com.github.cryptomorin")
        relocate("cloud.commandframework", "net.minelucky.vanish.dependencies.cloud.commandframework")
        relocate("kotlin", "net.minelucky.vanish.dependencies.kotlin")
        relocate("com.jeff_media", "net.minelucky.vanish.dependencies.com.jeff_media")
        relocate("org.jetbrains", "net.minelucky.vanish.dependencies.org.jetbrains")
        relocate("org.intellij", "net.minelucky.vanish.dependencies.org.intellij")

        doLast {
            println("Shadow JAR created at: ${archiveFile.get().asFile}")
        }
    }

    build {
        dependsOn(clean)
        dependsOn(shadowJar)
    }

    jar {
        enabled = true
    }

    withType<KotlinCompile> {
        dependsOn(generateNmsComponents)
        kotlinOptions.jvmTarget = "17"
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

artifacts.archives(tasks.shadowJar)

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf(
        "version" to project.version
    )
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.main.get().java.srcDir(generateTemplates.map { outputs -> outputs })
rootProject.idea.project.settings.taskTriggers.afterSync(generateTemplates)

/* First add a new source set. Don't use your main source set for generated stuff. */
sourceSets.main.get().java.srcDirs("src/generated/java", "src/main/java")

/* All other things will be set inside the nmsGen method, */
nmsGen {
    basePackage = "net.minelucky.nms.accessors" // All generated classes will be in this package.
    sourceSet = "src/generated/java" // All generated classes will be part of this source set.
    minMinecraftVersion = "1.8"

    /* This means that the folder will be cleared before generation.
     *
     * If this value is false, old and no longer used classes won't be removed.
     */
    isCleanOnRebuild = true

    /* Here we can define the classes */
    val ServerGamePacketListenerImpl = reqClass("net.minecraft.server.network.ServerGamePacketListenerImpl")
    val ClientboundUpdateMobEffectPacket =
        reqClass("net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket")
    val ClientboundRemoveMobEffectPacket =
        reqClass("net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket")
    val ClientboundPlayerInfoUpdatePacket =
        reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket")
    val ClientboundPlayerInfoUpdatePacketAction =
        reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket\$Action")
    val ClientboundPlayerInfoUpdatePacketEntry =
        reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket\$Entry")
    val ClientboundPlayerInfoRemovePacket =
        reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket")

    val ServerPlayer = reqClass("net.minecraft.server.level.ServerPlayer")
    val Entity = reqClass("net.minecraft.world.entity.Entity")
    val GameType = reqClass("net.minecraft.world.level.GameType")
    val Component = reqClass("net.minecraft.network.chat.Component")
    val RemoteChatSessionData = reqClass("net.minecraft.network.chat.RemoteChatSession\$Data")
    val Packet = reqClass("net.minecraft.network.protocol.Packet")
    val Connection = reqClass("net.minecraft.network.Connection")


    val MobEffect = reqClass("net.minecraft.world.effect.MobEffect")
    val MobEffectInstance = reqClass("net.minecraft.world.effect.MobEffectInstance")

    Connection
        .reqField("channel")

    ServerPlayer
        .reqField("connection")
    Entity
        .reqMethod("getUUID")
    GameType
        .reqEnumField("SURVIVAL")
        .reqEnumField("CREATIVE")
        .reqEnumField("SPECTATOR")
        .reqEnumField("ADVENTURE")
        .reqMethod("byName", String::class)
    MobEffect
        .reqMethod("byId", Int::class)
    MobEffectInstance
        .reqConstructor(MobEffect, Int::class, Int::class, Boolean::class, Boolean::class, Boolean::class)
        .reqField("effect")

    ServerGamePacketListenerImpl
        .reqMethod("send", Packet)
        .reqField("connection")
    ClientboundUpdateMobEffectPacket
        .reqConstructor(Int::class, MobEffectInstance)
    ClientboundRemoveMobEffectPacket
        .reqConstructor(Int::class, MobEffect)
    ClientboundPlayerInfoUpdatePacket
        .reqConstructor(ClientboundPlayerInfoUpdatePacketAction, ServerPlayer)
        .reqMethod("createPlayerInitializing", Collection::class)
        .reqField("entries")
        .reqMethod("entries")
    ClientboundPlayerInfoUpdatePacketAction
        .reqEnumField("UPDATE_GAME_MODE")
        .reqEnumField("ADD_PLAYER")
    ClientboundPlayerInfoUpdatePacketEntry
        .reqConstructor(
            UUID::class,
            "com.mojang.authlib.GameProfile",
            Boolean::class,
            Int::class,
            GameType,
            Component,
            RemoteChatSessionData
        )
    ClientboundPlayerInfoRemovePacket
        .reqConstructor(List::class)
        .reqField("profileIds")
}