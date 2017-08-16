package net.impl.rci.bidiMessages;
import net.api.Connections;
import net.impl.rci.Command;
import net.impl.rci.CommandsHandler;

public class BCAST implements Command{
	private short opcode;
	private byte deleted_or_added;
	private String name;
	
	public BCAST(short opcode, byte deleted_or_added, String name) {
		this.opcode = opcode;
		this.deleted_or_added = deleted_or_added;
		this.name = name;
	}
	@Override
	public void execute(CommandsHandler arg, int myConnectionId, Connections<Command> connectionsHanlder) {
	}

	public short getOpcode() {
		return opcode;
	}

	public void setOpcode(short opcode) {
		this.opcode = opcode;
	}

	public byte getDeleted_or_added() {
		return deleted_or_added;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
