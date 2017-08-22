package Commands;

import Commands.Messages.Disconnect;
import Protocol.MessagingProtocol;
import Protocol.Connect;

import static java.lang.System.exit;

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
        String clientName = commandsHandler.clients.get(myConnectionId);
        if(message instanceof Disconnect && commandsHandler.isLoggedin(myConnectionId)) {
            System.out.println(clientName +" is disconnected from the server");
            if (commandsHandler.isLoggedin(myConnectionId) && commandsHandler.checkIfthereOnlyOneClientConnected()) {
                System.out.println("All the clients disconnected from thr server,therefore the server will closing now");
                exit(1);
            }
            shouldTerminate = true;

        }
        else
            message.execute(commandsHandler,myConnectionId,connectionsHandler);
    }
    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}

