/**
 * 
 */
package stream.image;

import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;

/**
 * @author chris
 * 
 */
public class DetectBrokenImage extends AbstractImageProcessor {

	static Logger log = LoggerFactory.getLogger(DetectBrokenImage.class);

	double threshold = 0.5;

	/**
	 * @return the threshold
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            the threshold to set
	 */
	@Parameter(description = "The fraction of pixels that need to have the 'broken' color to mark this image as broken.")
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      stream.image.ImageRGB)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		double cnt = 0;
		double area = (img.getHeight()) * img.getWidth();

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int r = img.getRED(x, y);
				int g = img.getGREEN(x, y);
				int b = img.getBLUE(x, y);
				if (r == 128 && g == 128 && b == 128)
					cnt++;
			}
		}

		double broken = cnt / area;
		// log.debug("Fraction of {} equal pixels!", broken);
		item.put(getImage() + ":broken", broken);
		return item;
	}

	public static void main(String args[]) {
		try {
			ImageRGB img = new ImageRGB(ImageIO.read(new FileInputStream(
					new File("/Volumes/RamDisk/frame-48.jpg"))));

			Data item = DataFactory.create();
			item.put("data", img);

			DetectBrokenImage dbi = new DetectBrokenImage();
			dbi.process(item);

		} catch (Exception e) {

		}
	}
}
