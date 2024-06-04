package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent

class PlayerChangedWorldListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        val player = event.player

        if (plugin.getVanishedPlayers().containsKey(player.uniqueId.toString()))
            plugin.vanishManager.addPotionEffects(player)
    }
}