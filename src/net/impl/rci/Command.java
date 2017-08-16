package net.impl.rci;
import net.api.Connections;

public interface Command {

    void execute(CommandsHandler arg, int myConnectionId, Connections<Command> connectionsHanlder);
}
