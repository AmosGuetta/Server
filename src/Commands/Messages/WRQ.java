package Commands.Messages;

import Protocol.Connect;
import Commands.Command;
import Commands.CommandsHandler;

public class WRQ implements Command {
    private  short opcode;
    private String fileName;

    public WRQ(short opcode, String fileName){
        this.opcode = opcode;
        this.fileName = fileName;
    }

    @Override
    public void execute(CommandsHandler commandHandler, int myConnectionId, Connect<Command> connectionsHandler) {
        commandHandler.uploadsFile(myConnectionId,connectionsHandler,fileName);
    }
}
