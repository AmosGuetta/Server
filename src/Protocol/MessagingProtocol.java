package Protocol;

public interface MessagingProtocol<T> {

    void start (int Id, Connect<T> connectionsHandler);
 
    /**
     * process the given message 
     * @param msg the received message
     * @return the response to send or null if no response is expected by the client
     */
    void process(T msg);
 
    /**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
 
}