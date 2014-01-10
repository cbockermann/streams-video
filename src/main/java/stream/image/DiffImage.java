package stream.image;

import stream.Data;
import stream.annotations.Parameter;

public class DiffImage extends AbstractImageProcessor {

	ImageRGB lastImage = null;

	String output = ImageProcessor.DEFAULT_IMAGE_KEY;
	Integer threshold = 20;

	@Parameter(description = "The name/key under which the output image is stored. If this name equals the name of the input image, the input image is going to be overwritten.")
	public void setOutput(String output) {
		this.output = output;
	}

	@Override
	public Data process(Data item, ImageRGB img) {

		ImageRGB diffImage = new ImageRGB(img.width, img.height);
		for (int i = 0; i < diffImage.pixels.length; i++) {
			diffImage.pixels[i] = img.pixels[i];
		}

		if (lastImage == null) {
			lastImage = img;
		}

		for (int i = 0; i < lastImage.pixels.length; i++) {
			int rgbold = lastImage.pixels[i];
			int rgbnew = diffImage.pixels[i];

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

			if (rdiff < threshold) {
				rdiff = 0;
			}
			if (gdiff < threshold) {
				gdiff = 0;
			}
			if (bdiff < threshold) {
				bdiff = 0;
			}

			// int rgbdiff = rdiff * 65536 + gdiff * 265 + bdiff;
			int rgbdiff = rdiff;
			rgbdiff = (rgbdiff << 8) + gdiff;
			rgbdiff = (rgbdiff << 8) + bdiff;

			diffImage.pixels[i] = rgbdiff;
		}

		lastImage = img;

		item.put(output, diffImage);
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
