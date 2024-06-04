package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity

        if (plugin.vanishedNames.contains(player.name))
            event.deathMessage = null
    }
}