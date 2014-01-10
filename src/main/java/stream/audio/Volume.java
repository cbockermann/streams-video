/**
 * 
 */
package stream.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;
import stream.ProcessContext;

/**
 * @author chris
 * 
 */
public class Volume extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(Volume.class);

	String key = "wav:samples";
	int windowSize = 25;

	double[] window = new double[windowSize];
	int idx = 0;
	double current = 0;

	/**
	 * @see stream.AbstractProcessor#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);

		window = new double[windowSize];
		for (int i = 0; i < window.length; i++) {
			window[i] = 0.0;
		}
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		try {

			double[] samples = (double[]) input.get(key);

			double avg = 0.0;
			for (int i = 0; i < samples.length; i++) {
				avg = Math.max(avg, samples[i]);
			}

			// avg = avg / samples.length;

			current += avg;
			current -= window[(idx + 1) % window.length];
			window[(idx + 1) % window.length] = avg;

			input.put("wav:volume", current / window.length);

			idx++;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return input;
	}

	/**
	 * @return the windowSize
	 */
	public int getWindowSize() {
		return windowSize;
	}

	/**
	 * @param windowSize
	 *            the windowSize to set
	 */
	public void setWindowSize(Integer windowSize) {
		this.windowSize = windowSize;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
}