package Commands.Messages;

import Protocol.Connect;
import Commands.Command;
import Commands.CommandsHandler;

public class BCAST implements Command {
    private short opcode;
    private byte status;
    private String fileName;

    public BCAST(short opcode, byte status, String fileName){
        this.opcode = opcode;
        this.status = status;
        this.fileName = fileName;
    }

    public short getOpcode() {
        return opcode;
    }

    public void setOpcode(short opcode) {
        this.opcode = opcode;
    }

    public Byte getStatus() {
        return status;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public void execute(CommandsHandler commandHandler, int myConnectionId, Connect<Command> connectionsHandler) {
    }
}
