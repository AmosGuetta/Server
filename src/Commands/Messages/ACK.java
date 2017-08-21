package Commands.Messages;

import Protocol.Connect;
import Commands.Command;
import Commands.CommandsHandler;

public class ACK implements Command {
    private short myOpcode;
    private short blockNumber;

    public ACK(short opcode,short blockNumber){
        this.myOpcode = opcode;
        this.blockNumber = blockNumber;

    }

    public short getMyOpcode() {
        return myOpcode;
    }


    public short getBlockNumber() {
        return blockNumber;
    }

    @Override
    public void execute(CommandsHandler commandHandler, int myConnectionId, Connect<Command> connectionsHandler) {

        commandHandler.ackFromClient(blockNumber, myConnectionId, connectionsHandler);

    }
}
