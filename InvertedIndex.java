import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet; 

/**
 * Creates an InvertedIndex of a TreeMap which contains methods useful to 
 * @author Benny Fung
 */
public class InvertedIndex
{
	/**
	 * Stores a mapping of words to the paths and the positions the words were found.
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;
	/**
	 * Initializes the index.
	 */
	public InvertedIndex()
	{
		index = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
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
		if (!index.containsKey(word))
		{
			index.put(word, new TreeMap<String, TreeSet<Integer>>());
		}
		
		if (!index.get(word).containsKey(path))
		{
			index.get(word).put(path, new TreeSet<Integer>());
		}
		index.get(word).get(path).add(position);
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
		HashMap<String, SearchResult> searchMap = new HashMap<>();
		ArrayList<SearchResult> exactSearchResults = new ArrayList<SearchResult>();
		
		for (String word : words)
		{
			if (index.containsKey(word))
			{	
				searchHelper(word, exactSearchResults, searchMap);
			}
		}
		Collections.sort(exactSearchResults);
		return exactSearchResults;
	}
	
	/**
	 * searchHelper for the partialSearchResults method
	 * @param word
	 * 			word to input
	 * @param searchResults
	 * 			the searchResults ArrayLists 
	 * @param searchMap
	 * 			the hashMap of SearchResults
	 */
	private void searchHelper(String word, ArrayList<SearchResult> searchResults, HashMap<String, SearchResult> searchMap)
	{
		TreeMap<String, TreeSet<Integer>> pathAndPositions = index.get(word);
		for (String path : pathAndPositions.keySet())
		{
			TreeSet<Integer> positions = pathAndPositions.get(path);
			int frequency = positions.size();
			int initialPosition = positions.first();
			
			if (searchMap.containsKey(path))
			{
				searchMap.get(path).update(frequency, initialPosition);
			}
			else
			{
				searchMap.put(path, new SearchResult(path, frequency, initialPosition));
				searchResults.add(searchMap.get(path));
			}
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
		HashMap<String, SearchResult> searchMap = new HashMap<>();
		ArrayList<SearchResult> partialSearchResults = new ArrayList<SearchResult>();
		
		for (String partialWord : words)
		{
			for (String word : index.tailMap(partialWord).keySet())
			{	
				if (word.startsWith(partialWord))
				{
					searchHelper(word, partialSearchResults, searchMap);
				}
				else
				{
					break;
				}
			}
		}
		Collections.sort(partialSearchResults);
		return partialSearchResults;
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
		int position = 1;
		for (String i : words)
		{
			add(i, path.toString(), position);
			position++;
		}
	}

	/**
	 * addAll method for the multithreaded invertedindex
	 * @param other
	 */
	public void addAll(InvertedIndex other)
	{
		for (String word : other.index.keySet())
		{
			if (this.index.containsKey(word) == false)
			{
				this.index.put(word, other.index.get(word));
			}
			else
			{
				for (String path : other.index.get(word).keySet())
				{	
					if (!this.index.get(word).keySet().contains(path))
					{
						this.index.get(word).put(path, other.index.get(word).get(path));						
					}
					else
					{
						this.index.get(word).get(path).addAll(other.index.get(word).get(path));
					}
				}
			}
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
		JSONWriter.asDoubleNestedObject(index, path);
	}
	
	/**
	 * Returns the number of words stored in the index.
	 *
	 * @return number of words
	 */
	public int words()
	{
		return index.size();
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
		return index.containsKey(word);
	}
	
	/**
	 * Returns the number of unique flags stored in the argument map.
	 *
	 * @return number of flags
	 */
	public int numFlags()
	{
		return index.size();
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
		return index.containsKey(word) && index.get(word).containsKey(path);
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
		return index.containsKey(word) &&index.get(word).containsKey(path) && index.get(word).get(path).contains(position);
	}
	
	/**
	 * Returns a string representation of this index.
	 */
	public String toString() 
	{
		return index.toString();
	}
}