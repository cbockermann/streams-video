/**
 * 
 */
package stream.laser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;

/**
 * @author chris
 * 
 */
public class FrameRate extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(FrameRate.class);
	Long count = 0L;
	Long start = 0L;

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if (start == 0L)
			start = System.currentTimeMillis();

		if (input != null) {
			count++;

			if (count % 100 == 0) {
				Long time = System.currentTimeMillis() - start;
				// log.info("Frame rate after {} frames is: {} fps", count,
				// count.doubleValue() / (time.doubleValue() / 1000.0d));
				System.out.println("Frame rate after " + count + " frames is: "
						+ count.doubleValue() / (time.doubleValue() / 1000.0d)
						+ " fps");
				System.out.println(count + " " + count.doubleValue()
						/ (time.doubleValue() / 1000.0d));

			}
		}

		return input;
	}
}