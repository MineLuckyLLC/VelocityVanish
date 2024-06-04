package net.minelucky.vanish

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerOptions
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import net.minelucky.vanish.command.VanishCommand
import net.minelucky.vanish.configuration.Settings
import net.minelucky.vanish.core.VanishManager
import net.minelucky.vanish.listener.*
import net.minelucky.vanish.ruom.RUoMPlugin
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.utils.Utils
import net.minelucky.vanish.utils.adventure.AdventureApi
import net.minelucky.vanish.utils.string.component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.logging.Level


class GoodbyeGonePoof : RUoMPlugin() {

    private var redisClient: RedisClient? = null
    var redisConnection: StatefulRedisConnection<String, String>? = null

    lateinit var vanishManager: VanishManager
        private set

    var protocolManager: ProtocolManager? = null

    var vanishedPlayersCache: ConcurrentMap<String, String> = ConcurrentHashMap()

    override fun onEnable() {
        instance = this
        dataFolder.mkdir()

        Settings

        redisClient = RedisClient.create(Settings.redisURI)

        redisClient?.let { client ->
                redisConnection = client.connect()
                logger.log(
                    Level.INFO,
                    "Redis Connected: " + redisConnection?.isOpen + ", PING: " + redisConnection?.sync()?.ping()
                )
        }

        protocolManager = ProtocolLibrary.getProtocolManager()
        AdventureApi.initialize()
        vanishManager = VanishManager(this)

        resetData(true)

        VanishCommand(this)

        initializeListeners()

        Ruom.runAsync({
            updateVanishedPlayersCache()
        }, 20L, 10L)

        protocolManager?.addPacketListener(
            object : PacketAdapter(
                this,
                ListenerPriority.NORMAL,
                listOf(PacketType.Status.Server.SERVER_INFO),
                ListenerOptions.ASYNC
            ) {
                override fun onPacketSending(event: PacketEvent?) {
                    event?.packet?.serverPings?.let { serverPing ->
                        serverPing.read(0).setPlayers(
                            Ruom.onlinePlayers.filter { player -> !getVanishedPlayers().containsKey(player.uniqueId.toString()) }
                                .map { player ->
                                    WrappedGameProfile(player.uniqueId, player.name)
                                }
                        )
                    }
                }
            }
        )
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

    override fun onDisable() {
        Ruom.shutdown()

        resetData(false)

        redisConnection?.close()
        redisClient?.close()
    }

    private fun updateVanishedPlayersCache() {
        val newCache: Map<String, String> = redisConnection?.sync()?.hgetall("vanished-players") ?: emptyMap()
        vanishedPlayersCache = ConcurrentHashMap(newCache)
    }

    fun getVanishedPlayers(): Map<String, String> {
        return vanishedPlayersCache
    }

    private fun sendConsoleMessage(message: String) {
        AdventureApi.get().sender(server.consoleSender).sendMessage(message.component())
    }

    companion object {
        lateinit var instance: GoodbyeGonePoof
    }
}