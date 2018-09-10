import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;

/**
 * Class that traverses through a directory and builds the index
 * @author Benny Fung
 */
public class InvertedIndexBuilderHTML
{	
	/**
	 * Takes in a path and traverses through the directory, and calls the buildIndex method if the file extension ends in "HTML"
	 * @param path
	 * 			path name to take in
	 * @param index
	 * 			takes in the inverted index data
	 * @throws IOException
	 */
	private static void traverse(Path path, InvertedIndex index) throws IOException
	{
		if (Files.isDirectory(path))
		{
			try (DirectoryStream<Path> listing = Files.newDirectoryStream(path))
			{
				for (Path extension : listing)
				{	
					traverseDirectory(extension, index);
				}
			}
		}
		
		else if (path.toString().toLowerCase().endsWith("htm") || path.toString().toLowerCase().endsWith("html"))
		{
			buildIndex(path, index);
		}
	}
	
	/**
	 * Directory to traverse
	 * @param path
	 * 			path name to take in
	 * @param index
	 * 			invertedindex structure
	 * @throws IOException
	 */
	public static void traverseDirectory(Path path, InvertedIndex index) throws IOException 
	{
		traverse(path, index);
	}
	
	/** Reads through the file, cleans the HTML scripts, splits, and parses every individual word, and adds it into the data structure.
	 *
	 * @param path
	 * 			path to take in
	 * @param index
	 * 			inverted index structure
	 * @throws IOException
	 */
	public static void buildIndex(Path path, InvertedIndex index) throws IOException
	{
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		String htmlPage = String.join(" ", lines);
		String cleaned = HTMLCleaner.stripHTML(htmlPage);
		String[] words = WordParser.parseWords(cleaned);
		index.addAll(words, path);
	}
}