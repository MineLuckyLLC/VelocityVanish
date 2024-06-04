package net.minelucky.vanish.ruom

import net.kyori.adventure.text.Component
import net.minelucky.vanish.ruom.adventure.AdventureApi
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

object Ruom {

    private val recordedHasPluginSet: MutableSet<String> = HashSet()
    private var debug = false

    @JvmStatic
    val plugin: RUoMPlugin
        get() = RUoMPlugin.get()

    @JvmStatic
    val server: Server
        get() = plugin.server

    val consoleSender: ConsoleCommandSender
        get() = server.consoleSender

    fun hasPlugin(plugin: String): Boolean {
        return if (recordedHasPluginSet.contains(plugin)) true else {
            if (server.pluginManager.getPlugin(plugin) != null &&
                server.pluginManager.isPluginEnabled(plugin)
            ) {
                recordedHasPluginSet.add(plugin)
                true
            } else false
        }
    }

    val onlinePlayers: Set<Player>
        get() = HashSet(server.onlinePlayers)

    fun setDebug(debug: Boolean) {
        Ruom.debug = debug
    }

    fun registerListener(listener: Listener?) {
        RUoMPlugin.get().server.pluginManager.registerEvents(listener!!, RUoMPlugin.get())
    }

    fun unregisterListener(listener: Listener?) {
        HandlerList.unregisterAll(listener!!)
    }

    @JvmStatic
    fun initializeAdventure() {
        AdventureApi.initialize()
    }

    fun broadcast(message: String?) {
        Bukkit.broadcastMessage(message!!)
    }

    fun broadcast(message: Component?) {
        AdventureApi.get().players().sendMessage(message!!)
    }

    fun log(message: String?) {
        RUoMPlugin.get().logger.info(message)
    }

    fun debug(message: String) {
        if (debug) {
            log("[Debug] $message")
        }
    }

    fun warn(message: String?) {
        RUoMPlugin.get().logger.warning(message)
    }

    fun error(message: String?) {
        RUoMPlugin.get().logger.severe(message)
    }

    fun runSync(runnable: Runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable)
    }

    fun runSync(runnable: Runnable, delay: Long) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delay)
    }

    fun runSync(runnable: Runnable, delay: Long, period: Long) {
        Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period)
    }

    fun runAsync(runnable: Runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable)
    }

    fun runAsync(runnable: Runnable, delay: Long) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay)
    }

    fun runAsync(runnable: Runnable, delay: Long, period: Long) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period)
    }

    fun run(runnable: RunnableExc) {
        try {
            runnable.run()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        recordedHasPluginSet.clear()

        try {
            if (AdventureApi.get() != null)
                AdventureApi.get().close()
        } catch (ignore: Exception) {
        }
    }

    fun interface RunnableExc {
        @Throws(Exception::class)
        fun run()
    }
}
