package stream.laser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

public class UberDiffImage extends AbstractImageProcessor {
	static Logger log = LoggerFactory.getLogger(UberDiffImage.class);

	protected ImageRGB lastImage = new ImageRGB(0, 0);

	private int threshold;

	public UberDiffImage() {
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

		// int x = 5;

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

				if (rnew + gnew + bnew > 360) {
					// diffImage.pixels[idx] = 0xff00FF00;
					continue;
				}

				if (threshold > 0) {

					if (rdiff < threshold)
						continue;
					if (gdiff < threshold)
						continue;
					if (bdiff < threshold)
						continue;
					//
					// if (Math.abs(rnew - gnew) < x && Math.abs(gnew - bnew) <
					// x
					// && Math.abs(rnew - bnew) < x) {
					// diffImage.pixels[idx] = 0x00ffffff &
					// lastImage.pixels[idx];
					// continue;
					// }
				}
				//
				// int rm = (rold + rnew) / 2;
				// int gm = (gold + gnew) / 2;
				// int bm = (bold + bnew) / 2;

				// int c = (0xff << 24) + (rm << 16) + (gm << 8) + bm;

				diffImage.pixels[idx] = img.pixels[idx];
			}
		}

		lastImage = img;

		item.remove("data");
		item.put("data", diffImage);
		return item;
	}
}
