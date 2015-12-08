package service;
import java.util.*;
import java.io.*;

import net.sf.json.JSONObject;
public class ReadResultTxts {
	public static void main(String[] args)
	{
		getResult();
	}
	public static String[][] getResult()
	{
		BufferedReader br = null;

		String VoteName=VoteInfo.getVoteName().replaceAll("\\|[\\S\\s]*", "");
		
		ArrayList<String> filenames=new ArrayList<String>();
		try {
			//构造BufferedReader对象
			InputStreamReader isr = new InputStreamReader(new FileInputStream(VoteInfo.getDataRootPath()+"/"+VoteName+"/filelist.txt"), "GBK");  
			br = new BufferedReader(isr);  
			String line = null;
			while ((line = br.readLine()) != null) {

			//将文本打印到控制台
			//System.out.println(line);
				String filename=line;
				filenames.add(filename);
				
			}
			}
			catch (IOException e) {
			e.printStackTrace();
			}
			finally {

			//关闭BufferedReader
			if (br != null) {
			try {
			br.close();
			}
			catch (IOException e) {
			e.printStackTrace();
			}
			}}
		System.out.println(filenames.size());
		String[][] result=new String[VoteInfo.getProjectNum()+1][filenames.size()+2];
		result[0][0]=VoteInfo.getVoteName()+"机构名称";
				result[0][1]="项目名称";
		System.out.println((filenames.size()+2)+","+(VoteInfo.getProjectNum()+1));
		LinkedHashMap<String,String> ProOrg=ExcelOperations.readXml(VoteInfo.getExcelPath());
		Iterator iter = ProOrg.entrySet().iterator(); 
	    int count=1;
		while (iter.hasNext()) {
			if(count==result.length)
			{
			break;
			}
				Map.Entry entry = (Map.Entry) iter.next(); 
		result[count][0]=(String)entry.getValue();
		result[count][1]=(String)entry.getKey();
			
		
	    count++;
		} 
		for(int i=0;i<filenames.size();i++)
		{
			
			try {
				result[0][i+2]=filenames.get(i);//First Line Name
				//构造BufferedReader对象
				br = new BufferedReader(new FileReader(VoteInfo.getDataRootPath()+"/"+VoteName+"/"+filenames.get(i)+".txt"));

				String line = null;
				int linecount=1;
				while ((line = br.readLine()) != null) {

				//将文本打印到控制台
				//System.out.println(line);
					if(!line.equals(""))
					{
						String ABC="";
						String temp=line.split(" ")[line.split(" ").length-1];
						if(temp.equals("0"))
						{
						
							ABC="";
						
						}
						else if(temp.equals("1"))
						{
						
							ABC="A";
						
						}
						else if(temp.equals("2"))
						{
						
							ABC="B";
						
						}
						else if(temp.equals("3"))
						{
						
							ABC="C";
						
						}
						result[linecount][i+2]=ABC;
					}
					
					linecount++;
				}
				}
				catch (IOException e) {
				e.printStackTrace();
				}
				finally {

				//关闭BufferedReader
				if (br != null) {
				try {
				br.close();
				}
				catch (IOException e) {
				e.printStackTrace();
				}
				}}
		}
		
		for(int i=0;i<result.length;i++)
		{
			for(int j=0;j<result[1].length;j++)
			{
				System.out.print(result[i][j]+" ");
			}
			System.out.println();
		}
		return result;
	}

}
