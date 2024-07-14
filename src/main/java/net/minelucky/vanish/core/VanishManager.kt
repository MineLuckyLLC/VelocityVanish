package net.minelucky.vanish.core

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import net.minelucky.nms.accessors.ClientboundRemoveMobEffectPacketAccessor
import net.minelucky.nms.accessors.ClientboundUpdateMobEffectPacketAccessor
import net.minelucky.nms.accessors.MobEffectAccessor
import net.minelucky.nms.accessors.MobEffectInstanceAccessor
import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.configuration.Message
import net.minelucky.vanish.configuration.Settings
import net.minelucky.vanish.event.PostUnVanishEvent
import net.minelucky.vanish.event.PostVanishEvent
import net.minelucky.vanish.event.PreUnVanishEvent
import net.minelucky.vanish.event.PreVanishEvent
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.utils.NMSUtils
import net.minelucky.vanish.utils.Utils
import net.minelucky.vanish.utils.sendMessage
import net.minelucky.vanish.utils.string.TextReplacement
import org.bukkit.GameMode
import org.bukkit.entity.Creature
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*


class VanishManager(
    private val plugin: GoodbyeGonePoof
) {

    private val flyingPlayers = mutableListOf<UUID>()

    private val potions = mutableSetOf(
        PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 235, false, false),
        PotionEffect(PotionEffectType.FIRE_RESISTANCE, Int.MAX_VALUE, 235, false, false),
//        PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 235, false, false),
    )

    fun updateTabState(player: Player, state: GameMode) {
        if (Settings.seeAsSpectator)
            try {
                val tabPacket = plugin.protocolManager?.createPacket(
                    PacketType.Play.Server.PLAYER_INFO,
                    true
                )

                val infoData = tabPacket?.playerInfoDataLists
                val infoAction = tabPacket?.playerInfoAction

                val playerInfo = infoData?.read(0)

                playerInfo?.add(
                    PlayerInfoData(
                        WrappedGameProfile.fromPlayer(player),
                        0,
                        NativeGameMode.valueOf(state.name),
                        WrappedChatComponent.fromText(player.playerListName)
                    )
                )

                infoData?.write(0, playerInfo)
                infoAction?.write(0, EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE)

                val newTabPacket = PacketContainer(PacketType.Play.Server.PLAYER_INFO, tabPacket?.handle)

                for (onlinePlayer in Ruom.onlinePlayers.filter { it.hasPermission("vanish.admin.seevanished") && it != player })
                    plugin.protocolManager?.sendServerPacket(onlinePlayer, newTabPacket)
            } catch (_: Exception) {
                Ruom.warn("Couldn't vanish player using ProtocolLib")
            }
    }

    fun hidePlayer(player: Player) {
        for (onlinePlayer in Ruom.onlinePlayers.filter { !it.hasPermission("vanish.admin.seevanished") }) {
            val onlinePlayerVanishLevel = getVanishLevel(onlinePlayer)
            if (onlinePlayerVanishLevel >= getVanishLevel(player) && getVanishLevel(player) != 0)
                continue

            println("Hiding player: " + player.name + " for online player: " + onlinePlayer.name)
            onlinePlayer.hidePlayer(player)
        }
    }

    fun getVanishLevel(player: Player): Int {
        return player.effectivePermissions.map { it.permission }
            .filter { it.startsWith("vanish.level.") }.maxOfOrNull { it.split(".")[2].toInt() } ?: 0
    }

    private fun setMeta(player: Player, meta: Boolean) {
        player.setMetadata("vanished", FixedMetadataValue(plugin, meta))
    }

    fun addPotionEffects(player: Player) {
        Ruom.runSync({
            for (potionEffect in potions) {
                if (player.hasPotionEffect(potionEffect.type))
                    continue

                try {
                    @Suppress("DEPRECATION") val mobEffect = MobEffectInstanceAccessor.getConstructor0().newInstance(
                        MobEffectAccessor.getMethodById1().invoke(null, potionEffect.type.id),
                        Int.MAX_VALUE,
                        potionEffect.amplifier,
                        potionEffect.isAmbient,
                        potionEffect.hasParticles()
                    )
                    NMSUtils.sendPacket(
                        player,
                        ClientboundUpdateMobEffectPacketAccessor.getConstructor0()
                            .newInstance(player.entityId, mobEffect)
                    )
                } catch (e: Exception) {
                    player.addPotionEffect(potionEffect)
                }
            }
        }, 2)
    }

    private fun removePotionEffects(player: Player) {
        Ruom.runSync({
            for (potionEffect in potions) {
                try {
                    @Suppress("DEPRECATION") NMSUtils.sendPacket(
                        player,
                        ClientboundRemoveMobEffectPacketAccessor.getConstructor0().newInstance(
                            player.entityId,
                            MobEffectAccessor.getMethodById1().invoke(null, potionEffect.type.id)
                        )
                    )
                } catch (e: Exception) {
                    player.removePotionEffect(potionEffect.type)
                }
            }
        }, 2)
    }

    private fun denyPush(player: Player) {
        var team = player.scoreboard.getTeam("Vanished")

        if (team == null)
            team = player.scoreboard.registerNewTeam("Vanished")

        team.addEntry(player.name)
    }

    private fun allowPush(player: Player) {
        player.scoreboard.getTeam("Vanished")?.removeEntry(player.name)
    }

    fun vanish(player: Player, callPostEvent: Boolean = false) {
        val preVanishEvent = PreVanishEvent(player)
        GoodbyeGonePoof.instance.server.pluginManager.callEvent(preVanishEvent)

        if (preVanishEvent.isCancelled)
            return

        plugin.redisConnection?.sync()?.hset("vanished-players", player.uniqueId.toString(), player.name)

        setMeta(player, true)

        updateTabState(player, GameMode.SPECTATOR)
        hidePlayer(player)

        if (player.isFlying || player.allowFlight)
            flyingPlayers.add(player.uniqueId)

        player.allowFlight = true
        player.isFlying = true
        player.isSleepingIgnored = true
        player.spigot().collidesWithEntities = false

        player.world.entities.stream()
            .filter { entity -> entity is Creature }
            .map { entity -> entity as Creature }
            .filter { mob -> mob.target != null }
            .filter { mob -> player.uniqueId == mob.target?.uniqueId }
            .forEach { mob -> mob.target = null }

        addPotionEffects(player)

        denyPush(player)

        Settings.vanishSound.let {
            if (it != null)
                player.playSound(player.location, it, 1f, 1f)
        }

        Utils.sendVanishActionbar(player)

        if (callPostEvent)
            GoodbyeGonePoof.instance.server.pluginManager.callEvent(
                PostVanishEvent(
                    player
                )
            )

        for (staff in Ruom.onlinePlayers.filter { it.hasPermission("vanish.admin.seevanished") && it != player })
            staff.sendMessage(Message.VANISH_NOTIFY, TextReplacement("player", player.name))
    }

    fun unVanish(player: Player, callPostEvent: Boolean = false) {
        if (!plugin.getVanishedPlayers().containsKey(player.uniqueId.toString()))
            return

        val preUnVanishEvent = PreUnVanishEvent(player)
        GoodbyeGonePoof.instance.server.pluginManager.callEvent(preUnVanishEvent)

        if (preUnVanishEvent.isCancelled)
            return

        plugin.redisConnection?.sync()?.hdel("vanished-players", player.uniqueId.toString())

        setMeta(player, false)

        updateTabState(player, GameMode.SURVIVAL)

        val canFly = player.isOp || player.gameMode == GameMode.CREATIVE || flyingPlayers.contains(player.uniqueId)
        player.allowFlight = canFly
        player.isFlying = canFly
        flyingPlayers.remove(player.uniqueId)

        for (onlinePlayer in Ruom.onlinePlayers)
            onlinePlayer.showPlayer(player)

        player.isSleepingIgnored = false
        player.spigot().collidesWithEntities = true

        removePotionEffects(player)

        Utils.actionbarPlayers.remove(player)

        allowPush(player)

        Utils.sendVanishActionbar(player)

        if (callPostEvent)
            GoodbyeGonePoof.instance.server.pluginManager.callEvent(
                PostUnVanishEvent(
                    player
                )
            )

        for (staff in Ruom.onlinePlayers.filter { it.hasPermission("vanish.admin.seevanished") && it != player })
            staff.sendMessage(Message.UNVANISH_NOTIFY, TextReplacement("player", player.name))
    }
}