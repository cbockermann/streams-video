/**
 * 
 */
package stream.image.features;

import stream.Data;
import stream.annotations.Parameter;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * This processor computes the average color value for all three RGB channels.
 * 
 * @author chris, Matthias
 */
public class AverageRGB extends AbstractImageProcessor {

//	static Logger log = org.slf4j.LoggerFactory.getLogger(AverageRGB.class);
	Boolean includeRatios = false;
	
	/**
	 * If desired, the processor includes the ration between the color channels. Ratios are
	 * { (red/blue), (red/green), (green/blue) }.
	 * 
	 * @param includeRatios
	 */
	@Parameter(description="Sets, if the processor includes the ration between the color channels, or just the average RGB color values. Ratios are (red/blue), (red/green), (green/blue).",defaultValue="false")
	public void setIncludeRatios(boolean includeRatios) {
		this.includeRatios = includeRatios;
	}
	
	/**
	 * Delivers, if the processor is including the ratios or not.
	 * @return
	 */
	public Boolean getIncludeRatios() {
		return includeRatios;
	}
	
	@Override
	public Data process(Data item, ImageRGB img) {

		double r = 0.0;
		double g = 0.0;
		double b = 0.0;
		double px = img.pixels.length;
//		int skipped = 0;

		for (int i = 0; i < img.pixels.length; i++) {
			int argb = img.pixels[i];

//			int alpha = (argb >> 24) & 0xff;
//			if (alpha == 0) {
//				skipped++;
//				continue;
//			}

			int red = (argb >> 16) & 0xFF;
			int green = (argb >> 8) & 0xFF;
			int blue = argb & 0xFF;

			r += red;
			g += green;
			b += blue;
		}
		
//		px -= skipped;
//		log.info("Skipped {} fully transparent pixels", skipped);
		r = r / px;
		g = g / px;
		b = b / px;

		item.put("frame:red:average", r);
		item.put("frame:blue:average", b);
		item.put("frame:green:average", g);
		
		if (includeRatios) {
			item.put("frame:ratio:red_blue", r / b);
			item.put("frame:ratio:red_green", r / g);
			item.put("frame:ratio:green_blue", g / b);
		}
		return item;
	}
}
