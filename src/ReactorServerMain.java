import Commands.FileTransferProtocol;
import Server.Server;
import Commands.CommandsHandler;
import Commands.FileTransferEncoderDecoder;

public class ReactorServerMain {
    public static void main(String[] args) {
        CommandsHandler commandsHandler = new CommandsHandler(); //one shared object
        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                Integer.valueOf(args[0]), //port
                () ->  new FileTransferProtocol(commandsHandler), //protocol factory
                FileTransferEncoderDecoder::new //message encoder decoder factory
        ).serve();

    }
}

