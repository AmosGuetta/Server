package net.impl.rci.bidiMessages;
import net.api.Connections;
import net.impl.rci.Command;
import net.impl.rci.CommandsHandler;

public class RRQ implements Command{
	private String filename;

	
	public RRQ(short opcode, String filename) {
		this.filename = filename;
	}
	
	@Override
	public void execute(CommandsHandler handler, int myConnectionId, Connections<Command> connectionsHandler) {
		handler.requestfile(filename,myConnectionId,connectionsHandler);
	}
}
