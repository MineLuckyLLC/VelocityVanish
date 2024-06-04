package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.storage.Settings
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockPlaceListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    @EventHandler
    private fun onBlockBreak(event: BlockPlaceEvent) {
        val player = event.player

        if (plugin.vanishedNames.contains(player.name) && Settings.preventBlockPlace && !player.hasPermission("velocityvanish.bypass.prevention.block_place"))
            event.isCancelled = true
    }
}