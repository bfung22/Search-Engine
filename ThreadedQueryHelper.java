import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** 
 * Class that parses queries and writes to JSON format.
 * @author Benny Fung
 */
public class ThreadedQueryHelper implements QueryHelperInterface
{
	private final ThreadedInvertedIndex index;
	private final TreeMap<String, ArrayList<SearchResult>> map;
	private final WorkQueue queue;
	private final ReadWriteLock lock;
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Initialize the index and work queue
	 * @param index
	 * @param queue
	 */
	public ThreadedQueryHelper(ThreadedInvertedIndex index, WorkQueue queue) 
	{
		map = new TreeMap<>();
		this.queue = queue;
		this.index = index;
		lock = new ReadWriteLock();
	}

	/**
	 * Method that parses queries by reading the file line by line and calling the appropriate exact/partial search method
	 * @param path
	 * 			path to input
	 * @param exact
	 * 			boolean exact which is used to later call the exact/partial search methods accordingly
	 * @throws IOException
	 */
	@Override
	public void parseQuery(Path path, boolean exact) throws IOException
	{
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);)
		{	
			String line;
			while ((line = reader.readLine()) != null)
			{
				queue.execute(new SearchTask(line, map, index, exact));
			}
			queue.finish();
		}
	}
	
	/**
	 * Task that handles reading through query files, and calling the search method accordingly
	 */
	private class SearchTask implements Runnable
	{
		private String line;
		private boolean exact;
		
		public SearchTask(String line, TreeMap<String, ArrayList<SearchResult>> map, ThreadedInvertedIndex index, boolean exact)
		{
			this.line = line;
			this.exact = exact;
		}
		
		@Override
		public void run()
		{
			String[] words = WordParser.parseWords(line);
			if (words.length == 0) 
			{
				return; 
			}
			
			Arrays.sort(words);
			line = String.join(" ", words);
			
			ArrayList<SearchResult> local = exact ? index.exactSearch(words) : index.partialSearch(words);
			
			lock.lockReadWrite();
			try
			{
				map.put(line, local);
			}
			finally
			{
				lock.unlockReadWrite();
			}
			logger.debug("map: " + map);
		}
	}
	
	/**
	 * Method that writes the data structure to JSON format by calling the JSON method.
	 * @param path
	 * 			path to input
	 * @throws IOException
	 */
	@Override
	public void toJSON(Path path) throws IOException 
	{
		lock.lockReadOnly();
		try
		{
			JSONWriter.toSearchFormat(map, path);
		}
		finally
		{
			lock.unlockReadOnly();
		}
	}
}