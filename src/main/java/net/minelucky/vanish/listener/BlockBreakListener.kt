package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.configuration.Settings
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BlockBreakListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    @EventHandler
    private fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player

        if (plugin.getVanishedPlayers().containsKey(player.uniqueId.toString()) && Settings.preventBlockBreak && !player.hasPermission("vanish.bypass.prevention.block_break"))
            event.isCancelled = true
    }
}