package stream.laser;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

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
		
	public TurboRedDiffImage() {
		threshold = -1;
		maxRedPixels=500;
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

	@Override
	public Data process(Data item, ImageRGB img) {
		ImageRGB diffImage = new ImageRGB(img.width, img.height,
				new int[img.width * img.height]);
		int count=0;
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
					diffImage.pixels[idx] = 16711680;
					count++;
				}
			if(count>maxRedPixels)
				return null;
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

	private long getCpuTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? bean
				.getCurrentThreadCpuTime() : 0L;
	}
}
