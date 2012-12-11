/**
 * 
 */
package stream.image;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;

/**
 * <p>
 * This processor provides a very simple strategy for detecting laser pointers
 * in an image. It simply looks for small, very bright areas. If the
 * <code>laserImage</code> property is set, a track of the identified
 * laser-points is produced.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
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
//				int g = (rgb >> 8) & 0xFF;
//				int b = rgb & 0xFF;

//				if (r > 200 && g > 200 && b > 200) {
					// log.info("Laser-pointer at {},{} ?", x, y);
					// img.setRGB(x, y, 0, 255, 0);
//					points.add(new Point(x, y));
//				}
			}
		}

		if (!points.isEmpty()) {
			List<Point> laserPoints = new ArrayList<Point>();
			List<Point> visited = new ArrayList<Point>();

			for (Point p : points) {

				if (visited.contains(p))
					continue;

				Set<Point> neigh = getNeighbors(p, points, 10.0);
				if (neigh.size() > 5) {
					Point lp = this.getCenter(neigh);
					laserPoints.add(lp);
					visited.addAll(neigh);
				}
			}

			int i = -1;
			Point laserPoint = null;
			for (Point p : laserPoints) {

				String id = "laser";
				if (i > -1)
					id = "laser" + i;
				else
					laserPoint = p; // only the first laser-point will be
									// painted

				img.setRGB(p.x, p.y, 0, 255, 0);
				item.put(id + ":timestamp", System.currentTimeMillis());
				item.put(id + ":x", p.x);
				item.put(id + ":y", p.y);
				i++;
			}

			if (laserImage != null && laserPoint != null) {
				ImageRGB li = lastImage;
				if (li == null) {
					log.info("Need to add new laser-image...");
					li = new ImageRGB(img.getWidth(), img.getHeight());
					lastImage = li;
				}
				li.setRGB(laserPoint.x, laserPoint.y, 255, 255, 255);
				item.put(laserImage, li);
			}
		}

		item.put("data", img);
		return item;
	}

	public double dist(Point p, Point q) {
		return p.distance(q.getX(), q.getY());
	}

	public Set<Point> getNeighbors(Point p, Set<Point> points, double radius) {
		Set<Point> neigh = new LinkedHashSet<Point>();
		for (Point q : points) {
			if (p != q && p.distance(q) < radius)
				neigh.add(q);
		}
		return neigh;
	}

	public Point getCenter(Collection<Point> pts) {
		double cx = 0.0;
		double cy = 0.0;
		Point center = new Point(0, 0);
		double cnt = 0.0;

		for (Point p : pts) {
			cx += p.x;
			cy += p.y;
			cnt += 1.0d;
		}

		if (cnt == 0)
			cnt = 1.0;

		center.setLocation(cx / cnt, cy / cnt);
		return center;
	}
}