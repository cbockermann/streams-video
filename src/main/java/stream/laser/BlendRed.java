/**
 * 
 */
package stream.laser;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * @author chris
 * 
 */
public class BlendRed extends AbstractImageProcessor {

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      stream.image.ImageRGB)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		for (int i = 0; i < img.pixels.length; i++) {
			int argb = img.pixels[i];
			img.pixels[i] = 0xffff0000 & argb;
		}

		item.put(getImage(), img);
		return item;
	}

}
