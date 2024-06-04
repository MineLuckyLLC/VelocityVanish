package net.minelucky.vanish.hook.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.hook.Dependency
import net.minelucky.vanish.ruom.Ruom
import org.bukkit.OfflinePlayer

class PlaceholderAPIHook(plugin: GoodbyeGonePoof, name: String) : Dependency(name) {

    init {
        if (exists) {
            VanishExpansion(plugin).register()
        }
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Access to all placeholders in all plugin messages.",
            "Add plugin placeholders like %velocityvanish_online_total% to PlaceholderAPI."
        )
    }

    class VanishExpansion(
        private val plugin: GoodbyeGonePoof
    ) : PlaceholderExpansion() {
        override fun getIdentifier(): String {
            return Ruom.plugin.description.name.lowercase()
        }

        override fun getAuthor(): String {
            return Ruom.plugin.description.authors.joinToString(", ")
        }

        override fun getVersion(): String {
            return Ruom.plugin.description.version
        }

        override fun persist(): Boolean {
            return true
        }

        override fun canRegister(): Boolean {
            return true
        }

        override fun onRequest(player: OfflinePlayer?, params: String): String? {
            if (params.equals("vanished", true)) {
                return if (plugin.vanishedNames.contains(player?.name)) "true" else "false"
            }

            if (params.equals("count", true)) {
                return plugin.vanishedNamesOnline.size.toString()
            }

            if (params.startsWith("online_")) {
                val type = params.substring(7)

                return if (type.equals("here", true)) {
                    Ruom.onlinePlayers.filter { !plugin.vanishedNamesOnline.contains(it.name) }.size.toString()
                } else if (type.equals("total", true)) {
                    val players = mutableListOf<String>()
                    for (serverPlayers in plugin.proxyPlayers.values.filter { it.isNotEmpty() }) {
                        players.addAll(serverPlayers)
                    }
                    players.filter { !plugin.vanishedNamesOnline.contains(it) }.size.toString()
                } else {
                        (plugin.proxyPlayers[type.lowercase()]?.filter { !plugin.vanishedNamesOnline.contains(it) }?.size ?: 0).toString()
                }
            }

            return null
        }
    }

}