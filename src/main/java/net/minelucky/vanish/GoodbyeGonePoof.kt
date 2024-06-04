package net.minelucky.vanish

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerOptions
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import net.minelucky.vanish.command.VanishCommand
import net.minelucky.vanish.core.VanishManager
import net.minelucky.vanish.hook.DependencyManager
import net.minelucky.vanish.listener.*
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.ruom.adventure.AdventureApi
import net.minelucky.vanish.storage.Settings
import net.minelucky.vanish.utils.Utils
import net.minelucky.vanish.utils.component


class GoodbyeGonePoof : net.minelucky.vanish.ruom.RUoMPlugin() {

//    var bridgeManager: BukkitBridgeManager? = null
    lateinit var vanishManager: VanishManager
        private set

    val proxyPlayers = mutableMapOf<String, List<String>>()
    val vanishedNames = mutableSetOf<String>()
    val vanishedNamesOnline = mutableSetOf<String>()

    override fun onEnable() {
        instance = this
        dataFolder.mkdir()

        initializeInstances()
        resetData(true)
        sendWarningMessages()
        initializeCommands()
        initializeListeners()

        if (DependencyManager.protocolLibHook.exists) {
            DependencyManager.protocolLibHook.protocolManager?.addPacketListener(
                object : PacketAdapter(
                    this,
                    ListenerPriority.NORMAL,
                    listOf(PacketType.Status.Server.SERVER_INFO),
                    ListenerOptions.ASYNC
                ) {
                    override fun onPacketSending(event: PacketEvent?) {
                        event?.packet?.serverPings?.let { serverPing ->
                            serverPing.read(0).setPlayers(
                                Ruom.onlinePlayers.filter { player -> !vanishedNames.contains(player.name) }
                                    .map { player ->
                                        WrappedGameProfile(player.uniqueId, player.name)
                                    }
                            )
                        }
                    }
                }
            )
        }
    }

    private fun sendWarningMessages() {
        DependencyManager
    }

    private fun resetData(startup: Boolean) {
        try {
            for (player in Ruom.onlinePlayers) {
                if (startup)
                    Utils.sendVanishActionbar(player)

                vanishManager.unVanish(player)
            }
        } catch (_: Exception) {
            Ruom.warn("Plugin didn't fully complete reset data task on plugin shutdown")
        }
    }

    private fun initializeInstances() {
        AdventureApi.initialize()
        vanishManager = VanishManager(this)

        Settings
    }

    private fun initializeCommands() {
        VanishCommand(this)
    }

    private fun initializeListeners() {
        PlayerJoinListener(this)
        PlayerQuitListener(this)
        PlayerInteractListener(this)
        PlayerTeleportListener(this)
        PlayerDeathListener(this)
        EntityDamageListener(this)
        PlayerItemPickupListener(this)
        EntityTargetListener(this)
        PlayerChangedWorldListener(this)
        BlockBreakListener(this)
        BlockPlaceListener(this)
        PlayerGameModeChangeListener(this)
    }

//    private fun initializePluginChannels() {
//        val bridge = BukkitBridge()
//        bridgeManager = BukkitBridgeManager(bridge, this)
//
//        object : BukkitMessagingEvent(bridge) {
//            override fun onPluginMessageReceived(player: Player, jsonObject: JsonObject) {
//                bridgeManager!!.handleMessage(jsonObject)
//            }
//        }
//    }

    override fun onDisable() {
        Ruom.shutdown()

        resetData(false)
    }

    private fun sendConsoleMessage(message: String) {
        AdventureApi.get().sender(server.consoleSender).sendMessage(message.component())
    }

    companion object {
        lateinit var instance: GoodbyeGonePoof
    }
}