package com.jeiel.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Proxy {
	public static String PROPERTIES_PATH = AutoDownloaderMultiThread.ROOT_DIR_PATH + "//InitArgs.xml";
	private static int currentPorxy = -1;
	public static final String NOPROXYAVALIABLE = "No proxy avaliable!" ;
	
	private static List<String> proxyList = new ArrayList<String>();
	
	static{
		Log.log("Initializing proxy...");
		Properties props = new Properties();
		try {
			props.loadFromXML(new FileInputStream(new File(PROPERTIES_PATH)));
			if(props.containsKey("proxy")){
				for(String p : props.getProperty("proxy").split(";")){
					if(p.trim().length()>0){
						Log.log("Adding proxy: " + p.trim());
						proxyList.add(p.trim());
					}
				}
			}
			System.out.println("Proxy amount: " + proxyList.size());
			if(Proxy.changeProxy().equals(Proxy.NOPROXYAVALIABLE)){
				Log.log(Proxy.NOPROXYAVALIABLE);
				Log.log("Force quit!");
				System.exit(-1);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		
	}
	
	public static synchronized String changeProxy(){
		if(proxyList.size()==0){
			return NOPROXYAVALIABLE;
		}
		currentPorxy = (currentPorxy + 1) % proxyList.size();
		Log.log("Use proxy" + currentPorxy + ": " + getCurrentProxy());
		System.setProperty("http.proxyHost", getCurrentProxy().split(":")[0]);
		System.setProperty("http.proxyPort", getCurrentProxy().split(":")[1]);
		return getCurrentProxy();
	}
	
	public static synchronized String getCurrentProxy(){
		if(currentPorxy < 0){
			return "Please invoke changePorxy() first!";
		}
		return proxyList.get(currentPorxy);
	}
	
	public static synchronized String removeCurrentProxy(){
		if(currentPorxy >= 0 && currentPorxy < proxyList.size()){
			proxyList.remove(currentPorxy);
			return changeProxy();
		}
		return "ERROR";
	}

}
