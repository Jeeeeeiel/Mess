package service;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

import net.sf.json.JSONObject;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
public class ExcelOperations {

	public static void main(String[] args) {  
        readXml("D:/test.xlsx");  
        //System.out.println("-------------");  
        //readXml("d:/test2.xls");  
    }  
	public static String readVoteName(String fileName)
	{
		String result="";
        boolean isE2007 = false;    //判断是否是excel2007格式  
        if(fileName.endsWith("xlsx"))  
            isE2007 = true;  
        try {  
            InputStream input = new FileInputStream(fileName);  //建立输入流  
            Workbook wb  = null;  
            //根据文件格式(2003或者2007)来初始化  
            if(isE2007)  
                wb = new XSSFWorkbook(input);  
            else  
                wb = new HSSFWorkbook(input);  
            Sheet sheet = wb.getSheetAt(0);     //获得第一个表单  '
            result=sheet.getRow(0).getCell(0).getStringCellValue();
            
              
        } catch (IOException ex) {  
            ex.printStackTrace();  
        }  
        return result;
	}
    public static LinkedHashMap<String,String> readXml(String fileName){
    	LinkedHashMap<String,String> result=new LinkedHashMap<String,String>();
        boolean isE2007 = false;    //判断是否是excel2007格式  
        if(fileName.endsWith("xlsx"))  
            isE2007 = true;  
        try {  
            InputStream input = new FileInputStream(fileName);  //建立输入流  
            Workbook wb  = null;  
            //根据文件格式(2003或者2007)来初始化  
            if(isE2007)  
                wb = new XSSFWorkbook(input);  
            else  
                wb = new HSSFWorkbook(input);  
            Sheet sheet = wb.getSheetAt(0);     //获得第一个表单  
            Iterator<Row> rows = sheet.rowIterator(); //获得第一个表单的迭代器
            while (rows.hasNext()) {  
            	
                Row row = rows.next();  //获得行数据  
                //System.out.println("Row #" + row.getRowNum());  //获得行号从0开始  
                if(row.getRowNum()>=1)
                {
                	 Iterator<Cell> cells = row.cellIterator();    //获得第一行的迭代器  
                	 int index=0;
                	 String Organization="";
                	 String ProjectName="";
                    while (cells.hasNext()) {  
                    Cell cell = cells.next();  
                    //System.out.println("Cell #" + cell.getColumnIndex());  
                    if(index==0)
                    {
                    	 //System.out.print("Organization:\""+cell.getStringCellValue()+"\",");
                    	 //result.put("Organization", cell.getStringCellValue());
                    	 Organization=cell.getStringCellValue();
                    	 //System.out.println(Organization);
                    	 
                    }
                    else if(index==1)
                    {
                    	 //System.out.print("ProjectName:\""+cell.getStringCellValue()+"\" "); 
                    	 //result.put("ProjectName", cell.getStringCellValue());
                    	 ProjectName=cell.getStringCellValue();
                    	 //System.out.println(ProjectName);
                    	 result.put(ProjectName,Organization);
                    }
                    
                    index++;
                        //System.out.print(cell.getStringCellValue()+" ");  
                       
                    } 
                    //System.out.println();
                }
                
               
            }
            LinkedHashMap<String,String> data=result;
    	    Iterator iter = data.entrySet().iterator(); 
    	    int count=1;
    		while (iter.hasNext()) { 
    		Map.Entry entry = (Map.Entry) iter.next();
    	    //System.out.println("Organization-"+entry.getValue());
    	    //System.out.println("ProjectName-"+entry.getKey());
    		} 
              
        } catch (IOException ex) {  
            ex.printStackTrace();  
        }  
        return result;
    }  

    public static String exportExcel(String[][] data) {     
    				String filename="";
    	//创建excel文件对象  
    		        HSSFWorkbook wb = new HSSFWorkbook();  
    		        //创建一个张表  
    		        Sheet sheet = wb.createSheet();  
    		        //创建第一行  
    		        for(int i=0;i<data.length;i++)
    		        {
    		        	Row row=sheet.createRow(i);
    		        	for(int j=0;j<data[0].length;j++)
    		        	{
    		        		row.createCell(j).setCellValue(data[i][j]);
    		        	}
    		        }  
    		        
    		        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    		        Date d =new Date();
    		        String dd =format.format(d);
    		        try
    		        {
    		        	filename="/ExcelFile/"+dd+".xls";
    		        	File file=new File(ExcelOperations.class.getResource("/").getPath().replace("%20", " ")+"/../../"+filename);
    		        	System.out.println(ExcelOperations.class.getResource("/").getPath().replace("%20", " ")+"/../../"+filename);
    		        	//File file=new File(path+"/"+filename);
        		        file.createNewFile();
        		        FileOutputStream out=new FileOutputStream(file);
        		        wb.write(out);
        		        wb.close();
    		        }
    		        catch(Exception ee)
    		        {
    		        	ee.printStackTrace();
    		        	
    		        }
    		        
    		        return filename;  
    		    }     
    		  
}
