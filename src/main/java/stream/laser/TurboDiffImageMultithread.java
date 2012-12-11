package stream.laser;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

public class TurboDiffImageMultithread extends AbstractImageProcessor {

	static Logger log = LoggerFactory
			.getLogger(TurboDiffImageMultithread.class);
	protected ImageRGB lastImage = new ImageRGB(0, 0);

	int threshold;
	int threadCount = 4;
	DiffThread[] worker;
	Future<Boolean>[] threads;
	ExecutorService executorService;

	@SuppressWarnings("unchecked")
	public TurboDiffImageMultithread() {
		threshold = -1;
		worker = new DiffThread[threadCount];
		threads = (Future<Boolean>[]) new Future[threadCount];
		for (int i = 0; i < threadCount; i++) {
			worker[i] = new DiffThread();
		}

		executorService = Executors.newFixedThreadPool(threadCount);
	}

	/**
	 * @see stream.AbstractProcessor#finish()
	 */
	@Override
	public void finish() throws Exception {
		super.finish();
		executorService.shutdownNow();
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

			int max = diffImage.width * diffImage.height;

			int share = max / threadCount;

			for (int t = 0; t < threadCount; t++) {
				worker[t].init(lastImage, img, t * share, (t + 1) * share,
						diffImage);
				// threads[t] = new Thread(worker[t]);
				// log.info("Starting thread {}", threads[t]);
				// threads[t].start();
				threads[t] = executorService.submit(worker[t]);
			}

			for (int t = 0; t < threadCount; t++) {
				try {
					threads[t].get();
					// log.info("Thread {} joined.", threads[t]);
				} catch (Exception e) {
					e.printStackTrace();
				}
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

	public class DiffThread implements Callable<Boolean> {
		ImageRGB lastImage;
		ImageRGB diffImage;
		ImageRGB img;
		int offset;
		int max;

		public void init(ImageRGB last, ImageRGB img, int offset, int max,
				ImageRGB diffImage) {
			this.lastImage = last;
			this.diffImage = diffImage;
			this.img = img;
			this.offset = offset;
			this.max = max;
		}

		public Boolean call() {

			for (int idx = offset; idx < max; idx++) {
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

			return true;
		}
	}
}
