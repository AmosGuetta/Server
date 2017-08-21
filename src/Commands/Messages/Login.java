package Commands.Messages;

import Protocol.Connect;
import Commands.Command;
import Commands.CommandsHandler;

public class Login implements  Command {

    private String username;

    public Login(short opcode, String name){
        this.username = name;
    }

    @Override
    public void execute(CommandsHandler commandHandler, int myConnectionId, Connect<Command> connectionsHandler) {
        commandHandler.login(username, myConnectionId, connectionsHandler);

    }
}
