package net.impl.rci.bidiMessages;

import net.api.Connections;
import net.impl.rci.Command;
import net.impl.rci.CommandsHandler;

public class DATA implements Command{
	private short originalOpcode;
	
	private short packet_size;
	private short block_number;
	private byte[] data;
	
	public DATA(short opcode, short packet_size, short block_number, byte[] data){
		this.packet_size = packet_size;
		this.block_number = block_number;

		this.data = new byte[data.length];
		for (int i = 0; i < data.length; i++)
			this.data[i] = data[i];
		
		originalOpcode = opcode;
	}

	@Override
	public void execute(CommandsHandler handler, int myConnectionId, Connections<Command> connectionsHandler) {
		handler.parseData(packet_size,block_number,data,myConnectionId,connectionsHandler);
	}

	public short getOriginalOpcode() {
		return originalOpcode;
	}

	public short getPacket_size() {
		return packet_size;
	}

	public short getBlock_number() {
		return block_number;
	}

	public byte[] getData() {
		return data;
	}	
}
