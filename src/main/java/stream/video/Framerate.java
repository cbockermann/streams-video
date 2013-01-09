/**
 * 
 */
package stream.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;

/**
 * @author chris
 * 
 */
public class Framerate implements Processor {

	static Logger log = LoggerFactory.getLogger(Framerate.class);
	String frame = "data";
	Long first = 0L;
	Long last = 0L;
	Integer count = 0;

	Float fps = 25.0f;
	int frametime = 40;

	/**
	 * @return the fps
	 */
	public Float getFps() {
		return fps;
	}

	/**
	 * @param fps
	 *            the fps to set
	 */
	public void setFps(Float fps) {
		this.fps = fps;

		if (fps > 0) {
			Double frameTime = (1000.0d / fps);
			this.frametime = frameTime.intValue();
		}
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if (first == 0L)
			first = System.currentTimeMillis();

		if (input.containsKey(frame)) {
			count++;

			// Long dur = (System.currentTimeMillis() - first) / 1000L;
			// if (dur > 0) {
			// // double rate = count.doubleValue() / dur.doubleValue();
			// // log.info("Framerate is: {} / second", rate);
			//
			// // if (fps > 0.0) {
			// //
			// // double delayRate = rate - fps;
			// //
			// // Double delay = (delayRate);
			// // log.info("Inserting delay: {}", delay);
			// //
			// // try {
			// // Thread.sleep(delay.intValue());
			// // } catch (Exception e) {
			// // }
			// // }
			// }

			if (last > 0 && fps > 0.0) {

				Long dur = (System.currentTimeMillis() - first) / 1000L;
				double rate = count.doubleValue() / dur.doubleValue();
				// log.info("fps: {}", rate);

				if (rate < fps) {
					// log.info("Dropping frame...");
					last = System.currentTimeMillis();
					return null;
				}

				long ago = System.currentTimeMillis() - last;
				Long delay = frametime - ago;
				// log.info("Last frame was {} ms ago, waiting for {} ms", ago,
				// delay);
				if (delay > 0) {
					sleep(delay);
				}

				Double time = (System.currentTimeMillis() - first) / 1000.0d;
				if (count % 10 == 0)
					log.info("Video position is: {} (calculated: {})", time,
							(count * frametime) / 1000.0d);
			}

			last = System.currentTimeMillis();
		}

		return input;
	}

	public void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
		}
	}
}
