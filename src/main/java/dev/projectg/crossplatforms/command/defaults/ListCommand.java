package dev.projectg.crossplatforms.command.defaults;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import dev.projectg.crossplatforms.Logger;
import dev.projectg.crossplatforms.command.CommandOrigin;
import dev.projectg.crossplatforms.command.FormsCommand;

public class ListCommand implements FormsCommand {

    public static final String NAME = "list";
    public static final String PERMISSION = "crossplatforms.list";

    @Override
    public void register(CommandManager<CommandOrigin> manager, Command.Builder<CommandOrigin> defaultBuilder) {
        manager.command(defaultBuilder.literal(NAME)
                .permission(PERMISSION)
                .handler(context -> {
                    context.getSender().sendMessage(Logger.Level.INFO, "This is suppose to list forms");
                    // todo
                })
                .build());
    }
}
