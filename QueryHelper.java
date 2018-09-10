import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

/** 
 * Class that parses queries and writes to JSON format; Interface class overrides the methods
 * @author Benny Fung
 *
 */
public class QueryHelper implements QueryHelperInterface
{
	private final InvertedIndex index;
	private final TreeMap<String, ArrayList<SearchResult>> map;
	/**
	 * Initialize the index
	 * @param index
	 */
	public QueryHelper(InvertedIndex index) 
	{
		this.index = index;
		this.map = new TreeMap<>();
	}

	/**
	 * Overridden by Interface class
	 */
	@Override
	public void parseQuery(Path path, boolean exact) throws IOException
	{
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);)
		{	
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] words = WordParser.parseWords(line);
				Arrays.sort(words);
				
				if (words.length == 0) 
				{
					continue; 
				}
				
				line = String.join(" ", words);
				ArrayList<SearchResult> local = exact ? index.exactSearch(words) : index.partialSearch(words);
				map.put(line, local);
			}
		}
	}
	
	/**
	 * Overridden by Interface class
	 */
	@Override
	public void toJSON(Path path) throws IOException 
	{
		JSONWriter.toSearchFormat(map, path);
	}
}
