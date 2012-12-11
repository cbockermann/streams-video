package stream.image.laser;

import org.omg.PortableInterceptor.INACTIVE;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

public class DiffImage extends AbstractImageProcessor {

	protected ImageRGB lastImage = new ImageRGB(0, 0);

	int threshold;

	public DiffImage() {
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
					diffImage.setRGB(i, j, rgbdiff);
				}
			}
		}

		lastImage = img;

		item.remove("data");
		item.put("data", diffImage);

		return item;
	}

	private ImageRGB copy(ImageRGB img) {
		ImageRGB copy = new ImageRGB(img.getWidth(), img.getHeight());
		for (int i = 0; i < copy.getWidth(); i++) {
			for (int j = 0; j < copy.getHeight(); j++) {
				copy.setRGB(i, j, img.getRGB(i, j));
			}
		}
		return copy;
	}

}
