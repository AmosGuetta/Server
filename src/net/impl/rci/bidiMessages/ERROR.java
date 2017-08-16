package net.impl.rci.bidiMessages;
import net.api.Connections;
import net.impl.rci.Command;
import net.impl.rci.CommandsHandler;

public class ERROR implements Command{
	private short opcode;
	public short getOpcode() {
		return opcode;
	}

	public void setOpcode(short opcode) {
		this.opcode = opcode;
	}

	public short getError_code() {
		return error_code;
	}

	public void setError_code(short error_code) {
		this.error_code = error_code;
	}

	public String getError_Message() {
		return error_Message;
	}

	public void setError_Message(String error_Message) {
		this.error_Message = error_Message;
	}

	private short error_code;
	private String error_Message;
	
	public ERROR(short opcode, short error_code,String errorMessage){
		this.opcode = opcode;
		this.error_code = error_code;
		error_Message = errorMessage;
	}
	
	public void execute(CommandsHandler handler, int myConnectionId, Connections<Command> connectionsHanlder) {
		handler.errorMessage(myConnectionId,connectionsHanlder,error_code);
	}
}
