package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class EntityDamageListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity

        if (entity !is Player)
            return

        if (plugin.getVanishedPlayers().containsKey(entity.uniqueId.toString()))
            event.isCancelled = true
    }
}