package com.jeiel.tool;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jeiel.mailutls.MailSender;


public class AutoDownloaderMultiThread {
	
	public static final String URL = "http://sys.hibor.com.cn//center/maibo/qikanzuixin.asp?page=";
	public static final String params = "&abc="+Account.getCurrentAccount()+"&def=&F_F=13";//F_F=13显示最新股票
	public static final String ROOT_DIR_PATH = "d://DailyPDFMultiThread";
	public static final String TARGET_DIR_PATH = "d://WBWJ_YBDOWNLOAD";
	public static final int MAX_THREAD_AMOUT = 20;
	private static String nextWorkDate = null;
	private static String beginTime = "";
	private static List<PDF> pdfs = new ArrayList<PDF>();
	

	static{
		File rootDir = new File(ROOT_DIR_PATH);
		if(!rootDir.exists()){
			rootDir.mkdirs();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(!isWorkDate()) return;
		Log.log("Start");
		Log.log("Initializing...");
		Log.log("Root Directory: " + ROOT_DIR_PATH);
		getNextWorkDate();
		checkFileExist();
		initDownloadList();
		
			
		ExtractPDFURL();
		generateFileName();
		
		
		startWork();
		checkDownloadedFile();
		record();
		copyReportsToTargetDir();
		Log.log("Done");
		if(MailSender.remind(nextWorkDate)){
			Log.log("Succeed!");
		}else{
			Log.log("Failed!");
		}
		
		Log.closeLogFile();
		Log.copyTo(ROOT_DIR_PATH, nextWorkDate + ".log");
	}
	
	public static boolean isWorkDate(){
		boolean isWorkDate = true;
		Calendar calendar = Calendar.getInstance();
		isWorkDate = !isHoliday(calendar);
		return isWorkDate;
	}
	
	public static void getNextWorkDate(){
		if(nextWorkDate == null || nextWorkDate.equals("")){
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			Calendar calendar = Calendar.getInstance();
			do{
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}while(isHoliday(calendar));
			nextWorkDate = format.format(calendar.getTime());
		}
		Log.log("Next Work Date: " + nextWorkDate);
	}
	
	public static void checkFileExist(){
		File file = new File(ROOT_DIR_PATH + "/" + nextWorkDate);
		if(file.exists()){
			if(file.listFiles() != null){
				for(File f:file.listFiles()){
					f.delete();
				}
			}
			file.delete();
		}
		file = new File(ROOT_DIR_PATH + "/" + nextWorkDate + ".xls");
		if(file.exists()){
			file.delete();
		}
	}
	
	public static boolean isHoliday(Calendar calendar){
		if(calendar.get(Calendar.DAY_OF_WEEK) == 1 || calendar.get(Calendar.DAY_OF_WEEK) == 7){//sun sat
			return true;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		for(int i = 0; i < Holiday.HODILAY.length/2; i++){
			if(format.format(calendar.getTime()).compareTo(Holiday.HODILAY[i][0]) >= 0 &&
					format.format(calendar.getTime()).compareTo(Holiday.HODILAY[i][1]) <= 0){
				return true;
			}
		}
		return false;
	}
	
	public static void initDownloadList(){//
		pdfs.clear();
		checkRecords();
		Elements elements = null;
		elements = getEveryRow();//every row
		
		String baseUrl = "http://sys.hibor.com.cn/center/maibo/";
		for(Element e : elements){
			Elements tds = e.getElementsByTag("td");
			if(tds.get(1).getElementsByTag("a").get(0).attr("title").split("-")[2].startsWith("600")){
				continue;
			}
			PDF pdf = new PDF();
			pdf.setName(tds.get(1).getElementsByTag("a").get(0).attr("title"));
			pdf.setTime(completeDate(tds.get(5).text()));
			pdf.setUrl(baseUrl + tds.get(6).getElementsByTag("a").get(0).attr("href"));
			pdfs.add(pdf);
		}
		Log.log("File amount: " + pdfs.size());
	}
	
	public static void checkRecords(){//
		File dir = new File(ROOT_DIR_PATH);
		if(dir.exists()){//
			if(dir.listFiles() != null && dir.listFiles().length > 0){
				File file = null;
				HSSFSheet sheet = null;
				HSSFRow row = null;
				File[] files = dir.listFiles();
				for(int i = files.length - 1; i >= 0 ; i--){
					file = files[i];
					if(file.isFile() && file.getName().matches("^[0-9]{8}\\.xls$")){
						try(FileInputStream fis = new FileInputStream(file);
								HSSFWorkbook book = new HSSFWorkbook(fis);) {
							sheet = book.getSheet("records");
							if(sheet.getLastRowNum() >= 1){
								row = sheet.getRow(1);
								beginTime = completeDate(row.getCell(1).getStringCellValue());
								Log.log("beginTime: " + beginTime);
							}
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							Log.log(e.getMessage());
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Log.log(e.getMessage());
							e.printStackTrace();
						}
						break;
					}
				}
			}
		}
	}
	
	public static Document getDocument(String url, int timeoutMS){
		while(true){
			try{
				if(Proxy.changeProxy().equals(Proxy.NOPROXYAVALIABLE)){
					Log.log(Proxy.NOPROXYAVALIABLE);
					Log.log("Force quit!");
					System.exit(-1);
				}
				if(Account.changeAccount().equals(Account.NOACCOUNTAVALIABLE)){
					Log.log(Account.NOACCOUNTAVALIABLE);
					Log.log("Force quit!");
					System.exit(-1);
				}
				
				Connection conn = Jsoup.connect(url.replaceAll("abc=[0-9a-zA-Z]+", "abc=" + Account.getCurrentAccount()));
				Document doc = conn.timeout(timeoutMS > 0 ? timeoutMS : 10000).get();
				if(doc.text().contains("浏览上限")){
					Log.log("Account: " + Account.getCurrentAccount() + "\t overused!");
					if(Account.removeCurrentAccount().equals(Account.NOACCOUNTAVALIABLE)){
						Log.log(Account.NOACCOUNTAVALIABLE);
						Log.log("Force quit!");
						System.exit(-1);
					}else{
						continue;
					}
				}else if(doc.text().contains("禁止访问")){
					Log.log("Proxy: " + Proxy.getCurrentProxy() + "\t disabled!");
					if(Proxy.removeCurrentProxy().equals(Proxy.NOPROXYAVALIABLE)){
						Log.log(Proxy.NOPROXYAVALIABLE);
						Log.log("Force quit!");
						System.exit(-1);
					}
				}
				return doc;
			}catch(Exception e){
				if(e.getMessage().contains("403")){
					Proxy.changeProxy();
				}
				e.printStackTrace();
			}
		}
	}
	
	public static Elements getEveryRow(){//get every row in web
		String url = null;
		Elements elements = new Elements();

		if(beginTime == null || beginTime.equals("")){//
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			beginTime = format.format(new Date()) + " 00:00:00";
			Log.log("beginTime: " + beginTime);
		}
		int page = 1;
		while(true){
			url = URL + page + params;
			Document doc = getDocument(url, 60000);
			Elements tmps = doc.select("div.classbaogao_sousuo_ulresult tr");
			for(Element e : tmps){
				if(completeDate(e.select("td").get(5).text()).compareTo(beginTime) <= 0){
					return elements;
				}
				elements.add(e);
			}
			page++;
		}
	}
	
	public static String completeDate(String formerDate){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String latterDate = "";
		try {
			latterDate = format.format(format.parse(formerDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Log.log(e.getMessage());
			e.printStackTrace();
		}
		//Log.log(formerDate + "\t" + latterDate);
		return latterDate;
	}
	
	public static void generateFileName(){
		Log.log("Generating file name...");
		for(PDF pdf : pdfs){
			String date = pdf.getTime().split(" ")[0].replace("-", "");
			String name = pdf.getName().substring(0, pdf.getName().lastIndexOf("-")).replace("*", "")
					.replace("\\", "").replace("/", "").replace("<", "").replace(">", "")
					.replace("?", "").replace("|", "").replace("\"", "").replace(":", "");
			String suffix = pdf.getUrl().substring(pdf.getUrl().lastIndexOf("."));
			pdf.setFileName(date + "-" + name + suffix);
			Log.log((pdfs.indexOf(pdf) + 1) + " " + pdf.getFileName());
		}
	}
	
	public static void ExtractPDFURL(){
		Log.log("Extracting PDF URL...");

		for(PDF pdf : pdfs){
			Document doc = null;
			Log.log((pdfs.indexOf(pdf) + 1) + " " + pdf.getName() + " " + pdf.getUrl());
			while(true){
				doc = getDocument(pdf.getUrl(), 60000);
				if(doc.select("iframe[src$=.pdf], iframe[src$=.doc], iframe[src$=.docx]").size()>0){
					break;
				}
			}
			pdf.setUrl(doc.select("iframe[src$=.pdf], iframe[src$=.doc], iframe[src$=.docx]").get(0).attr("src"));
			Log.log((pdfs.indexOf(pdf) + 1) + " " + pdf.getName() + " " + pdf.getUrl());
		}
		
	}
	
	public static void startWork(){
		Log.log("Downloading...");
		ExecutorService pool = Executors.newCachedThreadPool();
		for(int i = 0; i < MAX_THREAD_AMOUT; i++){
			pool.execute(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub;
					Log.log(Thread.currentThread().getName() + " start");
					while(hasNextUnhandledPDF()){
						download(nextUnhandledPDF());
					}
				}
			});
		}
		pool.shutdown();
		try {
			pool.awaitTermination(30, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void download(PDF pdf){
		if(pdf == null){
			return;
		}
		OutputStream os = null;
		InputStream is = null;
		File dir = new File(ROOT_DIR_PATH + "/" +nextWorkDate);
		if(!dir.exists()){
			dir.mkdirs();
		}
		File file = null;
		try {
			URL url = new URL(pdf.getUrl()); 
			Proxy.changeProxy();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10000);;
			conn.setReadTimeout(60000);
			file = new File(dir, pdf.getFileName());
			if(!file.exists()){
				file.createNewFile();
			}
			os = new FileOutputStream(file);
			is = conn.getInputStream();
			byte[] bytes = new byte[10240];
			int len = 0;
			int sum = 0;
			while((len = is.read(bytes)) > 0){
				sum += len;
				os.write(bytes, 0, len);
			}
			if(sum == file.length()){
				pdf.setDownloaded(true);
				Log.log("Downloaded: " + (pdfs.indexOf(pdf) + 1) + "\t" + 
						pdf.getName().substring(0, pdf.getName().indexOf("-", pdf.getName().indexOf("-", pdf.getName().indexOf("-") + 1) + 1)) + "   \t" + 
						pdf.getTime());
			}else{
				pdf.setDistributed(false);
				pdf.setDownloaded(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			pdf.setDistributed(false);
			pdf.setDownloaded(false);
			Log.log(e.getMessage());
			e.printStackTrace();
		}finally{
			try {
				if(os != null){
					os.close();
				}
				if(is != null){
					is.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.log(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static boolean hasNextUnhandledPDF(){
		for(PDF pdf:pdfs){
			if(!pdf.isDownloaded()){
				return true;
			}
		}
		return false;
	}
	
	public static PDF nextUnhandledPDF(){
		for(PDF pdf:pdfs){
			if(!pdf.isDistributed()&&!pdf.isDownloaded()){
				pdf.setDistributed(true);
				return pdf;
			}
		}
		return null;
	}
	
	public static void checkDownloadedFile(){
		Log.log("Checking downloaded files");
		File dir = new File(ROOT_DIR_PATH + "/" +nextWorkDate);
		int missed = 0;
		for(PDF pdf : pdfs){
			File file = new File(dir, pdf.getFileName());
			if(!file.exists()){
				missed++;
				Log.log("Cannot found: " + pdf.getFileName());
			}
		}
		Log.log("Missed Files count: " + missed);
		int repeated = 0;
		for(int i = 0; i < pdfs.size() - 1; i++){
			for(int j = i + 1; j < pdfs.size(); j++){
				if(pdfs.get(i).getFileName().equals(pdfs.get(j).getFileName())){
					repeated++;
					Log.log("Repeated Files' index: " + i + " & " + j);
					Log.log("File name: " + pdfs.get(i).getFileName());
					Log.log("URLs: ");
					Log.log("\t" + pdfs.get(i).getUrl());
					Log.log("\t" + pdfs.get(j).getUrl());
				}
			}
		}
		Log.log("Repeated Files count: " + repeated);
	}
	
	public static void record(){//record last pdf info,data format: time:name:url
		Log.log("Recording...");
		File dir = new File(ROOT_DIR_PATH);
		if(!dir.exists()){
			dir.mkdirs();
		}
		
		String fileName = nextWorkDate + ".xls";
		File file = new File(dir, fileName);
		FileOutputStream fos = null;
		HSSFWorkbook book = new HSSFWorkbook();
		HSSFSheet sheet = book.createSheet("records");
		HSSFRow row = sheet.createRow(0);
		row.createCell(0).setCellValue("Name");
		row.createCell(1).setCellValue("Time");
		row.createCell(2).setCellValue("Filename");
		row.createCell(3).setCellValue("Url");
		int i = 1;
		for(PDF pdf : pdfs){
			row = sheet.createRow(i++);
			row.createCell(0).setCellValue(pdf.getName());
			row.createCell(1).setCellValue(pdf.getTime());
			row.createCell(2).setCellValue(pdf.getFileName());
			row.createCell(3).setCellValue(pdf.getUrl());
		}
		try {
			if(!file.exists()){
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			book.write(fos);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.log(e.getMessage());
			e.printStackTrace();
		}finally{
			try {
				if(fos != null){
					fos.close();
				}
				if(book != null){
					book.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.log(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void copyReportsToTargetDir(){
		Log.log("Copying to target directory: " + TARGET_DIR_PATH + "/" +nextWorkDate);
		File targetDir = new File(TARGET_DIR_PATH + "/" + nextWorkDate);
		if(!targetDir.exists()){
			targetDir.mkdirs();
		}else{
			if(targetDir.listFiles() != null && targetDir.listFiles().length > 0){
				Log.log("Target directory already exists and is not empty!!!");
				Log.log("Mission canceled!");
				return;
			}
		}
		File originalDir = new File(ROOT_DIR_PATH + "/" + nextWorkDate);
		if(!originalDir.exists() || originalDir.listFiles() == null){
			return;
		}
		FileInputStream fis = null;
		FileOutputStream fos = null;
		File newFile = null;
		byte[] bytes = new byte[10240];
		int len = 0;
		for(File originalFile : originalDir.listFiles()){
			if(originalFile.isFile()){
				try {
					newFile = new File(targetDir, originalFile.getName());
					if(!newFile.exists()){
						newFile.createNewFile();
					}
					fis = new FileInputStream(originalFile);
					fos = new FileOutputStream(newFile);
					while((len = fis.read(bytes)) > 0){
						fos.write(bytes, 0, len);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					try {
						if(fis != null) fis.close();
						if(fos != null) fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}
