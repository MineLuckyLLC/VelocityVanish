package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.TabCompleteEvent

class TabCompleteListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onTabComplete(event: TabCompleteEvent) {
        if (event.sender !is Player)
            return

        if (event.sender.hasPermission("velocityvanish.bypass.tabcompleter"))
            return

        if (event.completions.isEmpty())
            return

        val newCollection = mutableListOf<String>()

        for (completion in event.completions) {
            if (plugin.vanishedNames.contains(completion))
                continue

            newCollection.add(completion)
        }

        event.completions = newCollection
    }
}