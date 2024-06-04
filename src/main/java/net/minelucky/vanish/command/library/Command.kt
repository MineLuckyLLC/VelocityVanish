package net.minelucky.vanish.command.library

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.Command
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.minelucky.vanish.command.library.interfaces.ICommand
import net.minelucky.vanish.command.library.interfaces.ISender
import net.minelucky.vanish.ruom.Ruom
import net.minelucky.vanish.utils.string.component
import org.bukkit.command.CommandSender

abstract class Command(
    val name: String,
    val permission: String? = null,
    vararg val aliases: String,
) : ICommand {

    var ERROR_PREFIX = "<dark_gray>[</dark_gray><dark_red><bold>✘</bold></dark_red><dark_gray>]</dark_gray><gradient:dark_red:red>"

    var manager: PaperCommandManager<ISender>
    var builder: Command.Builder<ISender>
    var help: MinecraftHelp<ISender>

    init {
        val senderMapper = { commandSender: CommandSender -> Sender(commandSender) }
        val backwardsMapper = { sayanSender: ISender -> sayanSender.getSender() }
        val audienceMapper = { sayanSender: ISender -> BukkitAudiences.create(Ruom.plugin).sender(sayanSender.getSender()) }

        manager = PaperCommandManager(
            Ruom.plugin,
            CommandExecutionCoordinator.simpleCoordinator(),
            senderMapper,
            backwardsMapper
        )

        manager.createCommandHelpHandler()

        try {
            manager.registerAsynchronousCompletions()
        } catch (_: IllegalStateException) {
            Ruom.warn("Failed to enable asynchronous command completions.")
        }

        MinecraftExceptionHandler<ISender>()
            .withArgumentParsingHandler()
            .withInvalidSenderHandler()
            .withInvalidSyntaxHandler()
            .withNoPermissionHandler()
            .withCommandExecutionHandler()
            .withDecorator { message -> ERROR_PREFIX.component().append(Component.space()).append(message) }
            .apply(manager, audienceMapper)

        help = MinecraftHelp(
            "/${name} help",
            audienceMapper,
            manager
        )

        builder = manager.commandBuilder(name, *aliases).permission(permission ?: getPermission(name))
    }

    fun addLiteral(name: String, description: ArgumentDescription? = null, vararg aliases: String): Command.Builder<ISender> {
        return builder.literal(name, description ?: ArgumentDescription.empty(), *aliases)
    }

    fun saveCommand(command: Command<ISender>) {
        manager.command(command)
        manager.commandRegistrationHandler().registerCommand(command)
    }

    fun saveCommand(commandBuilder: Command.Builder<ISender>) {
        saveCommand(commandBuilder.build())
    }

    override fun setErrorPrefix(prefix: String) {
        ERROR_PREFIX = prefix
    }
}