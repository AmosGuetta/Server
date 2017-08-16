package net.impl.TFTPreactor;
import net.impl.rci.TFTPProtocol;
import net.srv.Server;
import net.impl.rci.CommandsHandler;
import net.impl.rci.TFTPEncoderDeccoder;

public class ReactorMain {

    public static void main(String[] args) {
        CommandsHandler commandsHandler = new CommandsHandler(); //one shared object
          Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                Integer.valueOf(args[0]), //port
                () ->  new TFTPProtocol(commandsHandler), //protocol factory
                TFTPEncoderDeccoder::new //message encoder decoder factory
        ).serve();

    }
}

