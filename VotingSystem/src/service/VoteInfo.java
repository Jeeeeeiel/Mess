package service;


public class VoteInfo {
	public static String VoteName="";
	public static int ProjectNum=0;
	public static String ExcelPath="d:/VoteList.xlsx";
	public static String getDataRootPath() {
		return DataRootPath;
	}
	public static void setDataRootPath(String dataRootPath) {
		DataRootPath = dataRootPath;
	}
	public static String DataRootPath="d://Vote2015/";
	public static String getExcelPath() {
		return ExcelPath;
	}
	public static void setExcelPath(String excelPath) {
		ExcelPath = excelPath;
	}
	public static String getVoteName() {
		setVoteName(ExcelOperations.readVoteName(getExcelPath()));
		return VoteName;
	}
	public static void setVoteName(String voteName) {
		VoteName = voteName;
	}
	public static int getProjectNum() {
		 //System.out.println(ExcelOperations.readXml(ExcelPath).size());
		return ExcelOperations.readXml(ExcelPath).size();
	}
	public static void setProjectNum(int projectNum) {
		ProjectNum = projectNum;
	}
	

}
