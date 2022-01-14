package dev.projectg.crossplatforms.command.defaults;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import dev.projectg.crossplatforms.CrossplatForms;
import dev.projectg.crossplatforms.Logger;
import dev.projectg.crossplatforms.command.CommandOrigin;
import dev.projectg.crossplatforms.command.FormsCommand;

public class VersionCommand extends FormsCommand {

    private static final String NAME = "version";
    private static final String PERMISSION = "crossplatforms.command" + NAME;

    public VersionCommand(CrossplatForms crossplatForms) {
        super(crossplatForms);
    }

    @Override
    public void register(CommandManager<CommandOrigin> manager, Command.Builder<CommandOrigin> defaultBuilder) {
        manager.command(defaultBuilder.literal(NAME)
                .permission(PERMISSION)
                .handler(context -> {
                    CommandOrigin proxy = context.getSender();
                    proxy.sendMessage(Logger.Level.INFO, "CrossplatForms version:");
                    proxy.sendMessage(Logger.Level.INFO, "Branch: " + crossplatForms.getBranch() + ", Commit: " + crossplatForms.getCommit());
                })
                .build());
    }
}
