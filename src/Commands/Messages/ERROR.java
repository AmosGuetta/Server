package Commands.Messages;

import Protocol.Connect;
import Commands.Command;
import Commands.CommandsHandler;



public class ERROR implements Command {
    private short opcode;
    private short errorCode;
    private String errorMessage;


    public ERROR (short opcode, short errorCode, String errorMessage){
        this.opcode = opcode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;

    }

    public short getOpcode() {
        return opcode;
    }

    public void setOpcode(short opcode) {
        this.opcode = opcode;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }




    @Override
    public void execute(CommandsHandler commandHandler, int myConnectionId, Connect<Command> connectionsHandler) {
        commandHandler.errorMessage(myConnectionId,connectionsHandler,errorCode);
    }
}
