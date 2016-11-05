package com.jeiel.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import com.jeiel.bean.Configuration;
import com.jeiel.bean.Drama;
import com.jeiel.mailutls.MailSender;

public class Log{
	private static ByteArrayOutputStream baos = new ByteArrayOutputStream();
	public static PrintStream customStream = new PrintStream(baos);
	private static StringBuilder sb = new StringBuilder();
	public static StringBuilder getSb() {
		return sb;
	}
	public static void log(String msg){
		sb.append(msg + "\n");
		System.out.println(msg);
	}
	public static void logWithoutNewline(String msg){
		sb.append(msg);
		System.out.print(msg);
	}
	public static void flush(){
		sb.append(baos.toString() + "\n");
		System.out.println(baos.toString());
		baos.reset();
	}
	public static void sendLog(boolean send){
		log("");
		log("");
		log("");
		log("");
		if(!Configuration.getUsername().isEmpty()&&
				!Configuration.getPassword().isEmpty()&&
				!Configuration.getRecipients().isEmpty()&&
				send){
			MailSender.send();
		}
		close();
	}
	private static void close(){
		if(customStream != null){
			customStream.close();
		}
		if(baos != null){
			try {
				baos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void record(String tag, List<Drama>dramas){
		log(tag + ":");
		for(Drama drama:dramas){
			log(drama.getName() + " " + drama.getSe() + " " + drama.getUrl());
		}
	}
}