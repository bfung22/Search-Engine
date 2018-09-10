import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

public class Driver 
{
	/**
	 * Initializes and runs the program
	 * @param args
	 * 			takes in arguments
	 * @throws IOException
	 * @author Benny Fung
	 */
	public static void main(String[] args)
	{
		ArgumentMap argument = new ArgumentMap(args);
		InvertedIndex invertedIndex = null;
		QueryHelperInterface queryHelper = null;
		WorkQueue queue = null;
		int threads;
		int total;
		
		try 
		{
			threads = Integer.parseInt(argument.getValue("-threads"));
			if (threads <= 0)
			{
				threads = 5;
			}
		} 
		catch (NumberFormatException e) 
		{
			threads = 5;
		}
		
		try
		{
			total = Integer.parseInt(argument.getValue("-limit"));
			if (total <= 0)
			{
				total = 50;
			}
		}
		catch (NumberFormatException e)
		{
			total = 50;
		}
				
		if (argument.hasFlag("-threads") && argument.hasValue("-threads"))
		{
			queue = new WorkQueue(threads);
			ThreadedInvertedIndex threadSafeIndex = new ThreadedInvertedIndex();
			invertedIndex = threadSafeIndex;
			queryHelper = new ThreadedQueryHelper(threadSafeIndex, queue);
						
			if (argument.hasFlag("-path") && argument.hasValue("-path"))
			{
				ThreadedIndexBuilder builder = new ThreadedIndexBuilder(threadSafeIndex, queue);
				try 
				{
					builder.traverse(Paths.get(argument.getValue("-path")), threadSafeIndex);
					
				}
				catch (IOException e)
				{
					System.out.println("Unable to build index from the path" + argument.getString("-path"));
				}
			}

		}
		
		else
		{
			invertedIndex = new InvertedIndex();
			queryHelper = new QueryHelper(invertedIndex);
			
			if (argument.hasFlag("-path") && argument.hasValue("-path"))
			{	
				try 
				{
					InvertedIndexBuilderHTML.traverseDirectory(Paths.get(argument.getValue("-path")), invertedIndex);
				}
				
				catch (IOException e)
				{
					System.out.println("Unable to build index from the path" + argument.getString("-path"));
				}
			}
		}
		
		if (argument.hasFlag("-url") && argument.hasValue("-url"))
		{
			queue = new WorkQueue();
			ThreadedInvertedIndex threadedIndex = new ThreadedInvertedIndex();
			WebCrawler crawler = new WebCrawler(threadedIndex, queue);
			invertedIndex = threadedIndex;
			queryHelper = new QueryHelper(invertedIndex);

			try 
			{
				URL urlFlag = new URL(argument.getValue("-url"));
				crawler.crawler(urlFlag, total);
			} 
			catch (MalformedURLException e) 
			{
				System.out.println("unable to read URL");
			}	
		}
		
		if (argument.hasFlag("-query") && argument.hasValue("-query"))
		{
			try
			{
				queryHelper.parseQuery(Paths.get(argument.getValue("-query")), argument.hasFlag("-exact"));
			}
			catch(IOException e)
			{
				System.out.println("Unable to read query file");
			}
		}
		
		String output = argument.getString("-index", "index.json");
		if (argument.hasFlag("-index"))
		{
			try
			{
				invertedIndex.writeToJSON(Paths.get(output));
			}
			catch (IOException e)
			{
				System.out.println("Unable to write index to the path" + output);
			}
		}
		
		String results = argument.getString("-results", "results.json");
		if (argument.hasFlag("-results")) 
		{
			try
			{
				queryHelper.toJSON(Paths.get(results));
			}
			catch(IOException e)
			{
				System.out.println("Unable to write results to path" + results);
			}
		}
		
		if (queue != null)
		{
			queue.shutdown();
		}
	}
}