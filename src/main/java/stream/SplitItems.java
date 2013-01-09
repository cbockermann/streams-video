/**
 * 
 */
package stream;

/**
 * @author chris
 * 
 */
public class SplitItems implements Processor {

	String key = "LINE";

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if (!input.containsKey(key)) {
			return input;
		}

		String line = input.remove(key).toString();
		for (String item : line.split(" ")) {
			input.put(item, 1);
		}

		return input;
	}
}
