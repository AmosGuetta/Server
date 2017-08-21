package Commands.Messages;

import Protocol.Connect;
import Commands.Command;
import Commands.CommandsHandler;

public class DATA implements Command {
    private short opcode;
    private short packetSize;
    private short blockNumber;
    private byte[] data;

    public DATA(short opcode, short packetSize, short blockNumber, byte[] data){
        this.opcode = opcode;
        this.packetSize = packetSize;
        this.blockNumber = blockNumber;

        this.data = new byte[data.length];
        for(int i = 0; i < data.length; i++)
            this.data[i] = data[i];
    }

    public short getOpcode() {
        return opcode;
    }

    public short getPacketSize() {
        return packetSize;
    }

    public short getBlockNumber() {
        return blockNumber;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public void execute(CommandsHandler commandHandler, int myConnectionId, Connect<Command> connectionsHandler) {
        commandHandler.parseData(blockNumber, data, myConnectionId, connectionsHandler);
    }
}


