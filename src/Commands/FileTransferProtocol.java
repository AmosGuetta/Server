package Commands;

import Commands.Messages.DISC;
import Protocol.MessagingProtocol;
import Protocol.Connect;

public class FileTransferProtocol implements MessagingProtocol<Command> {
    private CommandsHandler commandsHandler;
    private int myConnectionId;
    private Connect<Command> connectionsHandler;

    private boolean shouldTerminate = false;


    public FileTransferProtocol(CommandsHandler commandsHandler){
        this.commandsHandler = commandsHandler;
    }

    @Override
    public void start(int Id, Connect<Command> connectionsHandler) {
        this.myConnectionId = Id;
        this.connectionsHandler = connectionsHandler;
    }

    @Override
    public void process(Command message) {
        if(message instanceof DISC) {
            if (commandsHandler.isLoggedin(myConnectionId) && commandsHandler.checkIfthereOnlyOneClientConnected())
                System.out.println("All the clients disconnect,the server will closing now");
                shouldTerminate = true;
        }
        message.execute(commandsHandler,myConnectionId,connectionsHandler);
    }
    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}

