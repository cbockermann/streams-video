/**
 * 
 */
package stream;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chris
 * 
 */
public class BlockKrimp extends AbstractProcessor {

	List<Data> block = new ArrayList<Data>();

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if (block.size() < 100) {
			block.add(input);
			return input;
		}

		//
		// block enthaelt jetzt 100 items
		//
		for (Data item : block) {
			// berechne tausend mal ueber der liste irgendwas
		}

		block.remove(0);
		block.add(input);

		return input;
	}

}
