/**
 * 
 */
package stream.image;

import java.awt.image.BufferedImage;

import stream.Data;

/**
 * @author chris
 * 
 */
public class AverageRGB extends AbstractImageProcessor {

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      java.awt.image.BufferedImage)
	 */
	@Override
	public Data process(Data item, BufferedImage img) {

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

		item.put("frame:red:avg", r);
		item.put("frame:blue:avg", b);
		item.put("frame:green:avg", g);
		return item;
	}
}
