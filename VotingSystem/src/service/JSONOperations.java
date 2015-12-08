package service;
import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.*;
import java.net.URL;
public class JSONOperations {

	public static void main(String[] args) throws IOException
	{ 
		//String data="{\"id\":\"1\",\"JudgeName\":\"鐜嬭�鍒‐",\"VoteName\":\"澶嶅涓�瓑濂朶",\"time\":\"2015-11-03 14:50:50\",\"VoteData\":[{\"id\":\"1\",\"Organization\":\"涓浗寤鸿閾惰鑲′唤鏈夐檺鍏徃娣卞湷甯傚垎琛孿",\"ProjectName\":\"鏅烘収閾惰\",\"Result\":\"0\"},{\"id\":\"2\",\"Organization\":\"娣卞湷璇佸埜浜ゆ槗鎵��涓浗璇佸埜鐧昏缁撶畻鏈夐檺鍏徃娣卞湷鍒嗗叕鍙竆",\"ProjectName\":\"鍦哄唴璇佸埜鎷呬繚铻嶈祫浜ゆ槗\",\"Result\":\"0\"},{\"id\":\"3\",\"Organization\":\"鍓嶆捣鑲℃潈浜ゆ槗涓績锛堟繁鍦筹級鏈夐檺鍏徃\",\"ProjectName\":\"鍓嶆捣鑲℃潈浜ゆ槗涓績鏂板瀷鍦哄璧勬湰甯傚満骞冲彴\",\"Result\":\"0\"},{\"id\":\"4\",\"Organization\":\"涓浗閾惰鑲′唤鏈夐檺鍏徃娣卞湷甯傚垎琛孿",\"ProjectName\":\"鐢靛瓙鍟嗗姟鍏ㄧ悆閲戣瀺鏈嶅姟鏂规\",\"Result\":\"0\"},{\"id\":\"5\",\"Organization\":\"涓浗宸ュ晢閾惰鑲′唤鏈夐檺鍏徃娣卞湷甯傚垎琛孿",\"ProjectName\":\"宸ラ摱鍏宠锤E鑱旂洘\",\"Result\":\"0\"},{\"id\":\"6\",\"Organization\":\"鎷涘晢閾惰\",\"ProjectName\":\"鎷涘晢閾惰鏅烘収渚涘簲閾鹃噾铻峔",\"Result\":\"0\"},{\"id\":\"7\",\"Organization\":\"涓浗浜烘皯璐骇淇濋櫓鑲′唤鏈夐檺鍏徃娣卞湷甯傚垎鍏徃\",\"ProjectName\":\"娣卞湷甯傚法鐏鹃闄╃爺绌跺強宸ㄧ伨淇濋櫓瀹夋帓\",\"Result\":\"0\"},{\"id\":\"8\",\"Organization\":\"鐢熷懡浜哄淇濋櫓鑲′唤鏈夐檺鍏徃\",\"ProjectName\":\"瀹濊礉瀛橀挶缃怽",\"Result\":\"0\"},{\"id\":\"9\",\"Organization\":\"鍥藉寮�彂閾惰娣卞湷甯傚垎琛屻�涓叴閫氳鑲′唤鏈夐檺鍏徃\",\"ProjectName\":\"鍒涙柊鈥滆蛋鍑哄幓鈥濅笟鍔＄粨鏋勫寲铻嶈祫妯″紡锛屽姪鍔涗腑鍏撮�璁垚鍔熸嫇灞曟娲插競鍦篭",\"Result\":\"0\"},{\"id\":\"10\",\"Organization\":\"娣卞湷鎺掓斁鏉冧氦鏄撴墍鏈夐檺鍏徃\",\"ProjectName\":\"娣卞湷纰虫帓鏀炬潈浜ゆ槗甯傚満寤鸿\",\"Result\":\"0\"},{\"id\":\"11\",\"Organization\":\"鎷涘晢閾惰\",\"ProjectName\":\"鎷涘晢閾惰浜掕仈缃戦噾铻嶅钩鍙扳�灏忎紒涓欵瀹垛�\",\"Result\":\"1\"}]}";
		//AnalyseSaveJSON(data);
		System.out.println(NewVote());
	}
	public static String NewVote() throws IOException
	{
		ExcelOperations excel=new ExcelOperations();
		//File file = new File(JSONOperations.class.getResource("/").getPath()+"../../VoteName.txt");
		//System.out.println(file.getAbsolutePath());
		//System.out.println(JSONOperations.class.getResource("/").getPath() );
		
		String VoteName=VoteInfo.getVoteName();
		
		JSONObject VoteList=new JSONObject();
		VoteList.put("id", "1");
		VoteList.put("VoteName", VoteName);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		  Date d =new Date();
		  String dd =format.format(d);
		VoteList.put("time", dd);
		
		//VoteSubjects
		JSONObject SubjectItem;
	    List<JSONObject> SubjectList=new ArrayList<JSONObject>();
	    /*String[][] RawData={{"1","娣卞湷璇佸埜浜ゆ槗鎵�,"鏂囨湰鎸栨帢绯荤粺"}};
	    for(int i=0;i<RawData.length;i++){
	    	if(RawData[i].length==3)
	    	{
	    		SubjectItem=new JSONObject();
	    	    SubjectItem.put("id", RawData[i][0]);
	    	    SubjectItem.put("Organization", RawData[i][1]);
	    	    SubjectItem.put("ProjectName", RawData[i][2]);
	    	    SubjectList.add(SubjectItem);
	    	}
	    	
	    }*/
	    VoteInfo info=new VoteInfo();
	    LinkedHashMap<String,String> data=excel.readXml(info.getExcelPath());
	    Iterator iter = data.entrySet().iterator(); 
	    int count=1;
		while (iter.hasNext()) { 
		Map.Entry entry = (Map.Entry) iter.next(); 
		SubjectItem=new JSONObject();
	    SubjectItem.put("id", count+"");
	    SubjectItem.put("Organization", entry.getValue());
	    SubjectItem.put("ProjectName", entry.getKey());
	    SubjectList.add(SubjectItem);
	    count++;
		} 
	    VoteList.put("VoteSubjects", SubjectList);
	    //System.out.println(new String( VoteList.toString().getBytes("utf8")));
	    JSONObject result = new JSONObject();
	    result.put("result", VoteList);
	    return result.toString();
		
	}

	public static String AnalyseSaveJSON(String str)
	{
		try{
			JSONObject json = JSONObject.fromObject(str);
			String id=(String)json.get("id");
			String JudgeName=(String)json.get("JudgeName");
			String WebVoteName=((String)json.get("VoteName")).replaceAll("\\|[\\S\\s]*", "");
			String VoteName=VoteInfo.getVoteName().replaceAll("\\|[\\S\\s]*", "");
			if(!VoteName.equals(WebVoteName))
			{
				return "VoteName outdate";
			}
			List<JSONObject> VoteData=new ArrayList<JSONObject>();
			JSONArray arr=json.getJSONArray("VoteData");
			StringBuffer DataString=new StringBuffer();
			for(int i=0;i<arr.length();i++)
			{
				JSONObject temp=arr.getJSONObject(i);
				VoteData.add(temp);
				//System.out.print("id:"+temp.getString("id"));
				//System.out.print(" Organization:"+temp.getString("Organization"));
				//System.out.println(" ProjectName:"+temp.getString("ProjectName"));
				//System.out.println(" Result:"+temp.getString("Result"));
				
				DataString.append(temp.getString("id")+" "+temp.getString("Organization")+" "+temp.getString("ProjectName")+" "+temp.getString("Result")+"\r\n");
			}
			//System.out.println(VoteInfo.getDataRootPath()+"/"+VoteName+"/"+JudgeName+".txt");
			File file = new File(VoteInfo.getDataRootPath()+"/"+VoteName+"/"+JudgeName+".txt");
			
			File parent = file.getParentFile(); 
			if(parent!=null&&!parent.exists()){ 
			parent.mkdirs(); 
			} 
			file.createNewFile(); 
			FileOutputStream o=new FileOutputStream(file);
			
			o.write((new String(DataString)).getBytes());
			o.close();
			System.out.println(VoteInfo.getDataRootPath()+"/"+VoteName+"/filelist.txt");
			File file2=new File(VoteInfo.getDataRootPath()+"/"+VoteName+"/filelist.txt");
			if(!file2.exists())
			{
				System.out.println("file not exists");
				file2.createNewFile();
				FileOutputStream output=new FileOutputStream(file2.getAbsolutePath(), true);
				output.write((JudgeName).getBytes("gbk"));
				output.close();
			}
			else{
				//System.out.println("hi???????");
				
				InputStreamReader is=new InputStreamReader(new FileInputStream(VoteInfo.getDataRootPath()+"/"+VoteName+"/filelist.txt"), "GBK");
				BufferedReader in=new BufferedReader(is);
				String a=JudgeName;
				boolean flag=false;
				while(in.ready())
				{
					String line=in.readLine();
					System.out.println(line);
					if(line.equals(a))
					{
						System.out.println(a+""+line);
						flag=true;
						break;
					}
						
				}
				in.close();
				if(!flag)
				{
					FileOutputStream output=new FileOutputStream(file2.getAbsolutePath(), true);
					output.write(("\r\n"+JudgeName).getBytes("gbk"));
					output.close();
				}
				
			}
			
		   
			//System.out.println(id+JudgeName+VoteName+VoteData);
			return "true";
		}
		catch(Exception ee)
		{
			return "false";
		}
		
	}
}
