package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import service.ExcelOperations;
import service.JSONOperations;
import service.ReadResultTxts;

public class GenExcelServlet extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public GenExcelServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request,response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/json");
		response.setCharacterEncoding("utf-8");
		request.setCharacterEncoding("utf-8");
		//System.out.println(getServletContext().getServerInfo()+getServletContext().getContextPath());
		String result=ExcelOperations.exportExcel(ReadResultTxts.getResult());
		
		PrintWriter out = response.getWriter();
		JSONObject obj = new JSONObject();
		
		JSONObject r=new JSONObject();
		if(result!=null)
		{	
			obj.put("genStatus", true);
			obj.put("fileName", result);
			obj.put("fileURL", "./"+result);
			r.put("result", obj);
			out.print(r.toString());
		}
		else{
			obj.put("genStatus", false);
			r.put("result", obj);
			out.print(r.toString());
		}
		System.out.println(r.toString());
		out.flush();
		out.close();
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
