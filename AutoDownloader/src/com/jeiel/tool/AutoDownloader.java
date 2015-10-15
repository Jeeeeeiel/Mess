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
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class AutoDownloader {
	
	public static final String URL = "http://sys.hibor.com.cn/center/maibo/qikanzuixin.asp?page=";
	public static final String params = "&abc=&def=&vidd=&keyy=&F_F=13&pagenumber=&recordnumbe";
	public static final String ROOT_DIR_PATH = "d://DailyPDF";
	public static final String TARGET_DIR_PATH = "d://WBWJ_YBDOWNLOAD";
	private static String nextWorkDate = null;
	private static String beginTime = "";
	private static List<PDF> pdfs = new ArrayList<PDF>();
	private static boolean getElementsSucceed = false;
	private static boolean initializationSucceed = false;
	private static boolean downloadSucceed = false;
	private static boolean extractPDFURLSucceed = false;
	private static int downloaded = 0;
	private static int extracted = 0;

	static{
		File rootDir = new File(ROOT_DIR_PATH);
		if(!rootDir.exists()){
			rootDir.mkdirs();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Log.log("Start");
		Log.log("Initializing...");
		Log.log("Root Directory: " + ROOT_DIR_PATH);
		getNextWorkDate();
		checkFileExist();
		while(!initializationSucceed){
			initDownloadList();
		}
		Log.log("Extracting PDF URL...");
		while(!extractPDFURLSucceed){
			ExtractPDFURL();
		}
		generateFileName();
		Log.log("Downloading...");
		while(!downloadSucceed){
			download();
		}
		record();
		copyReportsToTargetDir();
		Log.log("Done");
		Log.closeLogFile();
		Log.copyTo(ROOT_DIR_PATH, nextWorkDate + ".log");
	}
	
	public static void getNextWorkDate(){
		/*boolean isNextWorkDate = false;
		Scanner in = new Scanner(System.in);
		do{
			if(nextWorkDate == null || nextWorkDate.equals("")){
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				Calendar calendar = Calendar.getInstance();
				do{
					calendar.add(Calendar.DAY_OF_MONTH, 1);
				}while(isHoliday(calendar));
				nextWorkDate = format.format(calendar.getTime());
			}
			Log.log("Next work Date: " + nextWorkDate);
			Log.log("Comfirm(Y/N): ");
			String next = in.next();
			Log.log(next);
			if(next.length() == 1){
				if(next.toUpperCase().equals("Y")){
					isNextWorkDate = true;
				}else if(next.toUpperCase().equals("N")){
					Log.log("Input next work date(yyyyMMdd): ");
					nextWorkDate = in.next();
					isNextWorkDate = false;
				}
			}
		}while(!isNextWorkDate);
		in.close();*/
		
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
	
	public static void initDownloadList(){//没有历史记录就只下当天pdf，有记录就下记录之后的pdf
		pdfs.clear();
		checkRecords();
		Elements elements = null;
		while(!getElementsSucceed){
			elements = getElements();
		}
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
		generateFileName();
		initializationSucceed = true;
		Log.log("File amount: " + pdfs.size());
	}
	
	public static void checkRecords(){//检查上次下载到哪里
		File dir = new File(ROOT_DIR_PATH);
		if(dir.exists()){//判断有无历史记录
			if(dir.listFiles() != null && dir.listFiles().length > 0){
				File file = null;
				FileInputStream fis = null;
				HSSFWorkbook book = null;
				HSSFSheet sheet = null;
				HSSFRow row = null;
				File[] files = dir.listFiles();
				for(int i = files.length - 1; i >= 0 ; i--){
					file = files[i];
					if(file.isFile() && file.getName().endsWith(".xls")){
						try {
							fis = new FileInputStream(file);
							book = new HSSFWorkbook(fis);
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
						}finally{
							try {
								if(fis != null)fis.close();
								if(book != null)book.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								Log.log(e.getMessage());
								e.printStackTrace();
							}
							
						}
						break;
					}
				}
			}
		}
	}
	
	public static Elements getElements(){//获取网页上研报元素
		String url = null;
		Elements elements = new Elements();
		try {
			if(beginTime == null || beginTime.equals("")){//没有历史记录,下载当天
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				beginTime = format.format(new Date()) + " 00:00:00";
			}
			boolean completed = false;
			getElementsSucceed = false;
			int page = 1;
			while(!completed){
				url = URL + page + params;
				Connection conn = Jsoup.connect(url);
				Document doc = conn.timeout(8000).get();
				Elements tmps = doc.getElementsByClass("classbaogao_sousuo_ulresult");
				for(Element e : tmps){
					if(completeDate(e.getElementsByTag("td").get(5).text()).compareTo(beginTime) <= 0){
						completed = true;
						break;
					}
					elements.add(e);
					/*if(elements.size()>=30){
						completed=true;
						break;
					}*/
				}
				page++;
			}
			getElementsSucceed = true;
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			Log.log(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.log(e.getMessage());
			e.printStackTrace();
		}
		
		return elements;
	}
	
	public static String completeDate(String formerDate){//补齐日期
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
	
	public static void generateFileName(){//格式化文件名
		for(PDF pdf : pdfs){
			String date = pdf.getTime().split(" ")[0].replace("-", "");
			String name = pdf.getName().substring(0, pdf.getName().lastIndexOf("-")).replace("*", "");
			String suffix = pdf.getUrl().substring(pdf.getUrl().lastIndexOf("."));
			pdf.setFileName(date + "-" + name + suffix);
			//Log.log(pdf.getFileName());
		}
	}
	
	public static void download(){
		OutputStream os = null;
		InputStream is = null;
		File dir = new File(ROOT_DIR_PATH + "/" +nextWorkDate);
		if(!dir.exists()){
			dir.mkdirs();
		}
		try {
			for(PDF pdf : pdfs){
				if(downloaded > pdfs.indexOf(pdf)){
					continue;
				}
				URL url = new URL(pdf.getUrl()); 
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(10000);;
				conn.setReadTimeout(60000);
				File file = new File(dir, pdf.getFileName());
				if(!file.exists()){
					file.createNewFile();
				}
				os = new FileOutputStream(file);
				is = conn.getInputStream();
				byte[] bytes = new byte[10240];
				int len = 0;
				while((len = is.read(bytes)) > 0){
					os.write(bytes, 0, len);
				}
				downloaded++;
				Log.log("Downloaded: " + downloaded + "\t" + 
						pdf.getName().substring(0, pdf.getName().indexOf("-", pdf.getName().indexOf("-", pdf.getName().indexOf("-") + 1) + 1)) + "   \t" + 
						pdf.getTime());
			}
			downloadSucceed = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	
	public static void ExtractPDFURL(){//抽取网页中frame中的研报链接
		for(PDF pdf : pdfs){
			if(extracted > pdfs.indexOf(pdf)){
				continue;
			}
			try {
				Connection conn = Jsoup.connect(pdf.getUrl());
				Document doc = conn.timeout(5000).get();
				pdf.setUrl(doc.getElementsByTag("iframe").get(0).attr("src"));
				//Log.log(pdf.getUrl());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.log(e.getMessage());
				e.printStackTrace();
			}
			extracted++;
		}
		extractPDFURLSucceed = true;
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
