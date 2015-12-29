package com.jeiel.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jeiel.tool.Log;

public class Proxy {
	private static int currentPorxy = -1;
	public static final String NOPROXYAVALIABLE = "No proxies avaliable!" ;
	public static final String PROXYAVALIABLE = "Proxies avaliable!" ;
	private static List<String> proxyList = new ArrayList<String>();
	
	static{
		Log.log("Initializing proxies...");
		Properties props = new Properties();
		try {
			Log.log("Adding proxies...");
			props.loadFromXML(new FileInputStream(new File(Config.EXTERNAL_ARGS_PATH)));
			if(props.containsKey("proxy")){
				for(String p : props.getProperty("proxy").split(";")){
					if(p.trim().length()>0){
						proxyList.add(p.trim());
					}
				}
			}
			Log.log("Proxies amount: " + proxyList.size());
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
		//Log.log("Switch proxy: " + getCurrentProxy());
		//Global proxy.It doesn't work if you set proxy locally.
		System.setProperty("http.proxyHost", getCurrentProxy().split(":")[0]);
		System.setProperty("http.proxyPort", getCurrentProxy().split(":")[1]);
		return getCurrentProxy();
	}
	
	private static String getCurrentProxy(){
		return proxyList.get(currentPorxy);
	}
	
	public static synchronized String removeCurrentProxy(String proxy){
		proxyList.remove(proxy);
		Log.log("Remove proxy: " + proxy + ". " + proxyList.size() + " proxies left.");
		currentPorxy = currentPorxy % proxyList.size();
		if(proxyList.size()==0){
			return NOPROXYAVALIABLE;
		}else{
			return PROXYAVALIABLE;
		}
	}

}
