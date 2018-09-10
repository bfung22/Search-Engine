import java.io.IOException; 
import java.nio.file.Path;
import java.util.ArrayList;

/**A threaded invertedindex that extends the original index class 
 * @author Benny Fung
 */
public class ThreadedInvertedIndex extends InvertedIndex
{
	private final ReadWriteLock lock;
	
	public ThreadedInvertedIndex()
	{
		super();
		lock = new ReadWriteLock();
	}
	
	/**
	 * Adds the word and the paths as well as the position it was found to the index.
	 * 
	 * @param word
	 * 			takes in a word to add
	 * @param path
	 * 			takes in a path to add
	 * @param positionOfWord 
	 * 			takes in the position of the word and path to add
	 */
	public void add(String word, String path, int position)
	{
		lock.lockReadWrite();
		try
		{
			super.add(word, path, position);	
		}
		
		finally
		{
			lock.unlockReadWrite();
		}
	}
	
	/**
	 * search method that takes in a query and searches through the index for an exact match
	 * @param query
	 * 			each individual query
	 * @return
	 * 		returns a list of sorted exact search results
	 */
	public ArrayList<SearchResult> exactSearch(String[] words)
	{
		lock.lockReadOnly();
		try
		{
			return super.exactSearch(words);
		}
		
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	/**
	 * search method that takes in a query and searches through the index for a partial match 
	 * @param query
	 * 			each individual query
	 * @return
	 * 		returns a list of sorted partial search results
	 */
	public ArrayList<SearchResult> partialSearch(String words[])
	{
		lock.lockReadOnly();
		try
		{
			return super.partialSearch(words);
		}
		
		finally
		{
			lock.unlockReadOnly();
		}
	}

	/**
	 * Adds the array of words at once, assuming the first word in the array is
	 * at position 1.
	 *
	 * @param words
	 *            array of words to add
	 *
	 * @see #addAll(String[], int)
	 */
	public void addAll(String[] words, Path path)
	{
		lock.lockReadWrite();
		try
		{	
			int position = 1;
			for (String i : words)
			{
				super.add(i, path.toString(), position);
				position++;
			} 
		}
		
		finally
		{
			lock.unlockReadWrite();
		}
	}
	
	/**
	 * addAll method for the threaded index;
	 */
	public void addAll(InvertedIndex other)
	{
		lock.lockReadWrite();
		try
		{	
			super.addAll(other);
		}
		
		finally
		{
			lock.unlockReadWrite();
		}
	}
	
	/**
	 * calls JSONWriter method "asNestedObject" to convert raw data structure to JSON format
	 * 
	 * @param path
	 * 			path to add
	 * @throws IOException
	 */
	public void writeToJSON(Path path) throws IOException
	{		
		lock.lockReadOnly();
		try
		{
			super.writeToJSON(path);
		}
		
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	/**
	 * Returns the number of words stored in the index.
	 *
	 * @return number of words
	 */
	public int words()
	{
		lock.lockReadOnly();
		try
		{
			return super.words();
		}
		
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	/**
	 * Tests whether the index contains the specified word.
	 *
	 * @param word
	 *            word to look for
	 * @return true if the word is stored in the index
	 */
	public boolean contains(String word)
	{
		lock.lockReadOnly();
		try
		{
			return super.contains(word);
		}
		
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	/**
	 * Returns the number of unique flags stored in the argument map.
	 *
	 * @return number of flags
	 */
	public int numFlags()
	{
		lock.lockReadOnly();
		try
		{
			return super.numFlags();
		}
		
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	/**
	 * returns true if word and path is stored in the index
	 * @param word
	 * 			word to check
	 * @param path
	 * 			path to check
	 * @return
	 */
	public boolean contains(String word, String path)
	{
		lock.lockReadOnly();
		try
		{
			return super.contains(word, path);
		}
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	/**
	 * returns true if index contains word, path, and position
	 * @param word
	 * 			word to check
	 * @param path
	 * 			path to check
	 * @param position
	 * @return
	 */
	public boolean contains(String word, String path, int position)
	{
		lock.lockReadOnly();
		try
		{
			return super.contains(word, path, position);
		}
		
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	/**
	 * Returns a string representation of this index.
	 */
	public String toString() 
	{
		lock.lockReadOnly();
		try
		{
			return super.toString();
		}
		finally
		{
			lock.unlockReadOnly();
		}
	}
}