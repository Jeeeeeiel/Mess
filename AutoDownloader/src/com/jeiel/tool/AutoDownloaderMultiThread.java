package com.jeiel.tool;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jeiel.bean.Account;
import com.jeiel.bean.Config;
import com.jeiel.bean.Holiday;
import com.jeiel.bean.PDF;
import com.jeiel.bean.Proxy;
import com.jeiel.mailutls.MailSender;


public class AutoDownloaderMultiThread {
	
	//private static final String URL = "http://sys.hibor.com.cn//center/maibo/qikanzuixin.asp?";
	//private static final String params = "&abc=tmp&def=&F_F=13";//F_F=13显示最新股票
	private static String URL = "http://sys.hibor.com.cn//gongsipingji/index/PingJiYanJiu.aspx";
	private static String params = "pagenum={page}&pagego={page}&lbc=最新&abc={account}&def=&vidd=5&xyz=oRwPvMsRrQoR&keyy=TYUGUIYUI&value=one";
	private static final int MAX_DOWNLOAD_THREAD_AMOUMT;//download thread
	private static final int MAX_EXTRACT_THREAD_AMOUNT;//extract url thread
	private static final int MAX_DEFAULT_THREAD_AMOUNT = 40;//default thread size
	private static String nextWorkDate = null;
	private static String beginTime = "";
	private static List<PDF> pdfs = new ArrayList<PDF>();
	
	static{//Initionalize thread amount
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(new File(Config.PROPERTIES_PATH)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(props != null && props.containsKey("MAX_DOWNLOAD_THREAD_AMOUNT")){
			String tmp = props.getProperty("MAX_DOWNLOAD_THREAD_AMOUNT").trim();
			if(tmp.matches("^[1-9]{1}[0-9]{0,1}$")){
				MAX_DOWNLOAD_THREAD_AMOUMT = Integer.parseInt(tmp);
			}else{
				MAX_DOWNLOAD_THREAD_AMOUMT = MAX_DEFAULT_THREAD_AMOUNT;
			}
		}else{
			MAX_DOWNLOAD_THREAD_AMOUMT = MAX_DEFAULT_THREAD_AMOUNT;
		}
		if(props != null && props.containsKey("MAX_EXTRACT_THREAD_AMOUNT")){
			String tmp = props.getProperty("MAX_DOWNLOAD_THREAD_AMOUNT").trim();
			if(tmp.matches("^[1-9]{1}[0-9]{0,1}$")){
				MAX_EXTRACT_THREAD_AMOUNT = Integer.parseInt(tmp);
			}else{
				MAX_EXTRACT_THREAD_AMOUNT = MAX_DEFAULT_THREAD_AMOUNT;
			}
		}else{
			MAX_EXTRACT_THREAD_AMOUNT = MAX_DEFAULT_THREAD_AMOUNT;
		}
		if(props != null && props.containsKey("URL") && props.containsKey("params")){
			String tmp = props.getProperty("URL");
			URL = tmp.matches("^http[s]{0,1}://[\\S]+$")?tmp:URL;
			tmp = props.getProperty("params");
			params = tmp.matches("^[\\S]+\\{page\\}[\\S]+\\{account\\}[\\S]+$")?tmp:params;
			
		}
		
	}

	static{
		File rootDir = new File(Config.ROOT_DIR_PATH);
		if(!rootDir.exists()){
			rootDir.mkdirs();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(!isWorkDate()) return;
		Log.log("Start");
		Log.log("Initializing...");
		Log.log("Root Directory: " + Config.ROOT_DIR_PATH);
		Log.log("URL: "+URL);
		Log.log("params: "+params);
		Proxy.changeProxy();
		Account.changeAccount();
		
		getNextWorkDate();
		checkFileExist();
		initDownloadList();
		
			
		ExtractPDFURL();
		generateFileName();
		
		
		startWork();
		checkDownloadedFile();
		exportToExcel();
		copyReportsToTargetDir();
		Log.log("Done");
		if(MailSender.remind(nextWorkDate)){
			Log.log("Succeed!");
		}else{
			Log.log("Failed!");
		}
		
		Log.closeLogFile();
		Log.copyTo(Config.ROOT_DIR_PATH, nextWorkDate + ".log");
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
		File file = new File(Config.ROOT_DIR_PATH + "/" + nextWorkDate);
		if(file.exists()){
			if(file.listFiles() != null){
				for(File f:file.listFiles()){
					f.delete();
				}
			}
			file.delete();
		}
		file = new File(Config.ROOT_DIR_PATH + "/" + nextWorkDate + ".xls");
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
		
		for(Element e : elements){
			Elements tds = e.getElementsByTag("td");
			if(tds.get(1).attr("title").split("-")[2].startsWith("6")){
				continue;
			}
			PDF pdf = new PDF();
			pdf.setName(tds.get(1).attr("title"));
			pdf.setTime(completeDate(tds.get(5).text()));
			pdf.setUrl(tds.get(6).getElementsByTag("a").get(0).attr("abs:href"));
			pdfs.add(pdf);
		}
		Log.log("File amount: " + pdfs.size());
	}
	
	public static void checkRecords(){//
		File dir = new File(Config.ROOT_DIR_PATH);
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
							sheet = book.getSheetAt(0);
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
	
	public static Document getDocument(String url, int timeoutMS){//add local proxy settings to make sure use different proxy and account every time
		while(true){
			try{
				String proxy = Proxy.changeProxy();
				String account = Account.changeAccount();
				if(proxy.equals(Proxy.NOPROXYAVALIABLE)){
					forceQuit(Proxy.NOPROXYAVALIABLE);
				}
				if(account.equals(Account.NOACCOUNTAVALIABLE)){
					forceQuit(Account.NOACCOUNTAVALIABLE);
				}
				
				/*Connection conn = Jsoup.connect(url.replaceAll("abc=[0-9a-zA-Z]+", "abc=" + account));
				Document doc = conn.timeout(timeoutMS > 0 ? timeoutMS : 10000).get();*/
				
				SocketAddress addr = new InetSocketAddress(proxy.split(":")[0], Integer.parseInt(proxy.split(":")[1]));
				java.net.Proxy tp = new java.net.Proxy(java.net.Proxy.Type.HTTP, addr);
				URLConnection conn = new URL(url.replace("{account}", account)).openConnection(tp);
				conn.setConnectTimeout(timeoutMS > 0 ? timeoutMS : 10000);
				conn.setReadTimeout(timeoutMS > 0 ? timeoutMS : 10000);
				Document doc = Jsoup.parse(conn.getInputStream(), "utf-8", url);
				
				//System.out.println(conn.getURL().toString());
				
				if(doc.text().contains("浏览上限")){
					Log.log("Account: " + account + "\t overused!");
					if(Account.removeCurrentAccount(account).equals(Account.NOACCOUNTAVALIABLE)){
						forceQuit(Account.NOACCOUNTAVALIABLE);
					}else{
						continue;
					}
				}else if(doc.text().contains("禁止访问")){
					Log.log("Proxy: " + proxy + "\t disabled!");
					if(Proxy.removeCurrentProxy(proxy).equals(Proxy.NOPROXYAVALIABLE)){
						forceQuit(Proxy.NOPROXYAVALIABLE);
					}else{
						continue;
					}
				}
				return doc;
			}catch(Exception e){
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
			url = URL + "?" + params.replace("{page}", page+"");
			Log.log(url);
			Document doc = getDocument(url, 20000);
			Elements tmps = doc.select("#content tr.li_bg");
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
		ExecutorService pool = Executors.newCachedThreadPool();
		for(int i = 0; i < (MAX_EXTRACT_THREAD_AMOUNT <= pdfs.size() ? MAX_EXTRACT_THREAD_AMOUNT : pdfs.size()); i++){
			pool.execute(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub;
					Log.log(Thread.currentThread().getName() + " start");
					while(hasNextUnhandledPDF()){
						PDF pdf = nextUnhandledPDF();
						if(pdf == null){
							break;
						}
						Document doc = null;
						//Log.log((pdfs.indexOf(pdf) + 1) + " " + pdf.getName() + " " + pdf.getUrl());
						while(true){
							doc = getDocument(pdf.getUrl(), 20000);
							if(doc.select("iframe[src$=.pdf], iframe[src$=.doc], iframe[src$=.docx]").size()>0){
								break;
							}
						}
						pdf.setUrl(doc.select("iframe[src$=.pdf], iframe[src$=.doc], iframe[src$=.docx]").get(0).attr("src"));
						Log.log((pdfs.indexOf(pdf) + 1) + " " + pdf.getName() + " " + pdf.getUrl());
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
		for(PDF pdf : pdfs){
			pdf.setDistributed(false);
			pdf.setDownloaded(false);
		}
		
	}
	
	public static void startWork(){
		Log.log("Downloading...");
		ExecutorService pool = Executors.newCachedThreadPool();
		for(int i = 0; i < (MAX_DOWNLOAD_THREAD_AMOUMT <= pdfs.size() ? MAX_DOWNLOAD_THREAD_AMOUMT : pdfs.size()); i++){
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
		File dir = new File(Config.ROOT_DIR_PATH + "/" +nextWorkDate);
		if(!dir.exists()){
			dir.mkdirs();
		}
		File file = null;
		String proxy = Proxy.changeProxy();
		try {
			SocketAddress addr = new InetSocketAddress(proxy.split(":")[0], Integer.parseInt(proxy.split(":")[1]));
			java.net.Proxy tp = new java.net.Proxy(java.net.Proxy.Type.HTTP, addr);
			
			URL url = new URL(pdf.getUrl()); 
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(tp);
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			file = new File(dir, pdf.getFileName());
			if(!file.exists()){
				file.createNewFile();
			}
			os = new FileOutputStream(file);
			is = conn.getInputStream();
			byte[] bytes = new byte[10240];
			int len = 0;
			long sum = conn.getContentLengthLong();
			while((len = is.read(bytes)) > 0){
				os.write(bytes, 0, len);
			}
			if(sum == file.length()){
				pdf.setDownloaded(true);
				Log.log("Downloaded: " + (pdfs.indexOf(pdf) + 1) + "\t" + 
						pdf.getName().substring(0, pdf.getName().indexOf("-", pdf.getName().indexOf("-", pdf.getName().indexOf("-") + 1) + 1)) + "   \t" + 
						pdf.getTime());
			}else{
				Log.log("File damaged: " + pdf.getFileName() + ". Redownload later.");
				pdf.setDistributed(false);
				pdf.setDownloaded(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			pdf.setDistributed(false);
			pdf.setDownloaded(false);
			Log.log(e.getMessage());
			if(e.getMessage().contains("code: 403")){
				Log.log("Proxy: " + proxy + "\t disabled!");
				if(Proxy.removeCurrentProxy(proxy).equals(Proxy.NOPROXYAVALIABLE)){
					forceQuit(Proxy.NOPROXYAVALIABLE);
				}
			}
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
	
	public static synchronized PDF nextUnhandledPDF(){//add "synchronized" to avoid repeated download
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
		File dir = new File(Config.ROOT_DIR_PATH + "/" +nextWorkDate);
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
	
	public static void exportToExcel(){//record last pdf info,data format: time:name:url
		Log.log("Exporting to excel...");
		File dir = new File(Config.ROOT_DIR_PATH);
		if(!dir.exists()){
			dir.mkdirs();
		}
		
		String fileName = nextWorkDate + ".xls";
		File file = new File(dir, fileName);
		FileOutputStream fos = null;
		HSSFWorkbook book = new HSSFWorkbook();
		HSSFSheet sheet = book.createSheet();
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
		Log.log("Copying to target directory: " + Config.TARGET_DIR_PATH + "/" +nextWorkDate);
		File targetDir = new File(Config.TARGET_DIR_PATH + "/" + nextWorkDate);
		if(!targetDir.exists()){
			targetDir.mkdirs();
		}else{
			if(targetDir.listFiles() != null && targetDir.listFiles().length > 0){
				Log.log("Target directory already exists and is not empty!!!");
				Log.log("Mission canceled!");
				return;
			}
		}
		File originalDir = new File(Config.ROOT_DIR_PATH + "/" + nextWorkDate);
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

	public static synchronized void forceQuit(String msg){
		Log.log(msg);
		Log.log("Force quitting...");
		exportToExcel();
		Log.log("Done");
		if(MailSender.remind(nextWorkDate)){
			Log.log("Succeed!");
		}else{
			Log.log("Failed!");
		}
		
		Log.closeLogFile();
		Log.copyTo(Config.ROOT_DIR_PATH, nextWorkDate + ".log");
		System.exit(-1);
	}

}
