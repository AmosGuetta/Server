package Commands.Messages;

import Protocol.Connect;
import Commands.Command;
import Commands.CommandsHandler;

public class Disconnect implements Command {
    private short opcode;

    public Disconnect(short opcode){
        this.opcode = opcode;
    }

    @Override
    public void execute(CommandsHandler commandHandler, int myConnectionId, Connect<Command> connectionsHandler) {
        commandHandler.disconnect(myConnectionId,connectionsHandler);

    }
}
