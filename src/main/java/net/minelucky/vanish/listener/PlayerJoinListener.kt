package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

class PlayerJoinListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        handleVanishOnJoin(event)
    }

    private fun handleVanishOnJoin(event: PlayerJoinEvent) {
        val player = event.player

        // Note: DiscordSRV support
        player.setMetadata("vanished", FixedMetadataValue(plugin, true))

        for (vanishedPlayer in plugin.getVanishedPlayers().keys.mapNotNull { Bukkit.getPlayer(UUID.fromString(it)) }) {
            println("Vanished Player: " + vanishedPlayer.name)
            plugin.vanishManager.hidePlayer(vanishedPlayer)
            plugin.vanishManager.updateTabState(vanishedPlayer, GameMode.SPECTATOR)

            Ruom.runSync({
                plugin.vanishManager.updateTabState(vanishedPlayer, GameMode.SPECTATOR)
            }, 1)
        }

        println("Vanished: " + plugin.getVanishedPlayers().toString())

        if (plugin.getVanishedPlayers().containsKey(player.uniqueId.toString()))
            plugin.vanishManager.vanish(player, callPostEvent = true)
        else
            plugin.vanishManager.unVanish(player, callPostEvent = false)
    }
}