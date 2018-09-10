import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Locale;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class SearchServlet
{
	private static final Logger logger = LogManager.getLogger();
	private final WorkQueue queue;
	public static final int PORT = 8080;
	//System.setProperty("org.eclipse.jetty.LEVEL", "DEBUG");
	Server server = new Server(PORT);
	
	public SearchServlet(ThreadedInvertedIndex index, ThreadedQueryHelper query)
	{
		queue = new WorkQueue();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
		request.getContextPath();
		PrintWriter writer = response.getWriter();
		
		writer.write("<p>Search for: <input type=\"text\"></p>");
		writer.write("\n");
		writer.write("<p><input type=\"submit\" value=\"\"></p>");
		
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
//		PrintWriter writer = response.getWriter();
//		writer.write("<p>Search for: <input type=\"text\"></p>");
//		writer.write("\n");
//		writer.write("<p><input type=\"submit\" value=\"Post Message\"></p>");
	}
}