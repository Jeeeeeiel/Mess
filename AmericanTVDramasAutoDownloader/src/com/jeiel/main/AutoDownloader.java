package com.jeiel.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jeiel.bean.Configuration;
import com.jeiel.bean.Drama;

public class AutoDownloader {
	private static final String ROOT_URL = "http://www.ttmeiju.com/weekcalendar.html";
	private static final String SEFORMATE = "[sS]{1}((0[1-9]+)|([1-9]{1}[0-9]*))[eE]{1}((0[1-9]+)|([1-9]{1}[0-9]*))[\\s]*720p";
	public static void main(String[] args) {
		Log.log(new Date().toString());
		List<Drama>dramas = new ArrayList<Drama>();
		for(String name:Configuration.getDramasNameList()){
			Drama drama = new Drama();
			drama.setName(name);
			dramas.add(drama);
		}
		Log.record("Start", dramas);
		getRecords(dramas);
		Log.record("After getRecords", dramas);
		searchDramasResources(dramas);
		Log.record("After searchDramasResources", dramas);
		getLatestSELink(dramas);
		Log.record("After getLatestSELink", dramas);
		if(!dramas.isEmpty()){
			download(dramas, Configuration.getSavePath(), Configuration.getDownloader());
			exportExcel(dramas);
			Log.sendLog(true);
		}else{
			Log.log("No update!");
			Log.sendLog(false);
		}
	}
	public static void download(List<Drama> dramas, String savePath, String downloader){
		Log.log("Dramas updated!");
		for(Drama drama:dramas){
			Log.log((1 + dramas.indexOf(drama)) + "." + drama.getName() + "\t" + drama.getSe() + "\tUpdated!");
			try{
				Process p =Runtime.getRuntime().exec(new String[]{
						"ruby",
						Configuration.RUBY_FILE,
						Configuration.getSavePath(),
						drama.getUrl()
				});
				System.out.println("new String[]{"+
						"\"ruby\","+
						"\""+Configuration.RUBY_FILE+"\","+
						"\""+Configuration.getSavePath()+"\","+
						"\""+drama.getUrl()+"\""+
				"}");
				InputStream is = p.getInputStream();
				byte[] b = new byte[1024];
				int len = 0;
				StringBuilder sb = new StringBuilder();
				while((len = is.read(b))>0){
					sb.append(new String(b, 0, len));
				}
				if(sb.length()>0){
					Log.log("    Normal output:" + sb.toString());
				}
				is = p.getErrorStream();
				b = new byte[1024];
				len = 0;
				sb.setLength(0);
				while((len = is.read(b))>0){
					sb.append(new String(b, 0, len));
				}
				if(sb.length()>0){
					Log.log("    Error output:" + sb.toString());
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	public static void searchDramasResources(List<Drama>dramas){//get dramas link
		Log.log("Searching dramas resouces...");
		Document doc = WebUtils.getDocument(ROOT_URL, WebUtils.METHOD_GET, 5*1000);
		Elements es = doc.select("table.seedtable a");
		for(Element e:es){
			for(Drama drama:dramas){
				if(e.text().toLowerCase().contains(drama.getName().toLowerCase())){
					drama.setUrl(e.attr("abs:href"));
					break;
				}
			}
		}
	}
	public static boolean isDownloaded(Drama drama, String SE){//compare SE
		int oldS = Integer.parseInt(drama.getSe().replaceAll("s|S", "").replaceAll("[eE]{1}[0-9]*", "").trim());
		int oldE = Integer.parseInt(drama.getSe().replaceAll("[sS]{1}[0-9]*", "").replaceAll("e|E", "").trim());
		int newS = Integer.parseInt(SE.replaceAll("s|S", "").replaceAll("[eE]{1}[0-9]*", "").trim());
		int newE = Integer.parseInt(SE.replaceAll("[sS]{1}[0-9]*", "").replaceAll("e|E", "").trim());

		if(oldS<newS)return false;
		else if(oldS==newS&&oldE<newE)return false;
		return true;
	}
	public static void getLatestSELink(List<Drama>dramas){//get lastest SE link
		Log.log("Getting latest SE infomation...");
		Iterator<Drama>iterator=dramas.iterator();
		while(iterator.hasNext()){
			Drama drama=iterator.next();
			if(drama.getUrl().isEmpty()){
				iterator.remove();
				continue;
			}
			Document doc = WebUtils.getDocument(drama.getUrl(), WebUtils.METHOD_GET, 5*1000);
			drama.setUrl("");
			if(doc.select("tr.Scontent").size()>0){
				for(Element e:doc.select("tr.Scontent")){
					Pattern p = Pattern.compile(SEFORMATE);
					Matcher m = p.matcher(e.select("td").get(1).text());
					if(m.find()){
						String SE = m.group().replace("720p", "").trim();
						if(!isDownloaded(drama, SE)){
							/*if(e.select("td").get(2).select("a[href^=magnet], a[href^=ed2k]").size()>0){
								drama.setUrl(e.select("td").get(2).select("a[href^=magnet], a[href^=ed2k]").get(0).attr("href"));
							}*/
							if(e.select("td").get(2).select("a[href^=magnet]").size()>0){
								drama.setSe(SE);
								drama.setUrl(e.select("td").get(2).select("a[href^=magnet]").get(0).attr("href"));
								break;
							}else if(e.select("td").get(2).select("a[href$=.torrent]").size()>0){
								drama.setSe(SE);
								drama.setUrl(e.select("td").get(2).select("a[href$=.torrent]").get(0).attr("href"));
								break;
							}/*else if(e.select("td").get(2).select("a[href^=http://pan.baidu.com]").size()>0){
								drama.setUrl(e.select("td").get(2).select("a[href^=http://pan.baidu.com]").get(0).attr("href"));
							}*/
						}
					}
				}
				if(drama.getUrl().isEmpty()){
					iterator.remove();
				}
			}
		}
	}
	public static void getRecords(List<Drama>dramas){//get SE history info
		File file = new File(Configuration.RECORDS_FILE);
		for(Drama drama:dramas){
			drama.setSe("S0E0");
		}
		if(file.exists()){
			try (FileInputStream fis = new FileInputStream(Configuration.RECORDS_FILE)){
				HSSFWorkbook book = new HSSFWorkbook(fis);
				HSSFSheet sheet = book.getSheetAt(0);
				HSSFRow row = null;
				for(int i = 1; i<=sheet.getLastRowNum(); i++){
					row = sheet.getRow(i);
					for(Drama drama:dramas){
						if(drama.getName().equals(row.getCell(0).getStringCellValue())){
							drama.setSe(row.getCell(1).getStringCellValue());
							break;
						}
					}
				}
				book.close();
				return;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
	public static void exportExcel(List<Drama>dramas){
		File file = new File(Configuration.RECORDS_FILE);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try (FileOutputStream fos = new FileOutputStream(Configuration.RECORDS_FILE)){
				HSSFWorkbook book = new HSSFWorkbook();
				HSSFSheet sheet = book.createSheet();
				HSSFRow row = sheet.createRow(0);
				row.createCell(0).setCellValue("Name");
				row.createCell(1).setCellValue("SE");
				row.createCell(2).setCellValue("Url");
				for(Drama drama:dramas){
					row = sheet.createRow(sheet.getLastRowNum()+1);
					row.createCell(0).setCellValue(drama.getName());
					row.createCell(1).setCellValue(drama.getSe());
					row.createCell(2).setCellValue(drama.getUrl());
					markRow(book, row);
				}
				book.write(fos);
				book.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}else{
			HSSFWorkbook book = null;
			try (FileInputStream fis = new FileInputStream(Configuration.RECORDS_FILE)){
				book = new HSSFWorkbook(fis);
				HSSFSheet sheet = book.getSheetAt(0);
				HSSFRow row = null;
				for(int i = 1; i<=sheet.getLastRowNum(); i++){
					row = sheet.getRow(i);
					Iterator<Drama>iterator = dramas.iterator();
					unmarkRow(book, row);
					while(iterator.hasNext()){
						Drama drama = iterator.next();
						if(drama.getName().equals(row.getCell(0).getStringCellValue())){
							row.getCell(1).setCellValue(drama.getSe());
							row.getCell(2).setCellValue(drama.getUrl());
							markRow(book, row);
							iterator.remove();
							break;
						}
					}
				}
				for(Drama drama:dramas){
					row = sheet.createRow(sheet.getLastRowNum()+1);
					row.createCell(0).setCellValue(drama.getName());
					row.createCell(1).setCellValue(drama.getSe());
					row.createCell(2).setCellValue(drama.getUrl());
					markRow(book, row);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try(FileOutputStream fos = new FileOutputStream(Configuration.RECORDS_FILE)){
				book.write(fos);
				book.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void markRow(HSSFWorkbook book, HSSFRow row){
		if(book!=null&&row!=null){
			HSSFFont markedFont = book.createFont();
			markedFont.setColor(HSSFColor.RED.index);
			markedFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			HSSFCellStyle markedCellStyle = book.createCellStyle();
			markedCellStyle.setFont(markedFont);
			for(int i=0;i<row.getLastCellNum();i++){
				row.getCell(i).setCellStyle(markedCellStyle);
			}
		}
	}
	public static void unmarkRow(HSSFWorkbook book, HSSFRow row){
		if(book!=null&&row!=null){
			HSSFFont normalFont = book.createFont();
			normalFont.setColor(HSSFColor.BLACK.index);
			normalFont.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
			HSSFCellStyle normalCellStyle = book.createCellStyle();
			normalCellStyle.setFont(normalFont);
			for(int i=0;i<row.getLastCellNum();i++){
				row.getCell(i).setCellStyle(normalCellStyle);
			}
		}
	}
}
