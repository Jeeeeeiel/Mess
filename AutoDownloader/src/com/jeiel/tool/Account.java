package com.jeiel.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Account {
	public static String PROPERTIES_PATH = AutoDownloaderMultiThread.ROOT_DIR_PATH + "//InitArgs.xml";
	private static int currentAccount = -1;
	public static final String NOACCOUNTAVALIABLE = "No account avaliable!" ;

	private static List<String> accountList = new ArrayList<String>();
	
	static{
		Log.log("Initializing account...");
		Properties props = new Properties();
		try {
			props.loadFromXML(new FileInputStream(new File(PROPERTIES_PATH)));
			if(props.containsKey("account")){
				for(String p : props.getProperty("account").split(";")){
					if(p.trim().length()>0){
						Log.log("Adding account: " + p.trim());
						accountList.add(p.trim().split(":")[0]);//remove account name
					}
				}
			}
			System.out.println("Account amount: " + accountList.size());
			if(Account.changeAccount().equals(Account.NOACCOUNTAVALIABLE)){
				Log.log(Account.NOACCOUNTAVALIABLE);
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
	
	public static synchronized String getCurrentAccount(){
		if(currentAccount < 0){
			return "Please invoke changeAccount() first!";
		}
		return accountList.get(currentAccount);
	}
	
	public static synchronized String changeAccount(){
		if(accountList.size()==0){
			return NOACCOUNTAVALIABLE;
		}
		currentAccount = (currentAccount + 1) % accountList.size();
		Log.log("Use account: " + getCurrentAccount());
		return getCurrentAccount();
	}
	
	public static synchronized String removeCurrentAccount(){
		if(currentAccount >= 0 && currentAccount < accountList.size()){
			accountList.remove(currentAccount);
			return changeAccount();
		}
		return "ERROR";
	}
}
//a 8V aX bW 9U
//b aU 9X
//d 9Z
//m hY mX
//i lY
//n mU
//s vY