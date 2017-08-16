package net.impl.rci;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import net.api.MessageEncoderDecoder;
import net.impl.rci.bidiMessages.*;


public class TFTPEncoderDeccoder implements MessageEncoderDecoder<Command>{

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private short currentOpcode = -1;
    @Override
    public Command decodeNextByte(byte nextByte) {
        return pushByte(nextByte); 
    }

    @Override
    public byte[] encode(Command message) {
    	if(message instanceof ACK) {
    		ACK ack = (ACK)message;
    		
    		bytes = new byte[4];
    		bytes[0] = shortToBytes(ack.getOriginalOpcode())[0];
    		bytes[1] = shortToBytes(ack.getOriginalOpcode())[1];
    		bytes[2] = shortToBytes(ack.getBlock_number())[0];
    		bytes[3] = shortToBytes(ack.getBlock_number())[1];
    		return bytes;
    	}
    	else if(message instanceof ERROR) {
    		ERROR error = (ERROR)message;
       		bytes = new byte[4 + error.getError_Message().length() + 1];
    		bytes[0] = shortToBytes(error.getOpcode())[0];
    		bytes[1] = shortToBytes(error.getOpcode())[1];
    		bytes[2] = shortToBytes(error.getError_code())[0];
    		bytes[3] = shortToBytes(error.getError_code())[1];
    		byte[] errormessagebytes = error.getError_Message().getBytes();
    		for (int i = 0; i < errormessagebytes.length; i++)
    			bytes[i + 4] = errormessagebytes[i];
    		bytes[bytes.length - 1] = '\0';
    		return bytes;
    	}
    	else if(message instanceof BCAST) {
    		BCAST broadcast = (BCAST)message;
       		bytes = new byte[4 + broadcast.getName().length()];
    		bytes[0] = shortToBytes(broadcast.getOpcode())[0];
    		bytes[1] = shortToBytes(broadcast.getOpcode())[1];
    		bytes[2] = broadcast.getDeleted_or_added();
    		
    		byte[] filename_message_bytes = broadcast.getName().getBytes();
    		for (int i = 0; i < filename_message_bytes.length; i++)
			  		bytes[i + 3] = filename_message_bytes[i];
    		bytes[bytes.length - 1] = '\0';
    		return bytes;
    	}
    	else if(message instanceof DATA) {
    		DATA datapacket = (DATA)message;
    		
    		bytes = new byte[6 + datapacket.getPacket_size()];
    		bytes[0] = shortToBytes(datapacket.getOriginalOpcode())[0];
    		bytes[1] = shortToBytes(datapacket.getOriginalOpcode())[1];
    		bytes[2] = shortToBytes(datapacket.getPacket_size())[0];
    		bytes[3] = shortToBytes(datapacket.getPacket_size())[1];
    		bytes[4] = shortToBytes(datapacket.getBlock_number())[0];
    		bytes[5] = shortToBytes(datapacket.getBlock_number())[1];
    		
    		byte[] databytes = datapacket.getData();
    		for (int i = 0; i < databytes.length; i++)
		  		bytes[i + 6] = databytes[i];
    		return bytes;
    	}
    	else
    		return null;
    }

    private Command pushByte(byte nextByte) {
        if (len >= bytes.length) 
            bytes = Arrays.copyOf(bytes, len * 2);
        bytes[len] = nextByte;
        
        if(len < 1) {
        	len++;
        	return null;
        }
        	   
        if(len == 1) {
        	byte[] b = new byte[2];
        	b[0] = bytes[0];
        	b[1] = bytes[1];
        	currentOpcode = bytesToShort(b);	
        }

        
        Command command = null;
        switch(currentOpcode) {
	   		case (short)1:
	   		case (short)2:
	   		case (short)7:
	   		case (short)8:
	   			command = createLOG_WRQ_RRQ_DELRQ_Command();
	   			break;
	   			
       		case (short)4:
       			command = createACK_Command();
       			break;

       		case (short)3:
       			command = createDATA_Command(currentOpcode);
		    	break;
       			
       		case (short)5:
       			if(len < 2) {
       				len++;
       				return null;
       			}
       			else {
       			command = new ERROR((short)5,(short)0,"bla"); // for error opcode
	   			currentOpcode = -1;
	   		    bytes = new byte[1 << 10];
	   		    len = 0;
       			}
	   		    break;
       			
	   		    
	   		case (short)6:
       			command = new DIR(currentOpcode);
       			currentOpcode = -1;
       			bytes = new byte[1 << 10];
       			len = 0;
       			break;
       			
       		case (short)10:
       			command = new DISC(currentOpcode);
       			currentOpcode = -1;
       			bytes = new byte[1 << 10];
       			len = 0;
       			break; 
       		default:
	   			command = new ERROR((short)5,(short)4,"bla"); // for error opcode
	   			currentOpcode = -1;
	   		    bytes = new byte[1 << 10];
	   		    len = 0;
	   			break;
        }
       return command;
    }

	private DATA createDATA_Command(short opcode) {
		if(len < 5) {
			len++;
			return null;
		}
		
	
		byte[] b = new byte[2];
		b[0] = bytes[2];
		b[1] = bytes[3];
		short packetsize = bytesToShort(b);
		
		if(len < packetsize + 5 && packetsize != 0)
		{
			len++;
			return null;
		}
		
		b[0] = bytes[4];
		b[1] = bytes[5];
		short blocknumber = bytesToShort(b); 
		
		b = new byte[packetsize];
		for(int i = 0; i < packetsize; i++)
			b[i] = bytes[i + 6];
		
		
		DATA data = new DATA(opcode,packetsize,blocknumber,b);
		currentOpcode = -1;
	    bytes = new byte[1 << 10];
	    len = 0;
		return data;
	}

	private ACK createACK_Command() {
		if(len < 3) {
			len++;
			return null;
		}

		byte[] blockNumberBytes = new byte[2];
	    blockNumberBytes[0] = bytes[2];
	    blockNumberBytes[1] = bytes[3];
	    short block = bytesToShort(blockNumberBytes);
	    ACK ack = new ACK(currentOpcode,block);
	    
	    currentOpcode = -1;
	    bytes = new byte[1 << 10];
	    len = 0;
	    return ack;
	}

	private Command createLOG_WRQ_RRQ_DELRQ_Command() {
		if(bytes[len] != '\0'){
			len++;
			return null;
		}
		Command command = null;
		byte [] buffer = new byte[len - 2];
		for(int i = 2; i < len; i++)
			buffer[i - 2] = bytes[i];
		
		try {
		    switch(currentOpcode) {	
	      		case (short)1:	
					command = new RRQ(currentOpcode,new String(buffer,"UTF-8"));	
	      			break;
	      		case (short)2:
	      			command = new WRQ(currentOpcode,new String(buffer,"UTF-8"));
	      			break;
	      		case (short)7:
	      			command = new LOG(currentOpcode,new String(buffer,"UTF-8"));
	      			break;
	      		case (short)8:
	      			command = new DEL(currentOpcode,new String(buffer,"UTF-8"));
	      			break;
		    }
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    currentOpcode = -1;
	    bytes = new byte[1 << 10];
	    len = 0;
	    return command;
	}

	private short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
	
	private byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
}
