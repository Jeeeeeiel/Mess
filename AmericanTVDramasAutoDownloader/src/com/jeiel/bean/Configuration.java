package com.jeiel.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.jeiel.main.Log;

public class Configuration {
	private static final String ROOT_PATH = getRootPath();
	private static final String SETTINGS_FILE = ROOT_PATH+"/Configuration.properties";
	public static final String RECORDS_FILE = ROOT_PATH + "/AutodownloadRecords.xls";
	public static final String RUBY_FILE = ROOT_PATH + "/addUri.rb";
	private static List<String>dramasNameList = new ArrayList<String>();
	private static String savePath = "";
	private static String downloader = "";
	private static String username = "";
	private static String password = "";
	private static String smtpHostName = "";
	private static String port = "";
	private static String sendRecords = "false";
	private static List<String>recipients = new ArrayList<String>();
	static{
		Log.log("ROOT_PATH: " + ROOT_PATH);
		File settingsFile = new File(SETTINGS_FILE);
		if(!settingsFile.exists()){
			Log.log("Configuration file not exists!");
			forceQuit();
		}
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(settingsFile)){
			props.load(fis);
			props.list(Log.customStream);
			Log.flush();
			initParameters(props);
			if(!checkParameters()){
				Log.log("Necessary parameters missing!");
				forceQuit();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void initParameters(Properties props){
		Log.log("Initializing parameters...");
		for(Object key : props.keySet()){
			String value = props.getProperty((String) key).trim();
			if(value.length()>0){
				switch((String)key){
				case "Dramas":
					for(String drama : value.split(";")){
						if(!drama.trim().isEmpty()){
							dramasNameList.add(drama.trim());
						}
					}
					Log.log(key + ": " + value);
					break;
				case "SavePath":
					savePath = value;
					Log.log(key + ": " + value);
					break;
				case "Downloader":
					downloader = value;
					Log.log(key + ": " + value);
					break;
				case "username":
					username = value;
					Log.log(key + ": " + value);
					break;
				case "password":
					password = value;
					Log.log(key + ": " + value);
					break;
				case "smtpHostName":
					smtpHostName = value;
					Log.log(key + ": " + value);
					break;
				case "port":
					port = value;
					Log.log(key + ": " + value);
					break;
				case "recipients":
					for(String recipient : value.split(";")){
						if(!recipient.trim().isEmpty()){
							recipients.add(recipient.trim());
						}
					}
					Log.log(key + ": " + value);
					break;
				case "sendRecords":
					port = value;
					Log.log(key + ": " + value);
					break;
					default:
				}
			}
		}
		
	}
	private static boolean checkParameters(){
		Log.log("Checking necessary parameters...");
		if(dramasNameList.isEmpty()){
			Log.log("No drama need to download!");
			return false;
		}
		if(!savePath.isEmpty()){
			File dir = new File(savePath);
			if(!dir.exists()||!dir.isDirectory()||!dir.canWrite()){
				Log.log("savePath unaccessible!");
				return false;
			}
		}else{
			Log.log("No savePath!");
			return false;
		}
		if(!downloader.isEmpty()){
			File file = new File(savePath);
			if(!file.exists()||!file.canRead()){
				Log.log("downloader unaccessible!");
				return false;
			}
		}else{
			Log.log("No downloader!");
			return false;
		}
		
		return true;
	}
	public static void forceQuit(){
		Log.log("Force quit!");
		Log.sendLog(false);
		System.exit(-1);
	}
	public static List<String> getDramasNameList() {
		return dramasNameList;
	}
	public static String getSavePath() {
		return savePath;
	}
	public static String getDownloader() {
		return downloader;
	}
	public static String getUsername() {
		return username;
	}
	public static String getPassword() {
		return password;
	}
	public static String getSmtpHostName() {
		return smtpHostName;
	}
	public static String getPort() {
		return port;
	}
	public static List<String> getRecipients() {
		return recipients;
	}
	public static String getSendRecords() {
		return sendRecords;
	}
	public static String getRootPath(){
		String path = "";
		try{
			path = java.net.URLDecoder.decode(new Configuration().getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),"UTF-8");
			path = path.substring(0, path.lastIndexOf(File.separator));
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return path;
	}
}
