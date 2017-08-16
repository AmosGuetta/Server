package net.impl.rci.bidiMessages;
import net.api.Connections;
import net.impl.rci.Command;
import net.impl.rci.CommandsHandler;

public class WRQ implements Command{
	private String filename;
	
	public WRQ(short opcode, String filename) {
		this.filename = filename;
	}

	@Override
	public void execute(CommandsHandler handler, int myConnectionId, Connections<Command> connectionsHandler) {
		handler.writefile(filename,myConnectionId,connectionsHandler);
	}
}
