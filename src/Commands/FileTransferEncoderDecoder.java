package Commands;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import Protocol.MessageEncoderDecoder;
import Commands.Messages.*;


public class FileTransferEncoderDecoder implements MessageEncoderDecoder<Command> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int length = 0;
    private short currentOpcode = -1;

    // Helper functions.
    private short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    private byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    private DATA createDATAcommand(short opcode) {
        if (length < 5) {
            length++;
            return null;
        } else {
            byte[] dataInfo = new byte[2];
            dataInfo[0] = bytes[2];
            dataInfo[1] = bytes[3];
            short packetSize = bytesToShort(dataInfo);

            if (length < (packetSize + 5) && packetSize != 0) {
                length++;
                return null;
            }
            dataInfo[0] = bytes[4];
            dataInfo[1] = bytes[5];
            short blockNumber = bytesToShort(dataInfo);

            dataInfo = new byte[packetSize];
            for (int i = 0; i < packetSize; i++)
                dataInfo[i] = bytes[i + 6];


            DATA data = new DATA(opcode, packetSize, blockNumber, dataInfo);
            currentOpcode = -1;
            bytes = new byte[1 << 10];
            length = 0;
            return data;
        }
    }

    private ACK createACKcommand() {
        if (length < 3) {
            length++;
            return null;
        } else {
            byte[] blockNumber = new byte[2];
            blockNumber[0] = bytes[2];
            blockNumber[1] = bytes[3];
            short block = bytesToShort(blockNumber);  // create the ack_command.
            ACK ack = new ACK(currentOpcode, block);
            currentOpcode = -1;
            bytes = new byte[1 << 10];
            length = 0;
            return ack;

        }
    }

    private Command createOthersCommand() {
        // Create Login and Upload and Download and Delete command.
        if (bytes[length] != '\0') {
            length++;
            return null;
        } else {
            Command command = null;
            byte[] buffer = new byte[length - 2];
            for (int i = 2; i < length; i++)
                buffer[i - 2] = bytes[i];
            try {
                switch (currentOpcode) {
                    case (short) 1:
                        command = new Download(currentOpcode, new String(buffer, "UTF-8"));
                        break;
                    case (short) 2:
                        command = new Upload(currentOpcode, new String(buffer, "UTF-8"));
                        break;
                    case (short) 7:
                        command = new Login(currentOpcode, new String(buffer, "UTF-8"));
                        break;
                    case (short) 8:
                        command = new Delete(currentOpcode, new String(buffer, "UTF-8"));
                        break;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            currentOpcode = -1;
            bytes = new byte[1 << 10];
            length = 0;

            return command;
        }
    }

    @Override
    public byte[] encode(Command message) {
    if(message instanceof ACK) {
        ACK ack = (ACK)message;
        bytes = new byte[4];
        bytes[0] = shortToBytes(ack.getMyOpcode())[0];
        bytes[1] = shortToBytes(ack.getMyOpcode())[1];
        bytes[2] = shortToBytes(ack.getBlockNumber())[0];
        bytes[3] = shortToBytes(ack.getBlockNumber())[1];
        return bytes;
    }
    else if(message instanceof ERROR) {
        ERROR error = (ERROR)message;
        bytes = new byte[4 + error.getErrorMessage().length() + 1];
        bytes[0] = shortToBytes(error.getOpcode())[0];
        bytes[1] = shortToBytes(error.getOpcode())[1];
        bytes[2] = shortToBytes(error.getErrorCode())[0];
        bytes[3] = shortToBytes(error.getErrorCode())[1];
        byte[] errorMessage = error.getErrorMessage().getBytes();
        for (int i = 0; i < errorMessage.length; i++)
            bytes[i + 4] = errorMessage[i];
        bytes[bytes.length - 1] = '\0';
        return bytes;
    }
    else if(message instanceof BCAST) {
        BCAST broadcast = (BCAST)message;
        bytes = new byte[4 + broadcast.getFileName().length()];
        bytes[0] = shortToBytes(broadcast.getOpcode())[0];
        bytes[1] = shortToBytes(broadcast.getOpcode())[1];
        bytes[2] = broadcast.getStatus();

        byte[] filename_message_bytes = broadcast.getFileName().getBytes();
        for (int i = 0; i < filename_message_bytes.length; i++)
            bytes[i + 3] = filename_message_bytes[i];
        bytes[bytes.length - 1] = '\0';
        return bytes;
    }
    else if(message instanceof DATA) {
        DATA dataPacket = (DATA)message;

        bytes = new byte[6 + dataPacket.getPacketSize()];
        bytes[0] = shortToBytes(dataPacket.getOpcode())[0];
        bytes[1] = shortToBytes(dataPacket.getOpcode())[1];
        bytes[2] = shortToBytes(dataPacket.getPacketSize())[0];
        bytes[3] = shortToBytes(dataPacket.getPacketSize())[1];
        bytes[4] = shortToBytes(dataPacket.getBlockNumber())[0];
        bytes[5] = shortToBytes(dataPacket.getBlockNumber())[1];

        byte[] databytes = dataPacket.getData();
        for (int i = 0; i < databytes.length; i++)
            bytes[i + 6] = databytes[i];
        return bytes;
    }
    else
        return null;
}
    @Override
    public Command decodeNextByte(byte nextByte) {
        if (length >= bytes.length)
            bytes = Arrays.copyOf(bytes, length * 2);
        bytes[length] = nextByte;

        if (length < 1) {
            length++;
            return null;
        }

        if (length == 1) {
            byte[] info = new byte[2];
            info[0] = bytes[0];
            info[1] = bytes[1];
            currentOpcode = bytesToShort(info);
        }

        Command command = null;
        switch (currentOpcode) {
            case (short) 3:
                command = createDATAcommand(currentOpcode);
                break;

            case (short) 4:
                command = createACKcommand();
                break;

            case (short) 5:
                if (length < 2) {
                    length++;
                    return null;
                } else {
                    command = new ERROR((short) 5, (short) 0, "illegalOpcode");
                    currentOpcode = -1;
                    bytes = new byte[1 << 10];
                    length = 0;
                }
                break;

            case (short) 6:
                command = new dir(currentOpcode);
                currentOpcode = -1;
                bytes = new byte[1 << 10];
                length = 0;
                break;

            case (short) 10:
                command = new Disconnect(currentOpcode);
                currentOpcode = -1;
                bytes = new byte[1 << 10];
                length = 0;
                break;

            case (short) 1:
            case (short) 2:
            case (short) 7:
            case (short) 8:
                command = createOthersCommand();
                break;

            default:
                command = new ERROR((short) 5, (short) 4, "errorOpcode"); // for error opcode
                currentOpcode = -1;
                bytes = new byte[1 << 10];
                length = 0;
                break;
        }
        return command;
    }











}
