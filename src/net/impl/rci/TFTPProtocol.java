package net.impl.rci;
import net.api.BidiMessagingProtocol;
import net.api.Connections;
import net.impl.rci.bidiMessages.DISC;

public class TFTPProtocol implements BidiMessagingProtocol<Command> {

    private CommandsHandler handler;
    private int myConnectionId;
    private Connections<Command> connectionsHanlder;
    private boolean shouldTerminate = false;
 
    public TFTPProtocol(CommandsHandler handler) {
        this.handler = handler;
    }

	@Override
	public void start(int connectionId, Connections<Command> connections) {
		myConnectionId = connectionId;
		connectionsHanlder = connections;
	}

	@Override
	public void process(Command message){
		if(message instanceof DISC) {
			if(handler.isLoggedin(myConnectionId))
				shouldTerminate = true;
		}
		message.execute(handler,myConnectionId,connectionsHanlder);
	}

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
