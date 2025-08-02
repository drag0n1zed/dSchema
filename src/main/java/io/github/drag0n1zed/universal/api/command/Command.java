package io.github.drag0n1zed.universal.api.command;

public interface Command {

    default CommandResult execute(CommandSender sender) {
        sender.send(build());
        return CommandResult.SUCCESS;
    }

    String build();

}
