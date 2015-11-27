package servlet;
import service.*;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
public class DataServlet extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public DataServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request,response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		

		response.setContentType("text/json");
		response.setCharacterEncoding("utf-8");
		request.setCharacterEncoding("utf-8");
		String json=request.getParameter("data");
		JSONOperations ana=new JSONOperations();
		String result=ana.AnalyseSaveJSON(json);
		PrintWriter out = response.getWriter();
		JSONObject obj = new JSONObject();
		
		JSONObject r=new JSONObject();
		if(result.equals("true"))
		{	
			obj.put("success", true);
			r.put("result", obj);
			out.print(r.toString());
		}
		else if(result.equals("VoteName outdate"))
		{
			obj.put("success", "outdate");
			r.put("result", obj);
			out.print(r.toString());
		}
		else{
			
			obj.put("success", false);
			r.put("result", obj);
			out.print(r.toString());
		}
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
