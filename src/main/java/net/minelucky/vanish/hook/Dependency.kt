package net.minelucky.vanish.hook

import net.minelucky.vanish.ruom.Ruom

abstract class Dependency(val name: String) {

    val exists = Ruom.hasPlugin(name)

    open fun features(): List<String> {
       return emptyList()
    }

    open fun description(): List<String> {
        return if (exists) {
            mutableListOf(
                "<green>$name found! dependency hook activated."
            )
        } else {
            mutableListOf(
                "<yellow>You may need to install <green>$name</green> to take full advantage of the plugin features."
            )
        }.apply {
            if (features().isNotEmpty()) this.add("<white>$name advantages are listed below:")
        }
    }

    open fun formatFeature(feature: String): String {
        return "<white>⬤ <gray>$feature"
    }
}