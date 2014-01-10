/**
 * 
 */
package stream.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;

/**
 * @author chris
 * 
 */
public class BlackFrameDetector extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(BlackFrameDetector.class);

	String key = "frame:image";
	Double threshold = 0.9d;

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		try {
			ImageRGB image = (ImageRGB) input.get(key);
			if (image != null) {

				int[] pixels = image.pixels;
				Integer total = pixels.length;
				Integer black = 0;

				for (int i = 0; i < total; i++) {
					int argb = pixels[i];

					int r = (0xff0000 & argb) >> 16;
					int g = (0xff00 & argb) >> 8;
					int b = (0xff & argb);

					if (r < 8 & g < 8 & b < 8) {
						black++;
					}
				}

				Double fraction = black.doubleValue() / total.doubleValue();
				input.put("blackRate", 100.0d * fraction);

				if (fraction > threshold) {
					input.put("frame:mark:blackFrame", 500.0);
				} else {
					input.put("frame:mark:blackFrame", 0.0);
				}
			}

		} catch (Exception e) {
			log.error("Error: {}", e.getMessage());
		}

		return input;
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

}
