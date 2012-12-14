package stream.laser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

public class TurboDiffImage extends AbstractImageProcessor {
	static Logger log = LoggerFactory.getLogger(TurboDiffImage.class);

	protected ImageRGB lastImage = new ImageRGB(0, 0);

	private int threshold;

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
		ImageRGB diffImage = new ImageRGB(img.width, img.height,
				new int[img.width * img.height]);

		if (diffImage.height == lastImage.height
				&& diffImage.width == lastImage.width) {

			int off = 0;
			int max = diffImage.width * diffImage.height;
			for (int idx = off; idx + 1 < max; idx++) {
				int rgbold = lastImage.pixels[idx];
				int rgbnew = img.pixels[idx];

				int p = rgbold >> 8;
				int gold = (p) & 0xFF;
				p = p >> 8;
				int rold = (p) & 0xFF;
				int bold = rgbold & 0xFF;

				p = rgbnew >> 8;
				int gnew = (p) & 0xFF;
				p = p >> 8;
				int rnew = (p) & 0xFF;
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
}
