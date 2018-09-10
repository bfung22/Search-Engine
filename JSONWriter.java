import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class that contains methods to print in JSON format
 * @author Benny Fung
 */
public class JSONWriter
{		
	/**
	 * Returns a String with the specified number of tab characters.
	 *
	 * @param times
	 *            number of tab characters to include
	 * @return tab characters repeated the specified number of times
	 */
	public static String indent(int times)
	{
		char[] tabs = new char[times];
		Arrays.fill(tabs, '\t');
		return String.valueOf(tabs);
	}

	/**
	 * Returns a quoted version of the provided text.
	 *
	 * @param text
	 *            text to surround in quotes
	 * @return text surrounded by quotes
	 */
	public static String quote(String text)
	{
		return String.format("\"%s\"", text);
	}
	
	/**
	 * Writes the set of elements as a JSON array at the specified indent level.
	 *
	 * @param writer
	 *            writer to use for output
	 * @param elements
	 *            elements to write as JSON array
	 * @param level
	 *            number of times to indent the array itself
	 * @throws IOException
	 */
	private static void asArray(Writer writer, TreeSet<Integer> elements, int level) throws IOException
	{	
		Iterator<Integer> iterator = elements.iterator();
		writer.write("[");
		
		if (iterator.hasNext())
		{
			writer.write("\n");
			writer.write(indent(1));
			writer.write(iterator.next().toString());
		}

		while (iterator.hasNext()) 
		{
			writer.write(",\n");
			writer.write(indent(1));
			writer.write(iterator.next().toString());
		}

		writer.write("\n");
		writer.write(']');
		writer.flush();
	}
	
	/**
	 * Writes the set of elements as a JSON object with a nested array to the
	 * path using UTF8.
	 *
	 * @param elements
	 *            elements to write as a JSON object with a nested array
	 * @param path
	 *            path to write file
	 * @throws IOException
	 */
	private static void asNestedObject(TreeMap<String, TreeSet<Integer>> elements, BufferedWriter writer, int level) throws IOException 
	{
		int bracketCommaCount = 0;
		for (String i : elements.keySet())
		{
			writer.write(indent(level));
			writer.write(quote(i) + ": ");
			if (elements.isEmpty())
			{
				asArray(writer, elements.get(i), 0);
			}
			
			asArray(writer, elements.get(i), level);
			
			
			while (bracketCommaCount < elements.size() - 1)
			{
				bracketCommaCount++;
				writer.write(",");
				break;
			}
			writer.write("\n");
		}
		writer.flush();
	}
	
	@SuppressWarnings("unused")
	/**
	 * Writes the set of elements as a JSON object with a double nested array to the
	 * path using UTF8.
	 *
	 * @param elements
	 *            elements to write as a JSON object with a nested array
	 * @param path
	 *            path to write file
	 * @throws IOException
	 */
	public static void asDoubleNestedObject(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, Path path) throws IOException
	{
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
		{
			int count = 0;
			writer.write("{\n");
			for (String word : elements.keySet())
			{
				writer.write(indent(1) + quote(word) + ": {\n");
				for (String extension : elements.get(word).keySet())
				{
					JSONWriter.asNestedObject(elements.get(word), writer, 2);
					writer.write(indent(1) + "}");
					while (count < elements.size()-1)
					{
						count++;
						writer.write(",\n");
						break;
					}
					break;
				}
			}
			writer.write("\n}");
		 }
	}
	
	/**
	 * helper method for JSON writer
	 * @param writer
	 * @param elements
	 * 			takes in the data structure
	 * @throws IOException
	 */
	private static void asResultsArray(BufferedWriter writer, ArrayList<SearchResult> elements) throws IOException
	{
		int num = 0;
		writer.write(indent(2) + quote("results") + ": [\n");
		for (SearchResult searchResult : elements)
		{
			writer.write(indent(3) + "{\n");
			writer.write(indent(4) + quote("where") + ": " + quote(searchResult.getPath()) + ",\n");
			writer.write(indent(4) + quote("count") + ": " + searchResult.getFrequency() + ",\n");
			writer.write(indent(4) + quote("index") + ": " + searchResult.getPosition() + "\n");
			writer.write(indent(3) + "}");
			
			int searchCount = elements.size();
			while (num < searchCount-1)
			{
				writer.write(",\n");
				num++;
				break;
			}
		}
		
		if (elements.size() == 0)
		{
			writer.write(indent(2) + "]");
			
		}
		else
		{
			writer.write("\n" + indent(2) + "]");
			writer.flush();
		}
	}
	
	/**
	 * Prints into JSON format given the raw data structure and path
	 * @param elements
	 * 			takes in the data structure
	 * @param path
	 * 			takes in the path
	 * @throws IOException
	 */
	public static void toSearchFormat(TreeMap<String, ArrayList<SearchResult>> elements, Path path) throws IOException
	{
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
		{
			writer.write("[");
			
			int count = 0;
			int numOfQueries = elements.size();
			for (String q : elements.keySet())
			{			
				writer.write("\n" + indent(1) + "{\n");
				writer.write(indent(2) + quote("queries") + ": " + quote(q) + ",\n");
				JSONWriter.asResultsArray(writer, elements.get(q));
				
				writer.write("\n" + indent(1) + "}");
				while (count < numOfQueries-1)
				{
					writer.write(",");
					count++;
					break;
				}
			}
			writer.write("\n]");
		}
	}
}