/**
 * 
 */
package stream.image.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * @author chris
 * 
 */
public class MnistFeatures extends AbstractImageProcessor {

	static Logger log = LoggerFactory.getLogger(MnistFeatures.class);
	int limit = 25 * 25;

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      stream.image.ImageRGB)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		int dim = img.getWidth() * img.getHeight();
		log.info("Creating {} features", dim);
		if (dim > limit) {

		}

		int[] rgb = img.getPixels();
		double[] features = new double[dim];

		double cx = 0.0;
		double cy = 0.0;
		double cnt = 0.0;

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int idx = y * img.getWidth() + x;
				if (rgb[idx] > 0) {
					cy += y;
					cx += x;
					cnt += 1.0d;
				}
			}
		}

		cx = cx / cnt;
		cy = cy / cnt;

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int idx = y * img.getWidth() + x;
				if (rgb[idx] > 0) {
					// features[i] = Math.sqrt( )
					cy += y;
					cx += x;
					cnt += 1.0d;
				}
			}
		}

		for (int i = 0; i < features.length; i++) {
			if (features[i] > 0.0)
				item.put(String.format("mnist:%d", i), new Double(features[i]));
		}

		return item;
	}
}
