package Commands.Messages;

import Protocol.Connect;
import Commands.Command;
import Commands.CommandsHandler;

public class DIRQ implements Command {
    private short opcode;

    public DIRQ(short opcode){
        this.opcode = opcode;
    }

    @Override
    public void execute(CommandsHandler commandHandler, int myConnectionId, Connect<Command> connectionsHandler) {
        commandHandler.dir(myConnectionId,connectionsHandler);
    }
}
