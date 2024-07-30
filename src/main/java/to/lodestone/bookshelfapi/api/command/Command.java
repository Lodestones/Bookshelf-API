package to.lodestone.bookshelfapi.api.command;

import dev.jorel.commandapi.BukkitExecutable;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.*;
import org.bukkit.command.CommandSender;

import java.util.function.Predicate;

public class Command {

    protected final CommandAPICommand command;
    protected final String name;
    public Command(String name) {
        this.name = name;
        this.command = new CommandAPICommand(name);
    }

    public Command withRequirement(Predicate<CommandSender> requirement) {
        this.command.withRequirement(requirement);
        return this;
    }

    public Command executes(final CommandExecutor executor, ExecutorType... types) {
        this.command.executes(executor, types);
        return this;
    }

    public Command executes(final CommandExecutionInfo executor, ExecutorType... types) {
        this.command.executes(executor, types);
        return this;
    }

    public Command executes(final ResultingCommandExecutionInfo executor) {
        this.command.executes(executor, ExecutorType.values());
        return this;
    }

    public Command optionalArguments(Argument<?> ...args) {
        this.command.withOptionalArguments(args);
        return this;
    }

    public Command arguments(Argument<?> ...args) {
        this.command.withArguments(args);
        return this;
    }

    public Command executesPlayer(PlayerCommandExecutor executor) {
        this.command.executesPlayer(executor);
        return this;
    }

    public Command executesPlayer(PlayerExecutionInfo info) {
        this.command.executesPlayer(info);
        return this;
    }

    public Command executesPlayer(PlayerResultingCommandExecutor executor) {
        this.command.executesPlayer(executor);
        return this;
    }

    public Command executesPlayer(PlayerResultingExecutionInfo info) {
        this.command.executesPlayer(info);
        return this;
    }

    public Command executesEntity(EntityCommandExecutor executor) {
        this.command.executesEntity(executor);
        return this;
    }

    public Command executesEntity(EntityExecutionInfo info) {
        this.command.executesEntity(info);
        return this;
    }

    public Command executesEntity(EntityResultingCommandExecutor executor) {
        this.command.executesEntity(executor);
        return this;
    }

    public Command executesEntity(EntityResultingExecutionInfo info) {
        this.command.executesEntity(info);
        return this;
    }

    public Command executesCommandBlock(CommandBlockCommandExecutor executor) {
        this.command.executesCommandBlock(executor);
        return this;
    }

    public Command executesCommandBlock(CommandBlockExecutionInfo info) {
        this.command.executesCommandBlock(info);
        return this;
    }

    public Command executesCommandBlock(CommandBlockResultingCommandExecutor executor) {
        this.command.executesCommandBlock(executor);
        return this;
    }

    public Command executesCommandBlock(CommandBlockResultingExecutionInfo info) {
        this.command.executesCommandBlock(info);
        return this;
    }

    public Command executesConsole(ConsoleCommandExecutor executor) {
        this.command.executesConsole(executor);
        return this;
    }

    public Command executesConsole(ConsoleExecutionInfo info) {
        this.command.executesConsole(info);
        return this;
    }

    public Command executesConsole(ConsoleResultingCommandExecutor executor) {
        this.command.executesConsole(executor);
        return this;
    }

    public Command executesConsole(ConsoleResultingExecutionInfo info) {
        this.command.executesConsole(info);
        return this;
    }

    public Command executesNative(NativeCommandExecutor executor) {
        this.command.executesNative(executor);
        return this;
    }

    public Command executesNative(NativeExecutionInfo info) {
        this.command.executesNative(info);
        return this;
    }

    public Command executesNative(NativeResultingCommandExecutor executor) {
        this.command.executesNative(executor);
        return this;
    }

    public Command executesNative(NativeResultingExecutionInfo info) {
        this.command.executesNative(info);
        return this;
    }

    public Command permission(String permission) {
        this.command.withPermission(permission);
        return this;
    }

    public Command aliases(String ...aliases) {
        this.command.setAliases(aliases);
        return this;
    }

    public Command subCommand(Command subCommand) {
        this.command.withSubcommand(subCommand.build());
        return this;
    }

    public CommandAPICommand build() {
        return this.command;
    }

    public void register() {
        this.command.register();
    }

}
