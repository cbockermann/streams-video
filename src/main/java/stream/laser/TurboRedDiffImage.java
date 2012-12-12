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

	private int threshold;

	public TurboRedDiffImage() {
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
		 ImageRGB diffImage = new ImageRGB(img.width, img.height, new
		 int[img.width*img.height]);

		if (diffImage.height == lastImage.height
				&& diffImage.width == lastImage.width) {

			int max = diffImage.width * diffImage.height;
			for (int idx = 0; idx + 1 < max; idx++) {
				int rgbold = lastImage.pixels[idx];
				int rgbnew = img.pixels[idx];

				int rold = (rgbold >> 16) & 0xFF;
				int rnew = (rgbnew >> 16) & 0xFF;
				int rdiff = Math.abs(rold - rnew);

				if (threshold > 0) {
					if (rdiff < threshold)
						rdiff = 0;
				}
				diffImage.pixels[idx] = rdiff<<16;
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