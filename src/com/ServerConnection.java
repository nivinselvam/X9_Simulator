package com;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;



public class ServerConnection extends Thread {
	
	Socket socket;
	Server server;
	DataInputStream din;
	DataOutputStream dout;
	boolean shouldRun = true;
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	Converter converter = new Converter();
	Responses responses;
	public ServerConnection(Socket socket, Server server) {
		super("ServerConnectionThread");
		this.socket = socket;
		this.server = server;
		
	}

	public void sendStringtoClient(String text) {
		try {
			byte[] messageToClient = text.getBytes("ISO-8859-1");
			int messageSize = messageToClient.length;
			dout.writeShort(messageSize);
			dout.write(messageToClient);
			dout.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//
	// public void sendStringToAllClients(String text) {
	// for (int index = 0; index < server.connections.size(); index++) {
	// ServerConnection sc = server.connections.get(index);
	// sc.sendStringtoClient(text);
	// }
	//
	// }

	public void run() {
		String msgin = "", msgout = "";
		System.out.println(socket.getRemoteSocketAddress().toString()+" is connected");
		System.out.println("Client "+socket.getRemoteSocketAddress().toString()+" is connected");
		try {
			din = new DataInputStream(socket.getInputStream());
			dout = new DataOutputStream(socket.getOutputStream());

			while (shouldRun) {
				while (din.available() == 0) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
				
				//HPS Sends the Data length in the first 2 bytes but other FEPs don't send.
				int msgSize = 0;
				if(Main.fepName.equals("X9")) {
					msgSize = din.readShort();
				}else {
					msgSize = din.available();
				}
				
				byte[] message = new byte[msgSize];
				//din.read(message, 0, msgSize);
				din.readFully(message, 0, msgSize);  //new

				StringBuffer requestPacket = new StringBuffer();
				
				for (byte currByte : message) {
					requestPacket.append((String.format("%02x", currByte)));
						
					}
				
				System.out.println(requestPacket.toString());
				/*
				 * for (byte currByte : message) {
				 * requestPacket.append(converter.hexToASCII(String.format("%02x", currByte)));
				 * 
				 * } System.out.println(requestPacket.toString());
				 */
				//responses = new Responses(new String (message,"ISO-8859-1"));
				responses = new Responses(requestPacket.toString());
				System.out.println("*************************************************************************************************");
				System.out.println("                                  Start of Transaction");
				System.out.println("*************************************************************************************************");
				//System.out.println(new String (message,"ISO-8859-1"));
				String responsePacket = "";
					
					responsePacket = converter.hexToASCII(responses.getResponsePacket());
					
				
				sendStringtoClient(responsePacket);
				System.out.println("*************************************************************************************************");
				System.out.println("                                   End of Transaction");
				System.out.println("*************************************************************************************************");
			}
			din.close();
			dout.close();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
