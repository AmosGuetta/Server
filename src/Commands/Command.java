package Commands;
import Protocol.Connect;

public interface Command {

    void execute(CommandsHandler commandHandler, int myConnectionId, Connect<Command> connectionsHandler );


}
