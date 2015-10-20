package com.jeiel.modify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
/**
 * 可先测试统计2014年度，合并口径下的合并财务报表、合并利润表、合并现金流量表、合并口径主要财务指标4个表中的下列指标：
    资产总计、所有者权益合计、负债合计、资产负债率、归属于母公司所有者的净利润、经营活动产生的现金流量净额、利息保障倍数、存货周转率...
 * @author Administrator
 *
 */

public class TableExtractor {
	
	private static final String ROOT_DIR_PATH = "反馈报告";//当前目录改为“.”即可
	private static final String DOC2DOCX_PATH = "./Doc2Docx.exe";
	private static final int TYPE_DOC = 0;
	private static final int TYPE_DOCX = 1;
	private static HSSFWorkbook book;
	private static HSSFSheet sheet;
	private static HSSFRow row;
	private static int rowIndex;
	
	public static void main(String[] args){
		List<File> fileList = initFileList();
		initExcelWriter();
		for(File file: fileList){
			row = sheet.createRow(rowIndex);
			chooseMethod(file);
			checkData();
			rowIndex++;
			/*if(rowIndex>1){
				break;
			}*/
		}
		exportExcel();
		
	}
	
	public static List<File> initFileList(){
		List<File> fileList = new ArrayList<File>();
		File dir = new File(ROOT_DIR_PATH);
		System.out.println(dir.getAbsolutePath());
		if(dir.listFiles() != null){
			for(File file : dir.listFiles()){
				if(file.isFile() && checkFileType(file.getName())){
					fileList.add(file);
					//System.out.println(file.getName());
				}
			}
		}
		System.out.println("File count: " + fileList.size());
		return fileList;
	}
	
	public static boolean checkFileType(String fileName){
		if(fileName.toLowerCase().endsWith(".doc") ||
				fileName.toLowerCase().endsWith("docx")){
			if(!fileName.startsWith("~$")){
				return true;
			}
		}
		return false;
	}
	
	public static void chooseMethod(File file){
		int type = -1;
		if(file.getName().toLowerCase().endsWith(".doc")){
			type = TYPE_DOC;
		}else if(file.getName().toLowerCase().endsWith(".docx")){
			type = TYPE_DOCX;
		}
		switch(type){
			case TYPE_DOC:
				extractFromDoc(file);
				break;
			case TYPE_DOCX:
				extractFromDocx(file);
				break;
			default:
				break;
		}
	}
	
	private static Process process = null;
	
	public static void extractFromDoc(final File file){
        try {
        	File exePath = new File(DOC2DOCX_PATH);
        	//System.out.println(exePath.getAbsolutePath());
        	final String cmdStr = exePath.getAbsolutePath() + " " + file.getAbsolutePath();
        	//System.out.println(cmdStr);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						process = Runtime.getRuntime().exec(cmdStr);
						process.waitFor();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			t.start();
			t.join(10000);
			File newfile = new File(file.getAbsolutePath() + "x");
			if(newfile.exists()){
				//System.out.println("转换成功: " + newfile.getAbsolutePath());
				extractFromDocx(newfile);
				file.delete();
				//newfile.delete();
			}else{
				System.out.println("转换失败: " + file.getAbsolutePath());
				if(process != null){
					process.destroy();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private static String unit = "";
	private static String u="";
	
	public static void extractFromDocx(File file){
		FileInputStream fis = null;
		XWPFDocument doc = null;
		unit = "";
		u = "";
		try {
			System.out.println(rowIndex + "\t" + file.getAbsolutePath());
			writeToExcel(0, "" + rowIndex);
			writeToExcel(1, file.getName().substring(0, file.getName().indexOf(".docx")));
			
			
			fis = new FileInputStream(file);
			doc = new XWPFDocument(fis);
			Iterator<IBodyElement> iterator = doc.getBodyElementsIterator();
			IBodyElement element;
			XWPFParagraph paragraph;
			
			XWPFTable table = null;
			
			while(iterator.hasNext()){//寻找标题
				element = iterator.next();
				if(BodyElementType.PARAGRAPH.equals(element.getElementType())){
					paragraph = (XWPFParagraph) element;
					if(isTargetTable(paragraph.getText())){//找到符合的表格标题
						while(iterator.hasNext()){//寻找表格
							element = iterator.next();
							if(BodyElementType.TABLE.equals(element.getElementType())){//找到标题后面紧跟的表格
								table = (XWPFTable) element;
								boolean isTableHead = true;
								Map<String,Integer> yearMap = new HashMap<String,Integer>();
								for(XWPFTableRow row:table.getRows()){//遍历每一行
									if(isTableHead){//获取年份对应的列号
										int index = 0;
										for(XWPFTableCell cell : row.getTableCells()){//遍历每一列
											String year = isTargetColumn(cell.getText());
											if(year != null){
												yearMap.put(year, index);
											}
											index++;
										}
										isTableHead = false;
									}else{//根据需要的属性获取数据
										String subject = isTargetSubject(row.getCell(0).getText());
										if(subject != null){
											int multiple = 1;
											if(row.getTableCells().size() != table.getRow(0).getTableCells().size()){
												multiple = (row.getTableCells().size() + 1)/table.getRow(0).getTableCells().size();
											}
											for(String year : Parameter.YEARS){//遍历每一列
												if(yearMap.containsKey(year)){
													writeToExcel(2 + getIndexFromArray(year, Parameter.YEARS) * (Parameter.SUBJECTS.length + 1), unit);
													writeToExcel(3 + getIndexFromArray(year, Parameter.YEARS) * (Parameter.SUBJECTS.length + 1) + 
															getIndexFromArray(subject, Parameter.SUBJECTS), 
															row.getCell(multiple==1?yearMap.get(year):(yearMap.get(year)*multiple-1)).getText().replace("%", "") + u);
												}
											}
										}
									}
									
								}
							}else if(BodyElementType.PARAGRAPH.equals(element.getElementType()) && ((XWPFParagraph)element).getText().contains("公司债券发行方案")){
								break;
							}else if(BodyElementType.PARAGRAPH.equals(element.getElementType()) && "".equals(unit) && table == null){//获取单位
								unit = ((XWPFParagraph)element).getText().trim().replace("　", "");
								if(!unit.contains("元")){
									unit = "";
								}
								/*if(!"".equals(unit)){
									System.out.println(unit);
								}*/
							}
						}
						table = null;
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(doc != null)doc.close();
				if(fis != null)fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public static int getIndexFromArray(String element, String[] array){
		if(element == null || array == null){
			return -1;
		}
		int i = 0;
		for(String tmp : array){
			if(tmp.equals(element)){
				return i;
			}
			i++;
		}
		return -1;
	}
	
	public static boolean isTargetTable(String name){
		//System.out.println(name);
		for(String table:Parameter.TABLES){
			if(name.contains(table)){
				//System.out.println(name);
				return true;
			}
		}
		return false;
	}
	
	public static String isTargetSubject(String name){
		name = name.replaceAll("\\s*", "");
		name = name.replaceAll(" ", "");
		name = name.replaceAll("　", "");
		if(name.contains("亿元")){
			u="亿元";
		}else if(name.contains("万元")){
			u="万元";
		}else  if(name.contains("元")){
			u="元";
		}else{
			u="";
		}
		if(name.contains("（")){
			name = name.substring(0, name.indexOf("（"));
		}
		
		for(String subject:Parameter.SUBJECTS){
			for(String s:subject.split(";")){
				if(name.trim().equals(s)){
					return subject;
				}
			}
		}
		return null;
	}
	
	public static String isTargetColumn(String text){
		for(String year:Parameter.YEARS){
			if(text.contains(year)){
				return year;
			}
		}
		return null;
	}
	
	public static void initExcelWriter(){
		book = new HSSFWorkbook();
		sheet = book.createSheet("ExtractorData");
		rowIndex = 0;
		row = sheet.createRow(rowIndex);
		row.createCell(0).setCellValue("编号");
		row.createCell(1).setCellValue("股票名称");
		for(int i = 0; i < Parameter.YEARS.length; i++){
			row.createCell(2 + i * (Parameter.SUBJECTS.length + 1)).setCellValue(Parameter.YEARS[i]);
			for(int j = 0; j < Parameter.SUBJECTS.length; j++){
				row.createCell(3 + i * (Parameter.SUBJECTS.length + 1) + j).setCellValue(Parameter.SUBJECTS[j].split(";")[0]);
			}
		}
		rowIndex++;
	}
	
	public static void writeToExcel(int col, String content){
		row.createCell(col).setCellValue(content);
	}
	
	public static void checkData(){//补齐可计算得出的数据
		if(sheet.getLastRowNum()>0){
			for(int i = 0; i < Parameter.YEARS.length; i++){//i代表年数
				for(int j = 3 + i * (Parameter.SUBJECTS.length + 1); j < (i + 1) * (Parameter.SUBJECTS.length + 1) +1; j++){//某一年的所有列数
					if(row.getCell(j)==null){
						DecimalFormat df=(DecimalFormat)NumberFormat.getInstance(); 
						df.setMaximumFractionDigits(2);//两位小时
						if(j == 3 + i * (Parameter.SUBJECTS.length + 1)){//资产总计   资产总计=所有者权益+负债合计
							if(row.getCell(j + 1) != null && row.getCell(j + 2) != null){
								if(isNumber(row.getCell(j + 1).getStringCellValue()) &&
										isNumber(row.getCell(j + 2).getStringCellValue())){
									row.createCell(j).setCellValue(df.format((Double.parseDouble(row.getCell(j + 1).getStringCellValue().replace(",", "").trim()) +
											Double.parseDouble(row.getCell(j + 2).getStringCellValue().replace(",", "").trim()))));
								}
							}
						}else if(j == 4 + i * (Parameter.SUBJECTS.length + 1)){//所有者权益   所有者权益=资产总计-负债合计
							if(row.getCell(j - 1) != null && row.getCell(j + 1) != null){
								if(isNumber(row.getCell(j - 1).getStringCellValue()) &&
										isNumber(row.getCell(j + 1).getStringCellValue())){
									row.createCell(j).setCellValue(df.format((Double.parseDouble(row.getCell(j - 1).getStringCellValue().replace(",", "").trim()) -
											Double.parseDouble(row.getCell(j + 1).getStringCellValue().replace(",", "").trim()))));
								}
							}
						}else if(j == 5 + i * (Parameter.SUBJECTS.length + 1)){//负债合计  负债合计 =资产总计-所有者权益  或者   负债合计=资产总计*资产负债率/100
							if(row.getCell(j - 2) != null && row.getCell(j - 1) != null){//负债合计 =资产总计-所有者权益
								if(isNumber(row.getCell(j - 2).getStringCellValue()) &&
										isNumber(row.getCell(j - 1).getStringCellValue())){
									row.createCell(j).setCellValue(df.format((Double.parseDouble(row.getCell(j - 2).getStringCellValue().replace(",", "").trim()) -
											Double.parseDouble(row.getCell(j - 1).getStringCellValue().replace(",", "").trim()))));
								}
							}else if(row.getCell(j - 2) != null && row.getCell(j + 1) != null){//负债合计=资产总计*资产负债率/100
								if(isNumber(row.getCell(j - 2).getStringCellValue()) &&
										isNumber(row.getCell(j + 1).getStringCellValue())){
									row.createCell(j).setCellValue(df.format((Double.parseDouble(row.getCell(j - 2).getStringCellValue().replace(",", "").trim()) *
											Double.parseDouble(row.getCell(j + 1).getStringCellValue().replace(",", "").trim())) / 100));
									//j-2有值，j-1没值     所有者权益=资产总计-负债合计
									row.createCell(j - 1).setCellValue(df.format((Double.parseDouble(row.getCell(j - 2).getStringCellValue().replace(",", "").trim()) -
											Double.parseDouble(row.getCell(j).getStringCellValue().replace(",", "").trim()))));
								}
							}
						}else if(j == 6 + i * (Parameter.SUBJECTS.length + 1)){//资产负债率   资产负债率=负债合计/资产总计*100
							if(row.getCell(j - 1) != null && row.getCell(j - 3) != null){
								if(isNumber(row.getCell(j - 1).getStringCellValue()) &&
										isNumber(row.getCell(j - 3).getStringCellValue())){
									row.createCell(j).setCellValue(df.format((Double.parseDouble(row.getCell(j - 1).getStringCellValue().replace(",", "").trim()) /
											Double.parseDouble(row.getCell(j - 3).getStringCellValue().replace(",", "").trim()) * 100)));
								}
							}
						}else if(j == 7 + i * (Parameter.SUBJECTS.length + 1)){
							
						}else if(j == 8 + i * (Parameter.SUBJECTS.length + 1)){
							
						}else if(j == 9 + i * (Parameter.SUBJECTS.length + 1)){
							
						}else if(j == 10 + i * (Parameter.SUBJECTS.length + 1)){
							
						}
					}
				}
			}
		}
		
	}
	
	public static boolean isNumber(String str){
		try{
			Double.parseDouble(str.replace(",", ""));
			return true;
		}catch(NullPointerException e){
			//e.printStackTrace();
			return false;
		}catch (NumberFormatException e) {
			//e.printStackTrace();
			return false;
		}
	}
	
 	public static void exportExcel(){
		File file = new File(ROOT_DIR_PATH + "/" + "Extractor.xls");
		System.out.println("Export Excel: " + file.getAbsolutePath());
		try {
			if(!file.exists()){
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);
			book.write(fos);
			book.close();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
