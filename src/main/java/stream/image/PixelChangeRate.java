/**
 * 
 */
package stream.image;

import stream.Data;

/**
 * @author chris
 * 
 */
public class PixelChangeRate extends AbstractImageProcessor {

	ImageRGB last = null;
	Integer threshold = 0;

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      stream.image.ImageRGB)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		if (last == null) {
			last = img;
		}

		Integer total = last.pixels.length;
		Integer differ = 0;
		int min = 255;
		int max = 0;

		for (int i = 0; i < last.pixels.length; i++) {
			int rgbold = last.pixels[i];
			int rgbnew = img.pixels[i];

			int rold = (rgbold >> 16) & 0xFF;
			int gold = (rgbold >> 8) & 0xFF;
			int bold = rgbold & 0xFF;

			int rnew = (rgbnew >> 16) & 0xFF;
			int gnew = (rgbnew >> 8) & 0xFF;
			int bnew = rgbnew & 0xFF;

			int rdiff = Math.abs(rold - rnew);
			int gdiff = Math.abs(gold - gnew);
			int bdiff = Math.abs(bold - bnew);

			// rdiff = 255 - rdiff;
			// gdiff = 255 - gdiff;
			// bdiff = 255 - bdiff;

			// int rgbdiff = rdiff * 65536 + gdiff * 265 + bdiff;
			int rgbdiff = rdiff;
			rgbdiff = (rgbdiff << 8) + gdiff;
			rgbdiff = (rgbdiff << 8) + bdiff;

			int diff = rdiff + gdiff + bdiff;
			if (diff > threshold) {
				differ++;
			}

			min = Math.min(min, diff);
			max = Math.max(max, diff);
		}

		item.put("frame:pixels:changed",
				differ.doubleValue() / total.doubleValue());
		item.put("frame:pixels:minDiff", min);
		item.put("frame:pixels:maxDiff", max);
		return item;
	}

	/**
	 * @return the threshold
	 */
	public Integer getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            the threshold to set
	 */
	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}
}