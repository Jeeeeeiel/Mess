package com.jeiel.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jeiel.tool.Log;

public class Holiday {

	
	public static List<String> holidayList = new ArrayList<String>();
	
	static{
		Log.log("Initializing holiday...");
		Properties props = new Properties();
		try {
		Log.log("Adding holiday...");
			props.loadFromXML(new FileInputStream(new File(Config.HOLIDAY_INFO_PATH)));
			if(props.containsKey("holiday")){
				
				for(String p : props.getProperty("holiday").split(";")){
					if(p.replaceAll("[\\s]*", "").length()>1){
						holidayList.add(p.trim().replaceAll("[\\s]*", ""));
					}
				}
			}
			Log.log("Holiday amount: " + holidayList.size());
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
}
