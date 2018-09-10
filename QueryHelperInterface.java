import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface class for QueryHelper
 * @author BennyFung
 */
public interface QueryHelperInterface 
{
	/**
	 * Method that parses queries by reading the file line by line and calling the appropriate exact/partial search method
	 * @param path
	 * 			path to input
	 * @param exact
	 * 			boolean exact which is used to later call the exact/partial search methods accordingly
	 * @throws IOException
	 */
	public void parseQuery(Path path, boolean exact) throws IOException;
	
	/**
	 * Method that writes the data structure to JSON format by calling the JSON method.
	 * @param path
	 * 			path to input
	 * @throws IOException
	 */
	public void toJSON(Path path) throws IOException;
}
