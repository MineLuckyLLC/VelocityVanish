package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.storage.Settings
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

        if (Settings.invincible)
            if (plugin.vanishedNames.contains(entity.name))
                event.isCancelled = true
    }
}