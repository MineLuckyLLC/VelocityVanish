package net.minelucky.vanish.hook

import net.minelucky.vanish.GoodbyeGonePoof
import net.minelucky.vanish.hook.hooks.PlaceholderAPIHook
import net.minelucky.vanish.hook.hooks.ProtocolLibHook

object DependencyManager {

    var protocolLibHook: ProtocolLibHook private set
    var placeholderAPIHook: PlaceholderAPIHook private set

    init {
        ProtocolLibHook("ProtocolLib").apply {
            protocolLibHook = this
        }

        PlaceholderAPIHook(GoodbyeGonePoof.instance, "PlaceholderAPI").apply {
            placeholderAPIHook = this
        }
    }
}