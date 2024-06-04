package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

class PlayerTeleportListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.from.world == event.to?.world) return

        for (vanishedPlayer in plugin.vanishedNames.mapNotNull { Bukkit.getPlayerExact(it) })
            plugin.vanishManager.updateTabState(vanishedPlayer, GameMode.SPECTATOR)
    }
}