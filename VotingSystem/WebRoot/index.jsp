<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ page import="java.io.*,service.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>Admin</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
  <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Admin Page</title>

    <!-- Bootstrap core CSS -->
    <link href="./dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap theme -->
    <link href="./dist/css/bootstrap-theme.min.css" rel="stylesheet">

    <script src="./dist/js/jquery.min.js"></script>
     <script src="./dist/js/custome.js"></script>
  </head>

  <body >
  <%
ReadResultTxts resultTable=new ReadResultTxts();
String[][] table=resultTable.getResult();
 %>
 
<div class="container">
      <div class="panel panel-default">
  <!-- Default panel contents -->
  <div class="panel-heading"><h1><%=VoteInfo.getVoteName()%> 投票结果 </h1><h2> 已投票人数:<%=table[0].length-2 %></h2></div>
  <div class="panel-body">
  
    <p>可以点击生成excel生成投票结果</p>
    <input id="genExcel" type="button" class="button" onclick="generateXLS()" value="生成excel" >
    <div id="download"></div>
  </div>

  <!-- Table -->
  <table class="table table-bordered">
      <thead>
          <tr>
          <th>No.</th>
          <%
          for(int i=0;i<table[0].length;i++)
          {
          	out.print("<th>");
          	out.print(table[0][i]);
          	out.print("</th>");
          }
          
           %>
            
          </tr>

      </thead>
      <tbody>
      <%
      
          for(int i=1;i<table.length;i++)
          {
          out.print("<tr>");
          out.print("<td>"+i+"</td>");
          for(int j=0;j<table[0].length;j++)
          {
          	out.print("<td>");
          	out.print(table[i][j]);
          	out.print("</td>");
          }
          	out.print("<tr>"); 
          }
         
           %>
      
      </tbody>
    
  </table>
</div>
      </div>

  </body>
</html>

