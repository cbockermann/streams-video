package stream.image;

import stream.Data;
import stream.annotations.Parameter;

public class DiffImage extends AbstractImageProcessor {

	ImageRGB lastImage = new ImageRGB(0, 0);

	String output = ImageProcessor.DEFAULT_IMAGE_KEY;

	@Parameter(description = "The name/key under which the output image is stored. If this name equals the name of the input image, the input image is going to be overwritten.")
	public void setOutput(String output) {
		this.output = output;
	}

	@Override
	public Data process(Data item, ImageRGB img) {

		ImageRGB diffImage = new ImageRGB(img.getHeight(), img.getWidth());
		for (int i = 0; i < diffImage.getWidth(); i++) {
			for (int j = 0; j < diffImage.getHeight(); j++) {
				diffImage.setRGB(i, j, img.getRGB(i, j));
			}
		}

		if (diffImage.getHeight() == lastImage.getHeight()
				&& diffImage.getWidth() == lastImage.getWidth()) {
			for (int i = 0; i < diffImage.getWidth(); i++) {
				for (int j = 0; j < diffImage.getHeight(); j++) {

					int rgbold = lastImage.getRGB(i, j);
					int rgbnew = diffImage.getRGB(i, j);

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

					if (rdiff < 20) {
						rdiff = 0;
					}
					if (gdiff < 20) {
						gdiff = 0;
					}
					if (bdiff < 20) {
						bdiff = 0;
					}

					// int rgbdiff = rdiff * 65536 + gdiff * 265 + bdiff;
					int rgbdiff = rdiff;
					rgbdiff = (rgbdiff << 8) + gdiff;
					rgbdiff = (rgbdiff << 8) + bdiff;

					diffImage.setRGB(i, j, rgbdiff);
				}
			}
		}

		lastImage = img;

		item.put(output, diffImage);

		return item;
	}

}
