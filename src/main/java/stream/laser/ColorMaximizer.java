package stream.laser;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

public class ColorMaximizer extends AbstractImageProcessor {

	int threshold;

	public ColorMaximizer() {
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

		for (int i = 0; i < img.getWidth(); i++) {
			for (int j = 0; j < img.getHeight(); j++) {

				int rgbnew = img.getRGB(i, j);

				int rnew = (rgbnew >> 16) & 0xFF;
				int gnew = (rgbnew >> 8) & 0xFF;
				int bnew = rgbnew & 0xFF;

				int color = rnew + gnew + bnew;
				if (color < 100) {
					img.setRGB(i, j, 0);
					continue;
				}
				if (rnew > gnew && rnew > bnew) {
					int rgbdiff = 255;
					rgbdiff = (rgbdiff << 8);
					rgbdiff = (rgbdiff << 8);
					img.setRGB(i, j, rgbdiff);
				}

			}
		}

		item.remove("data");
		item.put("data", img);

		return item;
	}
}
