/**
 * 
 */
package stream.image;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;

/**
 * @author chris
 * 
 */
public class LaserTracker extends AbstractImageProcessor {

	static Logger log = LoggerFactory.getLogger(LaserTracker.class);

	String laserImage = null;
	ImageRGB lastImage = null;

	/**
	 * @return the laserImage
	 */
	public String getLaserImage() {
		return laserImage;
	}

	/**
	 * @param laserImage
	 *            the laserImage to set
	 */
	public void setLaserImage(String laserImage) {
		this.laserImage = laserImage;
	}

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      stream.image.ImageRGB)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		Set<Point> points = new HashSet<Point>();

		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {

				int rgb = img.getRGB(x, y);
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = rgb & 0xFF;

				int min = Math.min(r, Math.min(g, b));
				int max = Math.max(r, Math.max(g, b));
				double v = max;
				double delta = max - min;
				double s = 0;

				if (max != 0)
					s = delta / max;

				// log.info("s-value at {},{} is: " + s, x, y);

				if (r > 200 && g > 200 && b > 200) {
					// log.info("Laser-pointer at {},{} ?", x, y);
					// img.setRGB(x, y, 0, 255, 0);
					points.add(new Point(x, y));
				}
			}
		}

		if (!points.isEmpty()) {
			double cx = 0.0;
			double cy = 0.0;
			Point center = null;

			for (Point p : points) {
				if (center == null) {
					center = new Point(p.x, p.y);
					cx = p.x;
					cy = p.y;
				} else {
					// log.info("distance to current center: {}", dist(center,
					// p));
					cx += p.x;
					cy += p.y;
				}
			}

			center.setLocation(cx / points.size(), cy / points.size());
			img.setRGB(center.x, center.y, 0, 255, 0);
			item.put("laser:timestamp", System.currentTimeMillis());
			item.put("laser:x", center.x);
			item.put("laser:y", center.y);

			if (laserImage != null) {
				ImageRGB li = lastImage;
				if (li == null) {
					log.info("Need to add new laser-image...");
					li = new ImageRGB(img.getWidth(), img.getHeight());
					lastImage = li;
				}
				li.setRGB(center.x, center.y, 255, 255, 255);
				item.put(laserImage, li);
			}
		}

		item.put("data", img);
		return item;
	}

	public double dist(Point p, Point q) {
		return p.distance(q.getX(), q.getY());
	}
}