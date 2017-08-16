package net.impl.rci.bidiMessages;
import net.api.Connections;
import net.impl.rci.Command;
import net.impl.rci.CommandsHandler;



public class DEL implements Command {
	private String filename;
	
	public DEL(short opcode, String filename) {
		this.filename = filename;
	}

	@Override
	public void execute(CommandsHandler handler, int myConnectionId, Connections<Command> connectionsHandler) {
		handler.deleteFile(filename,myConnectionId, connectionsHandler);
	}

}
