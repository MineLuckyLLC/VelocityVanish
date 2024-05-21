package ir.syrent.velocityvanish.spigot.hook

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.hook.hooks.PlaceholderAPIHook
import ir.syrent.velocityvanish.spigot.hook.hooks.ProtocolLibHook

object DependencyManager {

    var protocolLibHook: ProtocolLibHook private set
    var placeholderAPIHook: PlaceholderAPIHook private set

    init {
        ProtocolLibHook("ProtocolLib").apply {
            this.register()
            protocolLibHook = this
        }

        PlaceholderAPIHook(VelocityVanishSpigot.instance, "PlaceholderAPI").apply {
            this.register()
            placeholderAPIHook = this
        }
    }
}