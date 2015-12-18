package com.jeiel.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Controller {
	
	private static int CONTROLLER_PORT = 25463;
	
	private static ServerSocket serverSocket = null;
	private static Socket server = null;
	private static DataOutputStream dos = null;
	private static DataInputStream dis = null;
	private static List<String>inputList = new ArrayList<String>();//history command record
	
	public static void main(String[] args){
		// TODO Auto-generated method stub
		waitForConnection();
		if(server!=null){
			waitForInput();
		}
		close();
		System.out.println("Exit");
	}
	
	public static void waitForConnection(){
		try {
			System.out.println("Waiting for connect...");
			if(serverSocket==null){
				serverSocket = new ServerSocket(CONTROLLER_PORT);
			}
			server = serverSocket.accept();
			dos = new DataOutputStream(server.getOutputStream());
			dis = new DataInputStream(server.getInputStream());
			System.out.println("Connected: " + server.getRemoteSocketAddress().toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void waitForInput(){
		Scanner in = new Scanner(System.in);
		inputList.clear();
		while(true){
			System.out.print("Type command here:");
			String cmd = in.nextLine();
			inputList.add(cmd);
			if(cmd.equals("exit")){
				in.close();
				sendCommand("EXIT");
				return;
			}
			if(!sendCommand(cmd)){
				continue;
			}
			String result = waitForExecuteResult();
			if(result.equals("RESET")){
				System.out.println("Resetting connection...");
				waitForConnection();
				continue;
			}else{
				System.out.println("Result: " + result);
			}
		}
	}
	
	public static boolean sendCommand(String cmd){
		try{
			dos.writeUTF(cmd);
			dos.flush();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(e.getLocalizedMessage().equals("Socket is closed")||e.getLocalizedMessage().startsWith("Connection reset by peer")){
				waitForConnection();
			}
			e.printStackTrace();
		}
		return false;
	}
	
	public static String waitForExecuteResult(){
		System.out.println("Waiting for result...");
		try {
			server.setSoTimeout(60*1000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			String result = dis.readUTF();
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(e.getLocalizedMessage().equals("Connection reset")){
				return "RESET";
			}else if(e.getLocalizedMessage().equals("Accept timed out")){
				return "RESET";
			}
			e.printStackTrace();
		}
		return "";
	}
	
	public static void close(){
		try {
			if(server!=null){
				server.close();
			}
			if(serverSocket!=null){
				serverSocket.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
