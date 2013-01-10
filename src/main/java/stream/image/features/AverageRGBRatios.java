/**
 * 
 */
package stream.image.features;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * @author chris
 * @deprecated Please use the AverageRGB processor with includeRatios="true" instead.
 * 
 */
public class AverageRGBRatios extends AbstractImageProcessor {

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      java.awt.image.BufferedImage)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		int width = img.getWidth();
		int height = img.getHeight();
		double r = 0.0;
		double g = 0.0;
		double b = 0.0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				int rgb = img.getRGB(x, y);
				// int alpha = (rgb >> 24) & 0xFF;
				int red = (rgb >> 16) & 0xFF;
				int green = (rgb >> 8) & 0xFF;
				int blue = rgb & 0xFF;

				r += red;
				g += green;
				b += blue;
			}
		}

		int px = width * height;
		r = r / px;
		g = g / px;
		b = b / px;

		item.put("frame:red:blue", r / b);
		item.put("frame:red:green", r / g);
		item.put("frame:green:blue", g / b);
		return item;
	}
}
