package Protocol;

public interface Connect<T>  {

    boolean send (int myConnectionId, T msg);

    void broadcast(T msg);

    void disconnect(int myConnectionId);


}
