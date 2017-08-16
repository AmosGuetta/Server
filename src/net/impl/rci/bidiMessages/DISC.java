package net.impl.rci.bidiMessages;
import net.api.Connections;
import net.impl.rci.Command;
import net.impl.rci.CommandsHandler;

public class DISC implements Command{
	public DISC(short opcode) {
	}
	@Override
	public void execute(CommandsHandler handler, int myConnectionId, Connections<Command> connectionsHandler) {
		handler.disconnect(myConnectionId, connectionsHandler);	
	}
}
