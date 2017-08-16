package net.impl.rci.bidiMessages;

import net.api.Connections;
import net.impl.rci.Command;
import net.impl.rci.CommandsHandler;

public class ACK implements Command{
	private short originalOpcode;
	private short block_number;
	
	public ACK(short opcode, short block_number){
		this.block_number = block_number;
		this.originalOpcode = opcode;
	}
	
	
	@Override
	public void execute(CommandsHandler handler, int myConnectionId,Connections<Command> connectionsHandler) {
		handler.ack_from_client(block_number,myConnectionId,connectionsHandler);
	}
	
	public short getOriginalOpcode() {
		return originalOpcode;
	}

	public void setOriginalOpcode(short originalOpcode) {
		this.originalOpcode = originalOpcode;
	}

	public short getBlock_number() {
		return block_number;
	}

	public void setBlock_number(short block_number) {
		this.block_number = block_number;
	}
	}
