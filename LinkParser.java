import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkParser 
{

	// https://developer.mozilla.org/en-US/docs/Web/HTML/Element/a
	// https://docs.oracle.com/javase/tutorial/networking/urls/creatingUrls.html
	// https://developer.mozilla.org/en-US/docs/Learn/Common_questions/What_is_a_URL

	/** Port used by socket. For web servers, should be port 80. */
	public static final int DEFAULT_PORT = 80;
	
	/** Version of HTTP used and supported. */
	public static final String version = "HTTP/1.1";
	
	/** Valid HTTP method types. */
	public static enum HTTP {
		OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT
	};
	/**
	 * Removes the fragment component of a URL (if present), and properly
	 * encodes the query string (if necessary).
	 *
	 * @param url
	 *            url to clean
	 * @return cleaned url (or original url if any issues occurred)
	 */
	public static URL clean(URL url)
	{
		try 
		{
			return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), null).toURL();
		}
		catch (MalformedURLException | URISyntaxException e)
		{
			return url;
		}
	}
	
	/**
	 * Convenience method to get the header field names mapped to their values
	 * for the specified URL.
	 *
	 * @param url
	 *            - url to fetch
	 * @return field names mapped to values if the headers are properly
	 *         formatted
	 *
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static Map<String, String> getHeaderFields(String url) throws UnknownHostException, MalformedURLException, IOException
	{
		URL target = new URL(url);
		String request = craftHTTPRequest(target, HTTP.HEAD);
		List<String> lines = fetchLines(target, request);

		return parseHeaders(lines);
	}

	/**
	 * Helper method that parses HTTP headers into a map where the key is the
	 * field name and the value is the field value. The status code will be
	 * stored under the key "Status".
	 *
	 * @param headers
	 *            - HTTP/1.1 header lines
	 * @return field names mapped to values if the headers are properly
	 *         formatted
	 */
	public static Map<String, String> parseHeaders(List<String> headers)
	{
		Map<String, String> fields = new HashMap<>();

		if (headers.size() > 0 && headers.get(0).startsWith(version))
		{
			fields.put("Status", headers.get(0).substring(version.length()).trim());

			for (String line : headers.subList(1, headers.size()))
			{
				String[] pair = line.split(":", 2);

				if (pair.length == 2) 
				{
					fields.put(pair[0].trim(), pair[1].trim());
				}
			}
		}

		return fields;
	}
	
	/**
	 * Will connect to the web server and fetch the URL using the HTTP request
	 * provided. It would be more efficient to operate on each line as returned
	 * instead of storing the entire result as a list.
	 *
	 * @param url
	 *            - url to fetch
	 * @param request
	 *            - full HTTP request
	 *
	 * @return the lines read from the web server
	 *
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public static List<String> fetchLines(URL url, String request) throws UnknownHostException, IOException 
	{
		ArrayList<String> lines = new ArrayList<>();
		int port = url.getPort() < 0 ? DEFAULT_PORT : url.getPort();

		try 
		(
				Socket socket = new Socket(url.getHost(), port);
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				PrintWriter writer = new PrintWriter(socket.getOutputStream());
		) 
		{
			writer.println(request);
			writer.flush();

			String line = null;

			while ((line = reader.readLine()) != null)
			{
				lines.add(line);
			}
		}

		return lines;
	}
	
	/**
	 * Crafts a minimal HTTP/1.1 request for the provided method.
	 *
	 * @param url
	 *            - url to fetch
	 * @param type
	 *            - HTTP method to use
	 *
	 * @return HTTP/1.1 request
	 *
	 * @see {@link HTTP}
	 */
	public static String craftHTTPRequest(URL url, HTTP type) 
	{
		String host = url.getHost();
		String resource = url.getFile().isEmpty() ? "/" : url.getFile();

		// The specification is specific about where to use a new line
		// versus a carriage return!
		return String.format("%s %s %s\r\n" + "Host: %s\r\n" + "Connection: close\r\n" + "\r\n", type.name(), resource, version, host);
	}


	/**
	 * Fetches the HTML (without any HTTP headers) for the provided URL. Will
	 * return null if the link does not point to a HTML page.
	 *
	 * @param url
	 *            url to fetch HTML from
	 * @return HTML as a String or null if the link was not HTML
	 */
	
	public static String fetchHTML(URL url)
	{

		String request = craftHTTPRequest(url, HTTP.GET);
		List<String> lines = null;
		try
		{
			lines = fetchLines(url, request);
		}
		catch (IOException e)
		{
			System.out.println();
		}

		int start = 0;
		int end = lines.size();

		// Determines start of HTML versus headers.
		while (!lines.get(start).trim().isEmpty() && start < end) {
			start++;
		}

		// Double-check this is an HTML file.
		Map<String, String> fields = parseHeaders(lines.subList(0, start + 1));
		String type = fields.get("Content-Type");

		if (type != null && type.toLowerCase().contains("html")) 
		{
			return String.join(System.lineSeparator(), lines.subList(start + 1, end));
		}

		return null;
	}

	/**
	 * Returns a list of all the HTTP(S) links found in the href attribute of the
	 * anchor tags in the provided HTML. The links will be converted to absolute
	 * using the base URL and cleaned (removing fragments and encoding special
	 * characters as necessary).
	 *
	 * @param base
	 *            base url used to convert relative links to absolute3
	 * @param html
	 *            raw html associated with the base url
	 * @return cleaned list of all http(s) links in the order they were found
	 */
	public static ArrayList<String> listLinks(URL base, String html)
	{
		ArrayList<String> links = new ArrayList<>();
		String regex = "(?i)<a[^>]*\\s*href\\s*=\\s*\"(.+?)\\s*\"";
		html = html.replaceAll("\\s{2,}", "");
		URL url = null;
		Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        
        while (matcher.find())
        {
        	String site = matcher.group(1);
        	if (!site.startsWith("http"))
        	{
        		try
        		{
        			url = new URL(base, site);
        		}
        		catch (MalformedURLException e)
        		{
        			System.out.println("Protocol cannot be found");
        		}
        	}
        	
        	else if (site.startsWith("http"))
        	{
        		try
        		{
        			url = new URL(site);
        		}
        		catch (MalformedURLException e)
        		{
        			System.out.println("HTTP protocol cannot be found");
        		}
        	}
        	
        	if (url.toString().startsWith("http"))
        	{
        		links.add(clean(url).toString());
        	}
        }
		return links;
	}
}