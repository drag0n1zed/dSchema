package io.github.drag0n1zed.universal.api.command;

public abstract class CommandManager implements CommandRegister {

    public abstract void dispatch(Command command);

}
