package net.minelucky.vanish.listener

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.storage.Settings
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector


class PlayerInteractListener(
    private val plugin: GoodbyeGonePoof
) : Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun preventInteract(event: PlayerInteractEvent) {
        val player = event.player

        if (plugin.vanishedNames.contains(player.name) && Settings.preventInteract && !player.hasPermission("velocityvanish.bypass.prevention.interact"))
            event.isCancelled = true
    }

    @EventHandler
    private fun onActivePlate(event: PlayerInteractEvent) {
        val player = event.player
        val block = event.clickedBlock ?: return

        if (!plugin.vanishedNames.contains(player.name))
            return

        if (event.action != Action.PHYSICAL && event.clickedBlock == null)
            return

        if (!block.type.toString().contains("PLATE"))
            return

        event.isCancelled = true
    }

    private val silentInventoryMaterials = Material.entries.filter { it.name.contains("CHEST") }

    /**
     * Note: Not working when some ProCosmetics cosmetics are enabled!
     */
    @EventHandler
    fun onChestOpen(event: PlayerInteractEvent) {
        if (!Settings.silentOpenContainer)
            return

        if (event.action != Action.RIGHT_CLICK_BLOCK)
            return

        val block = event.clickedBlock ?: return

        val player = event.player

        if (!plugin.vanishedNames.contains(player.name))
            return

        if (block.type == Material.ENDER_CHEST) {
            event.isCancelled = true
            player.openInventory(player.enderChest)
            return
        }

        if (!silentInventoryMaterials.contains(block.type))
            return

        if (player.gameMode == GameMode.SPECTATOR)
            return

        val gamemode = player.gameMode
        val flight = player.allowFlight
        val fly = player.isFlying
        val velocity = player.velocity

        player.allowFlight = true
        player.isFlying = true
        player.gameMode = GameMode.SPECTATOR
        player.velocity = Vector(0.0, 0.0, 0.0)

        Ruom.runSync({
            player.gameMode = gamemode
            player.allowFlight = flight
            player.isFlying = fly
            player.velocity = velocity
        }, 2)
    }
}