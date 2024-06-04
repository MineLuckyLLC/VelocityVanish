package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTargetEvent

class EntityTargetListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onEntityTarget(event: EntityTargetEvent) {
        val target = event.target

        if (target !is Player)
            return

        if (plugin.vanishedNames.contains(target.name))
            event.target = null
    }
}