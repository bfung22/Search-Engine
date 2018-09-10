import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Multithreaded webcrawler that processes up to 50 webpages and stores it in the index
 * @author BennyFung
 *
 */
public class WebCrawler 
{
	private WorkQueue queue;
	private ThreadedInvertedIndex index;
	private static final Logger logger = LogManager.getLogger();
	private final ReadWriteLock lock;
	private final HashSet<String> links;
	private int LIMIT;
	
	/**
	 * Initialize index and work queue
	 * @param index
	 * @param queue
	 */
	public WebCrawler(ThreadedInvertedIndex index, WorkQueue queue)
	{
		lock = new ReadWriteLock();
		this.index = index;
		this.queue = queue;
		this.links = new HashSet<>();
		this.LIMIT = 0;
	}
	
	/**
	 * crawler method that checks to see if it's a unique url, then creates workers
	 * @param url
	 * @param limit
	 * @throws MalformedURLException
	 */
	public void crawler(URL url, int limit) throws MalformedURLException 
	{
		LIMIT = limit;
		
		if (!links.contains(url))
		{
			links.add(url.toString());
			queue.execute(new CrawlWorker(url, index, links));
		}
		
		logger.debug("links: " + links);
		queue.finish();
	}
	
	/**
	 * Crawl task that gets all links, creates task if there is a unique link and if it's under the limit
	 * and cleans/parses HTML to words and adds to index
	 * @author BennyFung
	 *
	 */
	private class CrawlWorker implements Runnable
	{
		private URL url;
		private ThreadedInvertedIndex index;
		private HashSet<String> links;
		
		private CrawlWorker(URL url, ThreadedInvertedIndex index, HashSet<String> links)
		{
			this.url = url;
			this.index = index;
			this.links = links;
		}
		
		@Override
		public void run()
		{
			String html = LinkParser.fetchHTML(url);
			try 
			{
				ArrayList<String> listOfLinks = LinkParser.listLinks(url, html);
				for (String link : listOfLinks)
				{
					if (!links.contains(link))
					{
						if (links.size() >= LIMIT)
						{
							break;
						}
						logger.debug("size: " + links.size());
						links.add(link);
						URL url = new URL(link);
						queue.execute(new CrawlWorker(url, index, links));
					}
				}
			}
			catch (MalformedURLException e)
			{
				logger.debug(e.getMessage());
			}

			String cleaned = HTMLCleaner.stripHTML(html);
			String[] words = WordParser.parseWords(cleaned);
			InvertedIndex local = new InvertedIndex();
			int position = 1;
			
			for (String word : words)
			{
				local.add(word, url.toString(), position);
				position++;
			}
			index.addAll(local);
		}
	}
}