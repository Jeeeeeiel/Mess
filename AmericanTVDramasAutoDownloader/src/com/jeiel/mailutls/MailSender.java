package com.jeiel.mailutls;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;

import com.jeiel.bean.Configuration;
import com.jeiel.main.Log;

public class MailSender {
	
	private static final Properties props = System.getProperties();

	private static MailAuthenticator authenticator;
	
	private static Session session;

	public static void main(String[] args) throws AddressException, MessagingException, FileNotFoundException, IOException {
		//remind("20151220");
	}
	
	public static boolean send(){
		if(!init()){
			Log.log("Init failed!");
			return false;
		}
		List<String> recipientsList = new ArrayList<String>();
		for(String recipient : Configuration.getRecipients()){
			if(!recipient.isEmpty()){
				recipientsList.add(recipient);
			}
		}
		if(recipientsList.isEmpty()){
			Log.log("No recipients!");
			return false;
		}
		try {
			String msg = Log.getSb().toString();
			msg = msg.substring(msg.indexOf("Dramas updated!"));
			msg = msg.replace("\n", "<br>");
			send(recipientsList, "Dramas updated!", msg, Configuration.RECORDS_FILE);
			return true;
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}

	private static boolean init(){
		if(Configuration.getUsername().isEmpty()||Configuration.getPassword().isEmpty())return false;
		if(!Configuration.getSmtpHostName().isEmpty()){
			props.put("mail.smtp.host", Configuration.getSmtpHostName());
		}else{
			props.put("mail.smtp.host", "smtp." + Configuration.getUsername().split("@")[1]);
		}
		if(!Configuration.getPort().isEmpty()){
			props.put("mail.smtp.port", Configuration.getPort());
		}
		props.put("mail.smtp.auth", "true");
		authenticator = new MailAuthenticator(Configuration.getUsername(), Configuration.getPassword());
		session = Session.getInstance(props, authenticator);
	
		return true;
	}
	
	private static void send(List<String> recipients, String subject, String content, String filename) throws AddressException, MessagingException{
		//Log.log("Sending mail...");
		final MimeMessage message = new MimeMessage(session);
		
		message.setFrom(new InternetAddress(authenticator.getUsername()));
		final int num = recipients.size();
		InternetAddress[] addresses = new InternetAddress[num];
		for(int i = 0; i < num; i++){
			addresses[i] = new InternetAddress(recipients.get(i));
		}
		
		Multipart mp = new MimeMultipart();
		MimeBodyPart mbp = new MimeBodyPart();
		mbp.setContent(content, "text/html;charset=utf-8");
		mp.addBodyPart(mbp);
		
		if(Configuration.getSendRecords().equals("true")&&filename!=null&&!filename.isEmpty()){
			mbp = new MimeBodyPart();
			mbp.setFileName(filename.replaceAll("[\\s\\S]*(\\\\|/)", ""));
			mbp.setDataHandler(new DataHandler(new FileDataSource(filename)));
			mp.addBodyPart(mbp);
		}
		
		
		message.setRecipients(RecipientType.TO, addresses);
		message.setSubject(subject);
		message.setContent(mp);
		Transport.send(message);
	}
	
}
