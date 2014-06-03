/**
 * 
 */
package stream.image.features;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * @author chris
 * 
 */
public class Brightness extends AbstractImageProcessor {

	Double last = null;

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      stream.image.ImageRGB)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		Double totalBrightness = 0.0;
		double cnt = img.pixels.length;

		Double max = null;
		Double min = null;

		for (int i = 0; i < img.pixels.length; i++) {

			int argb = img.pixels[i];

			int red = (argb >> 16) & 0xFF;
			int green = (argb >> 8) & 0xFF;
			int blue = argb & 0xFF;

			double bright = (red + green + blue) / 3;

			if (max == null || max < bright)
				max = bright;

			if (min == null || min > bright)
				min = bright;

			totalBrightness += bright;
		}

		Double b = totalBrightness / cnt;

		item.put("brightness", b);
		if (last != null) {
			item.put("brightness'", b - last);
		}
		last = b;
		item.put("max:brightness", max);
		item.put("min:brightness", min);
		return item;
	}
}
