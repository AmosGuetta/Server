package net.impl.rci.bidiMessages;
import net.api.Connections;
import net.impl.rci.Command;
import net.impl.rci.CommandsHandler;


public class DIR implements Command{
	private short originalOpcode;
	private short badOpcode = (short)11;
	
	public DIR(short opcode) {
		originalOpcode = opcode;
	}
	@Override
	public void execute(CommandsHandler handler, int myConnectionId, Connections<Command> connectionsHandler) {
		if(badOpcode == originalOpcode) {
			handler.errorWhileBadOpcode(myConnectionId, connectionsHandler);
		}
		else
			handler.dir(myConnectionId,connectionsHandler);
	}
}
