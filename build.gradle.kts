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
}

val versionString: String = findProperty("version")!! as String

val slug = "goodbyegonepoof"
group = "net.minelucky"
version = versionString
description = "Modern Vanish"

val mineluckyCredentials: () -> Map<String, String> = {
    val username = System.getenv("MINELUCKY_REPO_USERNAME") ?: ""
    val password = System.getenv("MINELUCKY_REPO_PASSWORD") ?: ""

    if (username.isEmpty() || password.isEmpty())
        println("MINELUCKY_REPO_USERNAME or MINELUCKY_REPO_PASSWORD environment variables are not set or empty.")

    mapOf("username" to username, "password" to password)
}

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        url = uri("https://nexus.minelucky.net/repository/minelucky-snapshots/")
        credentials {
            username = mineluckyCredentials()["username"]
            password = mineluckyCredentials()["password"]
        }
    }

    // Commons-IO
    maven("https://repo.maven.apache.org/maven2/")

    // Configurate-YAML
    maven("https://repo.spongepowered.org/maven")

    // AdventureAPI/MiniMessage
    maven("https://oss.sonatype.org/content/repositories/snapshots/")

    // ProtocolLib
    maven("https://repo.dmulloy2.net/repository/public/")

    // Cloud SNAPSHOT (Dev repository)
    maven("https://repo.masmc05.dev/repository/maven-snapshots/")
}

dependencies {
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("org.spongepowered:configurate-yaml:4.2.0-SNAPSHOT")
    compileOnly("io.lettuce:lettuce-core:6.3.2.RELEASE")

    implementation("org.spigotmc:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("commons-io:commons-io:2.16.1")

    implementation("com.github.cryptomorin:XSeries:9.8.1") { isTransitive = false }

    implementation("net.kyori:adventure-api:4.15.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    implementation("net.kyori:adventure-text-minimessage:4.15.0")

    implementation("cloud.commandframework:cloud-paper:tooltips-SNAPSHOT")
    implementation("cloud.commandframework:cloud-minecraft-extras:tooltips-SNAPSHOT")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "net.minelucky"
            artifactId = "GoodbyeGonePoof"
            version = "3.27.2"
        }
    }

    publishing {
        repositories {
            maven {
                name = "minelucky-releases"
                url = uri("https://nexus.minelucky.net/repository/maven-releases/")
                credentials {
                    username = mineluckyCredentials()["username"]
                    password = mineluckyCredentials()["password"]
                }
            }
        }
    }

    tasks.withType<PublishToMavenLocal> {
        dependsOn(tasks.shadowJar)
    }
}

tasks {
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