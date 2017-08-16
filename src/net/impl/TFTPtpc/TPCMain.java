package net.impl.TFTPtpc;
import net.impl.rci.TFTPProtocol;
import net.srv.Server;
import net.impl.rci.CommandsHandler;
import net.impl.rci.TFTPEncoderDeccoder;


public class TPCMain {

    public static void main(String[] args) {
        CommandsHandler commandsHandler = new CommandsHandler(); //one shared object
        Server.threadPerClient(Integer.valueOf(args[0]),() -> new TFTPProtocol(commandsHandler),TFTPEncoderDeccoder::new).serve();


    }
}

