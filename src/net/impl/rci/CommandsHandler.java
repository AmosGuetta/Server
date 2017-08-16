package net.impl.rci;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import net.api.Connections;
import net.impl.rci.bidiMessages.*;

public class CommandsHandler {

	private HashMap<Integer,String> clientsNames = new HashMap<Integer,String>();
	private HashMap<Integer,String> fileNamesByConnectionId = new HashMap<Integer,String>();
	private HashMap<Integer,Short> LastPacketBlockNumberByConnectionId = new HashMap<Integer,Short>();
	private HashMap<Integer,ArrayList<DATA>> requestFilePacketsByConnectionId = new HashMap<Integer,ArrayList<DATA>>();
	private HashMap<Integer,String> busyFiles = new HashMap<Integer,String>();
	private final String serverFilesPath = "Files";
	private HashMap<Short,String> errorsTable = new HashMap<Short,String>();
	private Object delObject;
	
	public CommandsHandler() {
		errorsTable.put((short)0,"Not defined, see error message (if any).");
		errorsTable.put((short)1,"File not found - RRQ \\ DELRQ of non-existing file");
		errorsTable.put((short)2, "Access violation - File cannot be written, read or deleted.");
		errorsTable.put((short)3, "Disk full or allocation exceeded - No room in disk");
		errorsTable.put((short)4, "Illegal TFTP operation - Unknown Opcode.");
		errorsTable.put((short)5, "File already exists - File name exists on WRQ.");
		errorsTable.put((short)6, "User not logged in - Any opcode received before Login completes.");
		errorsTable.put((short)7, "User already logged in - Login username already connected.");
		delObject = new Object();
	}
	
	public void login(String username, int myConnectionId, Connections<Command> connectionsHandler) {
		synchronized (clientsNames) {
			if(clientsNames.containsValue(username) || clientsNames.containsKey(myConnectionId)) {
					// User already logged in error
					short key = 7;
					ERROR error = new ERROR((short)5,key,errorsTable.get(key));
					connectionsHandler.send(myConnectionId, error);
					return;
			}
			clientsNames.put(myConnectionId, username);
			ACK ack = new ACK((short)4,(short)0);
			connectionsHandler.send(myConnectionId, ack);
		}
	}
	
	public void errorWhileBadOpcode(int myConnectionId, Connections<Command> connectionsHandler) {
		short key = 4;
		ERROR error = new ERROR((short)5,key,errorsTable.get(key));
		connectionsHandler.send(myConnectionId, error);
	}
	
	public void deleteFile(String filename, int myConnectionId, Connections<Command> connectionsHandler) {
		synchronized (filename) {
			boolean fileexists = false;
			File folder = new File(serverFilesPath);
			File[] listOfFiles = folder.listFiles();
			File file = new File(serverFilesPath + File.separator + filename);
			for (File file1: listOfFiles)
				if(file1.getName().equals(filename))
					fileexists = true;
			
			if(!fileexists){
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
			
			if(!clientsNames.containsKey(myConnectionId)) {
				short key = 6;
				ERROR error = new ERROR((short)5,key,errorsTable.get(key));
				connectionsHandler.send(myConnectionId, error);
				return;
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
	
	public void disconnect(int myConnectionId, Connections<Command> connectionsHandler) {
		if(!clientsNames.containsKey(myConnectionId)) {
			short key = 6;
			ERROR error = new ERROR((short)5,key,errorsTable.get(key));
			connectionsHandler.send(myConnectionId, error);
			return;
		}

		clientsNames.remove(myConnectionId);
		fileNamesByConnectionId.remove(myConnectionId);
		requestFilePacketsByConnectionId.remove(myConnectionId);
		
		ACK ack = new ACK((short)4,(short)0);
		connectionsHandler.send(myConnectionId, ack);
		connectionsHandler.disconnect(myConnectionId);
		
	}
	
	public void dir(int myConnectionId, Connections<Command> connectionsHandler) {
		if(!clientsNames.containsKey(myConnectionId)) {
			short key = 6;
			ERROR error = new ERROR((short)5,key,errorsTable.get(key));
			connectionsHandler.send(myConnectionId, error);
			return;
		}
		
		String answer = "";	
		File folder = new File(serverFilesPath);
		File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++){
	    	if(!fileNamesByConnectionId.containsValue(listOfFiles[i].getName()))
	    		answer += listOfFiles[i].getName() + "\0";
	    }

	    ArrayList<DATA> dataMessages = createQueueOfDataPackets(answer.getBytes());

	    LastPacketBlockNumberByConnectionId.put(myConnectionId, (short)1);
	    DATA nextData = dataMessages.remove(0);
		connectionsHandler.send(myConnectionId,nextData);
		requestFilePacketsByConnectionId.put(myConnectionId,dataMessages);	
	}
	
	private ArrayList<DATA> createQueueOfDataPackets(byte[] fileBytes) {	
		long length = fileBytes.length;
		int numberOfPackets = ((int)(length/512)) + 1;		
		ArrayList<DATA> dataMessages = new ArrayList<DATA>();
		
		byte[] dataBytes = null;
		for (int i = 0; i < numberOfPackets - 1; i++) {
			dataBytes = new byte[512];
			System.arraycopy(fileBytes, 512 * i, dataBytes, 0, 512);
			dataMessages.add(new DATA((short)3,(short)512,(short)(i + 1), dataBytes));
		}
		
		int lastPacketLength = (int)(length - (512 * (numberOfPackets - 1)));
		dataBytes = new byte[lastPacketLength];
		System.arraycopy(fileBytes, 512 * (numberOfPackets - 1), dataBytes, 0, lastPacketLength);

		dataMessages.add(new DATA((short)3,(short)lastPacketLength,(short)numberOfPackets, dataBytes));
		return dataMessages;
		
	}
	
	public void ack_from_client(short block_number, int myConnectionId, Connections<Command> connectionsHandler) {
		ArrayList<DATA> datas = requestFilePacketsByConnectionId.remove(myConnectionId);
		if(block_number != LastPacketBlockNumberByConnectionId.get(myConnectionId)) {
			short key = 0;
			ERROR error = new ERROR((short)5,key,errorsTable.get(key));
			connectionsHandler.send(myConnectionId, error);
			requestFilePacketsByConnectionId.remove(myConnectionId);
			LastPacketBlockNumberByConnectionId.remove(myConnectionId);
			busyFiles.remove(myConnectionId);
		}
		if(datas.size() == 1) { // If i have sent all of my packages of the file
			busyFiles.remove(myConnectionId);
			LastPacketBlockNumberByConnectionId.remove(myConnectionId);
		}
		
		LastPacketBlockNumberByConnectionId.put(myConnectionId, (short)(block_number + 1));
		DATA dataToSend = datas.remove(0);
		requestFilePacketsByConnectionId.put(myConnectionId, datas);
		connectionsHandler.send(myConnectionId,dataToSend);
	}
	
	public void requestfile(String filename, int myConnectionId, Connections<Command> connectionsHandler) {
		synchronized (delObject) {
			boolean fileexists = false;
			File folder = new File(serverFilesPath);
			File[] listOfFiles = folder.listFiles();
			File file = new File(serverFilesPath + File.separator + filename);
			for (File file1: listOfFiles)
				if(file1.getName().equals(filename))
					fileexists = true;
			
			if(!fileexists || fileNamesByConnectionId.containsValue(filename)){
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
			
			if(!clientsNames.containsKey(myConnectionId)) {
				short key = 6;
				ERROR error = new ERROR((short)5,key,errorsTable.get(key));
				connectionsHandler.send(myConnectionId, error);
				return;
			}
			
			BufferedInputStream bis = null;
			byte[] bytes = new byte[(int)file.length()];
			
			try {
				try {
					bis = new BufferedInputStream(new FileInputStream(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					short key = 1;
					ERROR error = new ERROR((short)5,key,errorsTable.get(key));
					connectionsHandler.send(myConnectionId, error);
					return;
				}
				try {
					bis.read(bytes, 0, (int)file.length());
				} catch (IOException e) {
					e.printStackTrace();
					short key = 2;
					ERROR error = new ERROR((short)5,key,errorsTable.get(key));
					connectionsHandler.send(myConnectionId, error);
					return;
				}
			} catch (SecurityException e) {
				e.printStackTrace();
				short key = 2;
				ERROR error = new ERROR((short)5,key,errorsTable.get(key));
				connectionsHandler.send(myConnectionId, error);
				return;
			}
			finally {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			};
			
		    LastPacketBlockNumberByConnectionId.put(myConnectionId, (short)1);
			ArrayList<DATA> dataMessages  = createQueueOfDataPackets(bytes);
		    DATA nextData = dataMessages.remove(0);
			requestFilePacketsByConnectionId.put(myConnectionId,dataMessages);
			busyFiles.put(myConnectionId,filename);
			connectionsHandler.send(myConnectionId,nextData);
		}
	}

	public void writefile(String filename, int myConnectionId, Connections<Command> connectionsHandler) {
		boolean fileexists = false;
		File folder = new File(serverFilesPath);
		File[] listOfFiles = folder.listFiles();
		for (File file: listOfFiles) {
			if(file.getName().equals(filename)) {
				fileexists = true;
			}
		}
		if(!fileexists && !fileNamesByConnectionId.containsValue(filename)) {			
			if(!clientsNames.containsKey(myConnectionId)) {
				short key = 6;
				ERROR error = new ERROR((short)5,key,errorsTable.get(key));
				connectionsHandler.send(myConnectionId, error);
				return;
			}
		    LastPacketBlockNumberByConnectionId.put(myConnectionId, (short)1);
			fileNamesByConnectionId.put(myConnectionId, filename);
			ACK ack = new ACK((short)4,(short)0);
			connectionsHandler.send(myConnectionId, ack);
		}
		else {
			short key = 5;
			ERROR error = new ERROR((short)5,key,errorsTable.get(key));
			connectionsHandler.send(myConnectionId, error);
		}
		
		
	}

	public void parseData(short packet_size, short block_number, byte[] data, int myConnectionId, 
			Connections<Command> connectionsHandler) {
		boolean done = false;
		String filename = fileNamesByConnectionId.get(myConnectionId);
		File f = new File(serverFilesPath + File.separator + filename);
		
		if(block_number != LastPacketBlockNumberByConnectionId.get(myConnectionId)) {
			short key = 0;
			ERROR error = new ERROR((short)5,key,errorsTable.get(key));
			connectionsHandler.send(myConnectionId, error);
			LastPacketBlockNumberByConnectionId.remove(myConnectionId);
			fileNamesByConnectionId.remove(myConnectionId);
			f.delete();
		}
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f,true);
		} catch (FileNotFoundException e) {
			short key = 1;
			ERROR error = new ERROR((short)5,key,errorsTable.get(key));
			connectionsHandler.send(myConnectionId, error);
			return;
		}
		try {
			fos.write(data);
		} catch (IOException e) {
			short key = 2;
			ERROR error = new ERROR((short)5,key,errorsTable.get(key));
			connectionsHandler.send(myConnectionId, error);
			try {
				fos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
		try {
			fos.flush();
		} catch (IOException e) {
			short key = 2;
			ERROR error = new ERROR((short)5,key,errorsTable.get(key));
			connectionsHandler.send(myConnectionId, error);
			try {
				fos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(data.length < 512)
			done = true;
		
		LastPacketBlockNumberByConnectionId.put(myConnectionId, (short)(block_number + 1));
		ACK ack = new ACK((short)4, block_number);
		connectionsHandler.send(myConnectionId, ack);
		
		if(done) {
			BCAST broadCast = new BCAST((short)9,(byte)'\1',filename);
			connectionsHandler.send(myConnectionId, broadCast);
			fileNamesByConnectionId.remove(myConnectionId);
		}
	}
	
	public boolean isLoggedin(int myConnectionId) {
		return clientsNames.containsKey(myConnectionId);
	}

	public void errorMessage(int myConnectionId, Connections<Command> connectionsHandler, short error_code) {
		ERROR error = new ERROR((short)5,error_code,errorsTable.get(error_code));
		connectionsHandler.send(myConnectionId, error);	
	}
}
