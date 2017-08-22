package Commands.Messages;

import Protocol.Connect;
import Commands.Command;
import Commands.CommandsHandler;

public class Delete implements Command {

    private String fileName;

    public Delete(short opcode, String fileName){
        this.fileName = fileName;
    }

    @Override
    public void execute(CommandsHandler commandHandler, int myConnectionId, Connect<Command> connectionsHandler) {
        commandHandler.deleteFile(myConnectionId, connectionsHandler,fileName);
    }
}
