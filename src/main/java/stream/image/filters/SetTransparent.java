/**
 * 
 */
package stream.image.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * @author chris
 * 
 */
public class SetTransparent extends AbstractImageProcessor {

	static Logger log = LoggerFactory.getLogger(SetTransparent.class);

	// String color = "000000";

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      stream.image.ImageRGB)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		int black = 0x000000;
		// int c = black;
		int transBlack = 0x00000000;
		int transparent = 0;

		for (int i = 0; i < img.pixels.length; i++) {
			if (img.pixels[i] == black) {
				img.pixels[i] = transBlack;
				transparent++;
			}
		}

		log.info("{} of {} pixels set to full transparency", transparent,
				img.pixels.length);
		return item;
	}
}
