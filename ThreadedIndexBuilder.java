import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.DirectoryStream; 
import java.nio.file.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class that traverses through a directory and builds the index
 * @author Benny Fung
 */
public class ThreadedIndexBuilder
{	
	private static final Logger logger = LogManager.getLogger();
	private final WorkQueue queue;
	
	public ThreadedIndexBuilder(ThreadedInvertedIndex index, WorkQueue queue) 
	{
		super();
		this.queue = queue;
	}
	
	/**
	 * Directory to traverse
	 * @param path
	 * 			path name to take in
	 * @param index
	 * 			threadedInvertedIndex structure
	 * @throws IOException
	 */
	public void traverse(Path path, ThreadedInvertedIndex index) throws IOException 
	{
		traverseHelper(path, index);
		queue.finish();
	}
		
	/**
	 * Takes in a path and traverses through the directory, and calls the buildIndex method if the file extension ends in "HTML"
	 * @param path
	 * 			path name to take in
	 * @param index
	 * 			takes in the threaded inverted index data
	 * @throws IOException
	 */
	private void traverseHelper(Path path, ThreadedInvertedIndex index) throws IOException
	{
		if (Files.isDirectory(path))
		{
			try (DirectoryStream<Path> listing = Files.newDirectoryStream(path))
			{
				for (Path extension : listing)
				{	
					traverseHelper(extension, index);
				}
			}
		}
		
		else if (path.toString().toLowerCase().endsWith("htm") || path.toString().toLowerCase().endsWith("html"))
		{
			queue.execute(new BuildTask(path, index));
		}		
	}
	
	/**
	 * Handles index building, particularly adding the path and contents into the threaded index structure
	 */
	private class BuildTask implements Runnable
	{
		private final ThreadedInvertedIndex index;
		private Path path;
		
		public BuildTask(Path path, ThreadedInvertedIndex index)
		{			
			this.index = index;
			this.path = path;
		}
		
		@Override
		public void run()
		{
			try 
			{
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilderHTML.buildIndex(path, local);
				index.addAll(local);	
			} 
			catch (IOException e) 
			{
				logger.debug("Unable to build to index");
			}
		}
	}
}