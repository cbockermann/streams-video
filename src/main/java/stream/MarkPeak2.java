/**
 * 
 */
package stream;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.annotations.Parameter;

/**
 * @author Christian Bockermann
 * 
 */
public class MarkPeak2 implements Processor {

	static Logger log = LoggerFactory.getLogger(MarkPeak2.class);
	public final static int STATE_BELOW = -1;
	public final static int STATE_ABOVE = 1;

	protected String[] keys = null;
	protected int state = 1;
	protected Double threshold = 100.5d;
	protected Double value = 255.0d;

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
	@Parameter(description = "A list of attributes that need to exceed the threshold to make the data item a peak.")
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
	@Parameter(description = "The threshold that needs to be traversed for a peak.")
	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return the value
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	@Parameter(required = false, description = "The value that is added to the data item to mark the peak, defaults to '1.0'")
	public void setValue(Double value) {
		this.value = value;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if (keys == null || keys.length == 0)
			return input;

		int below = 0;
		int above = 0;

		for (String key : keys) {
			Serializable val = input.get(key);
			if (val != null) {
				Double value = Double.valueOf(val.toString());
				if (value > threshold) {
					above++;
				} else {
					below++;
				}
			}
		}

		// log.info("{} out of {} keys have values above threshold!", above,
		// keys.length);
		input.put("peak", 0.0d);

		if (above != keys.length && above != keys.length) {
			// log.info("No unified peak!");
			return input;
		}

		if (state == STATE_BELOW && above == keys.length) {
			log.info("State was STATE_BELOW, but value fell below threshold, counting and switching state..");
			input.put("peak", 0.0d);
			state = STATE_BELOW;
			return input;
		}

		if (state == STATE_ABOVE && below == keys.length) {
			log.info("Switching state from STATE_ABOVE to STATE_BELOW");
			state = STATE_BELOW;
		}

		if (state == STATE_BELOW) {
			input.put("peak", value);
		} else {
			input.put("peak", 0.0d);
		}

		return input;
	}
}