package stream.laser;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

public class TurboDiffImage extends AbstractImageProcessor {

	protected ImageRGB lastImage = new ImageRGB(0, 0);

	int threshold;

	public TurboDiffImage() {
		threshold = -1;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public Data process(Data item, ImageRGB img) {

		ImageRGB diffImage = copy(img);

		if (diffImage.height == lastImage.height
				&& diffImage.width == lastImage.width) {

			int off = 0;
			int max = diffImage.width * diffImage.height;

			for (int idx = off; idx + 1 < max; idx += 2) {
				int rgbold = lastImage.pixels[idx];
				int rgbnew = img.pixels[idx];

				int rold = (rgbold >> 16) & 0xFF;
				int gold = (rgbold >> 8) & 0xFF;
				int bold = rgbold & 0xFF;

				int rnew = (rgbnew >> 16) & 0xFF;
				int gnew = (rgbnew >> 8) & 0xFF;
				int bnew = rgbnew & 0xFF;

				int rdiff = Math.abs(rold - rnew);
				int gdiff = Math.abs(gold - gnew);
				int bdiff = Math.abs(bold - bnew);

				if (threshold > 0) {
					if (rdiff < threshold)
						rdiff = 0;
					if (gdiff < threshold)
						gdiff = 0;
					if (bdiff < threshold)
						bdiff = 0;
				}
				int rgbdiff = rdiff;
				rgbdiff = (rgbdiff << 8) + gdiff;
				rgbdiff = (rgbdiff << 8) + bdiff;
				diffImage.pixels[idx] = rgbdiff;
			}
		}

		lastImage = img;

		item.remove("data");
		item.put("data", diffImage);
		return item;
	}

	private ImageRGB copy(ImageRGB img) {

		int[] pixels = new int[img.pixels.length];
		for (int i = 0; i < img.pixels.length; i++)
			pixels[i] = img.pixels[i];

		return new ImageRGB(img.width, img.height, pixels);
	}
}
