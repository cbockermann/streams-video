package stream.laser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

public class TurboRedDiffImage extends AbstractImageProcessor {
	static Logger log = LoggerFactory.getLogger(TurboRedDiffImage.class);

	protected ImageRGB lastImage = new ImageRGB(0, 0);

	protected int threshold;
	protected int maxRedPixels;
	protected String output;

	public TurboRedDiffImage() {
		threshold = -1;
		maxRedPixels = 500;
		output = this.imageKey;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getMaxRedPixels() {
		return maxRedPixels;
	}

	public void setMaxRedPixels(int maxRedPixels) {
		this.maxRedPixels = maxRedPixels;
	}

	/**
	 * @return the output
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * @param output
	 *            the output to set
	 */
	public void setOutput(String output) {
		this.output = output;
	}

	@Override
	public Data process(Data item, ImageRGB img) {
		ImageRGB diffImage = new ImageRGB(img.width, img.height,
				new int[img.width * img.height]);
		int count = 0;
		if (diffImage.height == lastImage.height
				&& diffImage.width == lastImage.width) {

			int max = diffImage.width * diffImage.height;
			for (int idx = 0; idx + 1 < max; idx++) {
				int rgbold = lastImage.pixels[idx];
				int rgbnew = img.pixels[idx];

				int rold = (rgbold >> 16) & 0xFF;
				int rnew = (rgbnew >> 16) & 0xFF;
				int rdiff = Math.abs(rold - rnew);

				if (rdiff > threshold) {
					diffImage.pixels[idx] = (rdiff << 16);
					count++;
				}
//				if (count > maxRedPixels)
//					return null;
			}
		}

		lastImage = img;

		item.put(output, diffImage);
		return item;
	}
}