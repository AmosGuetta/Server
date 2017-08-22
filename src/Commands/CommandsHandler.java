package Commands;

import Protocol.Connect;
import Commands.Messages.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class CommandsHandler {
    private Object deleteObj;
    private final String serverFilesPath = "Files"; // The server files path.
    private HashMap<Short, String> errorsTable = new HashMap<Short, String>(); // Error table.
    public HashMap<Integer, String> clients = new HashMap<Integer, String>();  //  The clients names.
    private HashMap<Integer, String> fileName = new HashMap<Integer, String>(); // The files of each clients.
    private HashMap<Integer, Short> lastPacketBlockNumber = new HashMap<Integer, Short>(); // The last block number packet, of each clients.
    private HashMap<Integer, ArrayList<DATA>> requestFilePackets = new HashMap<Integer, ArrayList<DATA>>(); // The files request of each clients.
    private HashMap<Integer, String> busyFiles = new HashMap<Integer, String>(); //  Files in progress.

    public CommandsHandler() {
        //Constructor.
        this.deleteObj = new Object();
        this.errorsTable.put((short) 0, "Not defined, see error message (if any).");
        this.errorsTable.put((short) 1, "File not found - Download \\ Delete of non-existing file");
        this.errorsTable.put((short) 2, "Access violation - File cannot be written, read or deleted.");
        this.errorsTable.put((short) 3, "Disk full or allocation exceeded - No room in disk");
        this.errorsTable.put((short) 4, "Illegal TFTP operation - Unknown Opcode.");
        this.errorsTable.put((short) 5, "File already exists - File name exists on Upload.");
        this.errorsTable.put((short) 6, "User not logged in - Any opcode received before Login completes.");
        this.errorsTable.put((short) 7, "User already logged in - Login username already connected.");
    }


    private ArrayList<DATA> arrayOfDataPackets(byte[] bytes) {
        long length = bytes.length;
        int numOfPackets = ((int)(length / 512)) + 1;
        ArrayList<DATA> data = new ArrayList<>();
        byte[] dataBytes;
        for(int i = 0; i < numOfPackets - 1; i++){
            dataBytes = new byte[512];
            System.arraycopy(bytes, 512 * i, dataBytes, 0, 512);
            data.add(new DATA((short)3,(short)512,(short)(i + 1), dataBytes));
        }
        // calculate the size of the last packet.
        int lastPacketLength = (int)(length - (512 * (numOfPackets - 1)));
        dataBytes = new byte[lastPacketLength];
        System.arraycopy(bytes, 512 * (numOfPackets - 1), dataBytes, 0, lastPacketLength);

        data.add(new DATA((short)3,(short)lastPacketLength,(short)numOfPackets, dataBytes));
        return data;
    }

    private void checkBufferedOutputStream(FileOutputStream outputStream, short key, int myConnectionId, Connect<Command> connectionsHandler){
        ERROR error = new ERROR((short)5,key,errorsTable.get(key));
        connectionsHandler.send(myConnectionId, error);
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    private void checkBufferedInputStream(BufferedInputStream bufferInput, byte[] bytes, short key, int myConnectionId, File file, Connect<Command> connectionsHandler){
        try {
            bufferInput.read(bytes, 0, (int)file.length());
        } catch (IOException e) {
            e.printStackTrace();
            ERROR error = new ERROR((short)5,key,errorsTable.get(key));
            connectionsHandler.send(myConnectionId, error);
            return;
        }
    }

    boolean isLoggedin(int myConnectionId){
        return clients.containsKey(myConnectionId);
    }

    boolean checkIfthereOnlyOneClientConnected(){
        return (clients.size() == 1);
    }

    public void login(String username, int myConnectionId, Connect<Command> connectionsHandler){
        synchronized (clients){
            if (clients.containsValue(username) || clients.containsKey(myConnectionId)){
                //User already logged in.

                short key = 7;
                ERROR error = new ERROR((short)5, key, errorsTable.get(key));
                connectionsHandler.send(myConnectionId, error);
                return;
            }

                // User not logged yet.

            clients.put(myConnectionId,username);
            ACK ack = new ACK((short)4,(short)0);
            connectionsHandler.send(myConnectionId,ack);
            System.out.println(username +" " + "is login now");
            //System.out.println("Client" + myConnectionId + " :is login now");
            }
        }

    public void disconnect(int myConnectionId, Connect<Command> connectionsHandler){
        if(clients.containsKey(myConnectionId)){  //User already logged in.
            ACK ack = new ACK((short)4, (short)0);
            connectionsHandler.send(myConnectionId, ack);
            clients.remove(myConnectionId);
            fileName.remove(myConnectionId);
            busyFiles.remove(myConnectionId);
            requestFilePackets.remove(myConnectionId);

            Disconnect disconnected = new Disconnect((short)10);
            connectionsHandler.send(myConnectionId,disconnected);
            connectionsHandler.disconnect(myConnectionId);
        }
        else{  // User not logged and want to do some operations.
            short key = 6;
            ERROR error = new ERROR((short)5, key, errorsTable.get(key));
            connectionsHandler.send(myConnectionId,error);
            return;
        }
    }

    public void errorMessage(int myConnectionId, Connect<Command> connectionsHandler, short errorCode){
        ERROR error = new ERROR((short)5, errorCode,errorsTable.get(errorCode));
        connectionsHandler.send(myConnectionId,error);
        return;
    }

    public void ackFromClient(short blockNumber, int myConnectionId, Connect<Command> connectionsHandler){
        ArrayList<DATA> requestData = requestFilePackets.remove(myConnectionId);

        if(blockNumber != lastPacketBlockNumber.get(myConnectionId)){
            short key = 0;
            ERROR error = new ERROR((short)5, key, errorsTable.get(key));
            connectionsHandler.send(myConnectionId, error);
            requestFilePackets.remove(myConnectionId);
            lastPacketBlockNumber.remove(myConnectionId);
            busyFiles.remove(myConnectionId);
        }

        if(requestData.size() == 1) {
            // I sent all my packages of the file.
            busyFiles.remove(myConnectionId);
            lastPacketBlockNumber.remove(myConnectionId);

        }

        lastPacketBlockNumber.put(myConnectionId, (short)(blockNumber + 1));
        DATA dataToSend = requestData.remove(0);
        requestFilePackets.put(myConnectionId, requestData);
        connectionsHandler.send(myConnectionId,dataToSend);

        }

    public void dir(int myConnectionId, Connect<Command> connectionsHandler) {
        if (clients.containsKey(myConnectionId)) {  //User already logged in.
            String ans = "";
            File folder = new File(serverFilesPath);
            File[] filesList = folder.listFiles();

            for (int i = 0; i < filesList.length; i++) {
                if (!fileName.containsValue(filesList[i].getName()))
                    ans = ans + filesList[i].getName() + "\r\n";
            }

            ArrayList<DATA> data = arrayOfDataPackets(ans.getBytes());  // convert the answer to bytes, and division the information into blocks of size 512.
            lastPacketBlockNumber.put(myConnectionId, (short) 1);        //
            requestFilePackets.put(myConnectionId, data);
            DATA nextData = data.remove(0);                      // remove the first packet.
            connectionsHandler.send(myConnectionId, nextData);          // The next packet.
        } else {
            short key = 6;
            ERROR error = new ERROR((short) 5, key, errorsTable.get(key));
            connectionsHandler.send(myConnectionId, error);
            return;
        }
    }

    public void parseData(short blockNumber, byte[] data, int myConnectionId, Connect<Command> connectionsHandler) {
        boolean done = false;
        String filename = fileName.get(myConnectionId);
        File f = new File(serverFilesPath + File.separator + filename);

        if (blockNumber != lastPacketBlockNumber.get(myConnectionId)) {
            short key = 0;
            ERROR error = new ERROR((short) 5, key, errorsTable.get(key));
            connectionsHandler.send(myConnectionId, error);
            lastPacketBlockNumber.remove(myConnectionId);
            fileName.remove(myConnectionId);
            f.delete();
        }

        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(f, true);
        } catch (FileNotFoundException e) {
            short key = 1;
            ERROR error = new ERROR((short) 5, key, errorsTable.get(key));
            connectionsHandler.send(myConnectionId, error);
            return;
        }

        try {
            outputStream.write(data);
        } catch (IOException e) {
            checkBufferedOutputStream(outputStream, (short) 2, myConnectionId, connectionsHandler);
        }

        try {
            outputStream.flush();
        } catch (IOException e) {
            checkBufferedOutputStream(outputStream, (short) 2, myConnectionId, connectionsHandler);
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (data.length < 512)
            done = true;

        lastPacketBlockNumber.put(myConnectionId, (short) (blockNumber + 1));
        ACK ack = new ACK((short) 4, blockNumber);
        connectionsHandler.send(myConnectionId, ack);

        if (done) {
            BCAST broadCast = new BCAST((short) 9, (byte) '\1', filename);
            connectionsHandler.send(myConnectionId, broadCast);
            fileName.remove(myConnectionId);
        }
    }

    public void uploadsFile(int myConnectionId, Connect<Command> connectionsHandler, String name) {
        boolean fileExists = false;
        File folder = new File(serverFilesPath);
        File[] filesList = folder.listFiles();
        for (File file : filesList) {
            if (file.getName().equals(name))
                fileExists = true;
        }

        if (fileExists) {
            short key = 5;
            ERROR error = new ERROR((short) 5, key, errorsTable.get(key));
            connectionsHandler.send(myConnectionId, error);

        } else {
            if (!fileName.containsValue(name)) {
                if (clients.containsKey(myConnectionId)) {
                    lastPacketBlockNumber.put(myConnectionId, (short) 1);
                    fileName.put(myConnectionId, name);
                    ACK ack = new ACK((short) 4, (short) 0);
                    connectionsHandler.send(myConnectionId, ack);
                }
                else {
                    short key = 6;
                    ERROR error = new ERROR((short) 5, key, errorsTable.get(key));
                    connectionsHandler.send(myConnectionId, error);
                    return;
                }
            }
        }
    }

    public void downloadsFile(int myConnectionId, Connect<Command> connectionsHandler, String name){
        synchronized (deleteObj){
            boolean fileExists = false;
            File folder = new File(serverFilesPath);
            File[] filesList = folder.listFiles();
            File file = new File(serverFilesPath + File.separator + name);
            for (File file1 : filesList) {
                if (file1.getName().equals(name))
                    fileExists = true;
            }

            if(!clients.containsKey(myConnectionId)){
                short key = 6;
                ERROR error = new ERROR((short)5,key,errorsTable.get(key));
                connectionsHandler.send(myConnectionId, error);
                return;
            }

            if(!fileExists || fileName.containsValue(name)){
                short key = 1;
                ERROR error = new ERROR((short)5,key,errorsTable.get(key));
                connectionsHandler.send(myConnectionId, error);
                return;
            }

            if(!file.getParentFile().canWrite()) {
                short key = 2;
                ERROR error = new ERROR((short)5,key,errorsTable.get(key));
                connectionsHandler.send(myConnectionId, error);
                return;
            }

            BufferedInputStream bufferInput = null;
            byte[] bytes = new byte[(int)file.length()];
            try {
                try {
                    bufferInput = new BufferedInputStream(new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    short key = 1;
                    ERROR error = new ERROR((short)5,key,errorsTable.get(key));
                    connectionsHandler.send(myConnectionId, error);
                    return;
                }
                checkBufferedInputStream(bufferInput, bytes, (short)2, myConnectionId, file, connectionsHandler);
            } catch (SecurityException e) {
                e.printStackTrace();
                short key = 2;
                ERROR error = new ERROR((short)5,key,errorsTable.get(key));
                connectionsHandler.send(myConnectionId, error);
                return;
            }

            finally {
                try {
                    bufferInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            lastPacketBlockNumber.put(myConnectionId, (short)1);
            ArrayList<DATA> dataMessages  = arrayOfDataPackets(bytes);
            DATA nextData = dataMessages.remove(0);
            requestFilePackets.put(myConnectionId,dataMessages);
            busyFiles.put(myConnectionId,name);
            connectionsHandler.send(myConnectionId,nextData);
        }
    }

    public void deleteFile(int myConnectionId, Connect<Command> connectionsHandler, String filename){
        synchronized (filename) {
            boolean fileExists = false;
            File folder = new File(serverFilesPath);
            File[] filesList = folder.listFiles();
            File file = new File(serverFilesPath + File.separator + filename);
            for (File file1 : filesList) {
                if (file1.getName().equals(filename))
                    fileExists = true;
            }

            if(!clients.containsKey(myConnectionId)) {
                ERROR error = new ERROR((short)5,(short)6,errorsTable.get(6));
                connectionsHandler.send(myConnectionId, error);
                return;
            }

            if(!fileExists){
                short key = 1;
                ERROR error = new ERROR((short)5,key,errorsTable.get(key));
                connectionsHandler.send(myConnectionId, error);
                return;
            }

            if(!file.getParentFile().canWrite())
            {
                short key = 2;
                ERROR error = new ERROR((short)5,key,errorsTable.get(key));
                connectionsHandler.send(myConnectionId, error);
            }

            while(busyFiles.containsValue(filename)) {
            }
            try {
                file.delete();
                ACK ack = new ACK((short)4,(short)0);
                connectionsHandler.send(myConnectionId, ack);
                BCAST broadCast = new BCAST((short)9,(byte)'\0', filename);
                connectionsHandler.broadcast(broadCast);
            }
            catch(SecurityException e) {
                short key = 2;
                ERROR error = new ERROR((short)5,key,errorsTable.get(key));
                connectionsHandler.send(myConnectionId, error);
            }
        }
    }

    public HashMap<Integer, String> getClients() {
        return clients;
    }
}

