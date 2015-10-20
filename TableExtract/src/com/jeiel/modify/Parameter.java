package com.jeiel.modify;

public class Parameter {
	public static final String[] TABLES = new String[]{
		/*"合并资产负债表",
		"合并利润表",
		"合并现金流量表",
		"主要财务数据和财务指标",
		"最近三年一期的主要财务指标",//这个表名和下一个表名仅为表述不同
		"最近三年及一期主要财务指标",
		"主要财务数据和财务指标",
		"负债结构（合并口径）"*/
		"发行人主要财务数据和财务指标"
	};
	
	public static final String[] SUBJECTS = new String[]{//每项数据中的第一个名称为excel列名
		"资产总计;总资产;负债及所有者权益合计;负债及所有者权益总计;负债和所有者权益总计;资产总额",
		"所有者权益;所有者权益合计;股东权益合计",
		"负债合计;总负债",
		"资产负债率",
		"归属于母公司所有者的净利润;归属于母公司股东的净利润;归于母公司所有者的净利润;",
		"经营活动产生的现金流量净额;经营活动产生现金流量净额",//经营活动产生的现金流量净额
		"利息保障倍数;EBITDA利息倍数;EBITDA利息保障倍数;利息倍数",
		"存货周转率"
	};
	public static final String[] YEARS = new String[]{
		"2014"
	};
}
