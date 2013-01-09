/**
 * 
 */
package stream.image.features;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * This processor computes the average color value for all three RGB channels
 * 
 * @author chris, Matthias
 */
public class AverageRGB extends AbstractImageProcessor {

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      java.awt.image.BufferedImage)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		double r = 0.0;
		double g = 0.0;
		double b = 0.0;
		double px = img.pixels.length;
		int skipped = 0;

		for (int i = 0; i < img.pixels.length; i++) {
			int argb = img.pixels[i];

			int alpha = (argb >> 24) & 0xff;
			if (alpha == 0) {
				skipped++;
				continue;
			}

			int red = (argb >> 16) & 0xFF;
			int green = (argb >> 8) & 0xFF;
			int blue = argb & 0xFF;

			r += red;
			g += green;
			b += blue;
		}
		
		px -= skipped;
		//log.info("Skipped {} fully transparent pixels", skipped);
		r = r / px;
		g = g / px;
		b = b / px;

		item.put("frame:red:avg", r);
		item.put("frame:blue:avg", b);
		item.put("frame:green:avg", g);
		return item;
	}
}
