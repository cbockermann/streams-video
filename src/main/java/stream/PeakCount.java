/**
 * 
 */
package stream;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class PeakCount implements Processor {

	static Logger log = LoggerFactory.getLogger(PeakCount.class);
	public final static int STATE_BELOW = -1;
	public final static int STATE_ABOVE = 1;
	String[] keys = null;

	int state = 0;
	Double threshold = 100.5d;

	Integer count = 0;

	/**
	 * @return the keys
	 */
	public String[] getKeys() {
		return keys;
	}

	/**
	 * @param keys
	 *            the keys to set
	 */
	public void setKeys(String[] keys) {
		this.keys = keys;
	}

	/**
	 * @return the threshold
	 */
	public Double getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            the threshold to set
	 */
	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if (keys == null)
			return input;

		int below = 0;
		int above = 0;

		for (String key : keys) {
			Serializable val = input.get(key);
			if (val != null) {
				Double value = Double.valueOf(val.toString());
				if (value < threshold) {
					below++;
				} else {
					above++;
				}
			}
		}

		input.put("peak", 0.0d);

		if (below != keys.length && above != keys.length) {
			log.info("No unified peak!");
			return input;
		}

		if (state == STATE_BELOW) {
			input.put("peak", 50.0d);
		}

		if (below == keys.length) {
			// log.info("State was STATE_ABOVE, but value fell below threshold, counting and switching state..");
			input.put("peak", 50.0d);
			if (state == STATE_ABOVE) {
				count++;
				input.put("peak:count", count);
			}
			state = STATE_BELOW;
			return input;
		}

		if (above == keys.length) {
			// log.info("State changed to STATE_ABOVE");
			state = STATE_ABOVE;
		}

		return input;
	}
}