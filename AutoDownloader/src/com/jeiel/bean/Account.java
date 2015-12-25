package com.jeiel.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.jeiel.tool.Log;

public class Account {
	private static int currentAccount = -1;
	public static final String NOACCOUNTAVALIABLE = "No accounts avaliable!";
	public static final String ACCOUNTAVALIABLE = "Accounts avaliable!";
	private static List<String> accountList = new ArrayList<String>();
	
	static{
		Log.log("Initializing accounts...");
		Properties props = new Properties();
		try {
			Log.log("Adding accounts...");
			props.loadFromXML(new FileInputStream(new File(Config.EXTERNAL_ARGS_PATH)));
			if(props.containsKey("account")){
				for(String p : props.getProperty("account").split(";")){
					if(p.trim().length()>0){
						accountList.add(p.trim().split(":")[0]);//remove account name
					}
				}
			}
			Log.log("Accounts amount: " + accountList.size());
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
	
	private static String getCurrentAccount(){
		return accountList.get(currentAccount);
	}
	
	public static synchronized String changeAccount(){
		if(accountList.size()==0){
			return NOACCOUNTAVALIABLE;
		}
		currentAccount = (currentAccount + 1) % accountList.size();
		Log.log("Switch account: " + getCurrentAccount());
		return getCurrentAccount();
	}
	
	public static synchronized String removeCurrentAccount(String account){
		accountList.remove(account);
		Log.log("Remove account: " + account + ". " + accountList.size() + " accounts left.");
		currentAccount = currentAccount % accountList.size();
		if(accountList.size()==0){
			return NOACCOUNTAVALIABLE;
		}else{
			return ACCOUNTAVALIABLE;
		}
	}

}
//a 8V aX bW 9U
//b aU 9X
//d 9Z
//m hY mX
//i lY
//n mU
//s vY