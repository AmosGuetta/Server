package Commands.Messages;

import Protocol.Connect;
import Commands.Command;
import Commands.CommandsHandler;

public class Upload implements Command {
    private  short opcode;
    private String fileName;

    public Upload(short opcode, String fileName){
        this.opcode = opcode;
        this.fileName = fileName;
    }

    @Override
    public void execute(CommandsHandler commandHandler, int myConnectionId, Connect<Command> connectionsHandler) {
        commandHandler.uploadsFile(myConnectionId,connectionsHandler,fileName);
    }
}
