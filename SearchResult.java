/**
 * SearchResult class that contains the compareTo method that sorts search results by frequency, position, and path.
 * @author Benny Fung 
 *
 */
public class SearchResult implements Comparable<SearchResult>
{
	private int frequency;
	private int initialPosition;
	private final String path;
	
	/**
	 * SearchResult constructor
	 * @param path
	 * 			input path
	 * @param frequency
	 * 			input frequency
	 * @param initialPosition
	 * 			inputposition
	 */
	public SearchResult(String path, int frequency, int initialPosition)
	{
		this.path = path;
		this.frequency = frequency;
		this.initialPosition = initialPosition;
	}
	
	/**
	 * Getter for frequency
	 * @return
	 */
	public int getFrequency()
	{
		return this.frequency;
	}
	
	/**
	 * getter for position
	 * @return
	 */
	public int getPosition()
	{
		return this.initialPosition;
	}
	
	/**
	 * getter for path
	 * @return
	 */
	public String getPath()
	{
		return this.path;
	}
	
	/**
	 * update method that updates the position and frequency
	 * @param updatedFrequency
	 * @param updatedPosition
	 */
	public void update(int updatedFrequency, int updatedPosition)
	{
		if (updatedPosition < initialPosition)
		{
			initialPosition = updatedPosition;
		}

		frequency += updatedFrequency;
	}
	
	
	@Override
	/**
	 * Sorting method that sorts accordingly by path, positions, and frequency
	 */
	public int compareTo(SearchResult other)
	{
		if (Integer.compare(this.frequency, other.frequency) == 0)
		{
			if (Integer.compare(this.initialPosition, other.initialPosition) == 0)
			{
				return this.path.compareTo(other.path);
			}
			
			return Integer.compare(this.initialPosition, other.initialPosition);
		}
		return Integer.compare(other.frequency, this.frequency);
	}
	
	/**
	 * toString method that prints out search results
	 */
	public String toString()
	{
		return "Path: " + this.path + " Count: " + this.frequency + " Position: " + this.initialPosition;
	}
}
