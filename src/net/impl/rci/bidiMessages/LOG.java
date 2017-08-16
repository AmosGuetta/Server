package net.impl.rci.bidiMessages;
import net.api.Connections;
import net.impl.rci.Command;
import net.impl.rci.CommandsHandler;

public class LOG implements Command {
	private String username;
	
	public LOG(short opcode, String username) {
		this.username = username;
	}
	@Override
	public void execute(CommandsHandler handler, int myConnectionId, Connections<Command> connectionsHandler) {
		handler.login(username, myConnectionId, connectionsHandler);
	}
}
