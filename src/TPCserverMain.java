import Commands.FileTransferProtocol;
import Server.Server;
import Commands.CommandsHandler;
import Commands.FileTransferEncoderDecoder;

public class TPCserverMain {
    public static void main(String[] args) {
        CommandsHandler commandsHandler = new CommandsHandler(); // The Shared object
        Server.threadPerClient(Integer.valueOf(args[0]), () -> new FileTransferProtocol(commandsHandler), FileTransferEncoderDecoder::new).serve();
    }
}
