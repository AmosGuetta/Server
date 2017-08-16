package net.srv;
import java.util.HashMap;
import net.api.Connections;
import net.srv.bidi.ConnectionHandler;

public class ConnectionsImpl<T> implements Connections<T> {

	HashMap<Integer, ConnectionHandler<T>> connectionsHandler = new HashMap<Integer, ConnectionHandler<T>>();
	
	@Override
	public boolean send(int connectionId, T msg) {
		connectionsHandler.get(connectionId).send(msg);
		return true;
	}

	@Override
	public void broadcast(T msg) {
		Integer[] keys = new Integer[connectionsHandler.keySet().size()];
		connectionsHandler.keySet().toArray(keys);		
		for(Integer key : keys) 
			send(key,msg);
	}

	@Override
	public void disconnect(int connectionId) {
		connectionsHandler.remove(connectionId);
	}

	public void add(int connectionID, ConnectionHandler<T> handler) {
		connectionsHandler.put(connectionID, handler);
	}
}
