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
public class MaxRGB extends AbstractImageProcessor {

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      stream.image.ImageRGB)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		Integer maxRed = null;
		Integer maxGreen = null;
		Integer maxBlue = null;

		for (int i = 0; i < img.pixels.length; i++) {
			int argb = img.pixels[i];

			int red = (argb >> 16) & 0xFF;
			int green = (argb >> 8) & 0xFF;
			int blue = argb & 0xFF;

			if (maxRed == null || maxRed < red) {
				maxRed = red;
			}

			if (maxGreen == null || maxGreen < green) {
				maxGreen = green;
			}

			if (maxBlue == null || maxBlue < blue) {
				maxBlue = blue;
			}
		}

		item.put("max:red", maxRed.doubleValue());
		item.put("max:green", maxGreen.doubleValue());
		item.put("max:blue", maxBlue.doubleValue());

		return item;
	}

}
