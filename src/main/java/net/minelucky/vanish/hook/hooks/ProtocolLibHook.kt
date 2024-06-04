package net.minelucky.vanish.hook.hooks

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import net.minelucky.vanish.hook.Dependency

class ProtocolLibHook(name: String) : Dependency(name) {

    var protocolManager: ProtocolManager? = null

    init {
        if (exists) {
            protocolManager = ProtocolLibrary.getProtocolManager()
        }
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Change player name type to spectator whenever player vanishes (Applies to tab and player character)"
        )
    }
}