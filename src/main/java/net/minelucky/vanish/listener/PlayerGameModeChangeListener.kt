package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent

class PlayerGameModeChangeListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    @EventHandler
    private fun onPlayerGamemodeChange(event: PlayerGameModeChangeEvent) {
        val player = event.player

        if (!plugin.getVanishedPlayers().containsKey(player.uniqueId.toString()))
            return

        plugin.vanishManager.updateTabState(player, GameMode.SPECTATOR)
    }
}