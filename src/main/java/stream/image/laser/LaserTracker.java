/**
 * 
 */
package stream.image.laser;

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
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

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

	protected String laserImage;
	protected ImageRGB lastImage;
	protected Point initialPoint;
	protected int initialRGB;
	protected int searchSize;
	protected int threshold;

	public LaserTracker() {
		laserImage = null;
		lastImage = null;
		initialPoint = null;
		searchSize = 20;
		threshold = 100;
	}

	public int getSearchSize() {
		return searchSize;
	}

	public void setSearchSize(int searchSize) {
		this.searchSize = searchSize;
	}

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

		if (initialPoint == null) {
			initialPoint = getInitialPoint(img);
			if (initialPoint != null) {
				initialRGB = img.getRGB(initialPoint.x, initialPoint.y);
				markLaserPointer(initialPoint, img,0,255,0);
				log.info(
						"********************* found initial point {} ***************************",
						initialPoint);
			}
			item.put("data", img);
			return item;
		}
		Point evalPoint = evaluateLaserPointer(initialPoint, initialRGB, img);
		if (evalPoint != null) {
			log.info("Found laserPointer");
			initialPoint = evalPoint;
			initialRGB = img.getRGB(initialPoint.x, initialPoint.y);
			markLaserPointer(initialPoint, img, 0,0,255);
			item.put("data", img);
			return item;
		}

		log.info("can't find laserPointer");
		initialPoint = null;
		initialRGB = -1;

		item.put("data", img);
		return item;
	}

	private Point evaluateLaserPointer(Point p, int oldRGB, ImageRGB img) {
		int size = searchSize;
		Point ep = null;
		int minThreshold = threshold;
		
		int minx=(p.x - size>0)?p.x - size:0;
		int maxx=(p.x + size>img.getWidth()-1)?img.getWidth()-1:(p.x + size);
		int miny=(p.y - size>0)?p.y - size:0;
		int maxy=(p.y + size>img.getHeight()-1)?img.getHeight()-1:(p.y + size);
		
		for (int x =minx ; x < maxx; x++) {
			for (int y = miny; y <maxy; y++) {
				int rgbnew = img.getRGB(x, y);

				int rnew = (rgbnew >> 16) & 0xFF;
				if (rnew > 20) {
//					int gnew = (rgbnew >> 8) & 0xFF;
//					int bnew = rgbnew & 0xFF;

					int rold = (oldRGB >> 16) & 0xFF;
//					int gold = (oldRGB >> 8) & 0xFF;
//					int bold = oldRGB & 0xFF;

					int rdiff = Math.abs(rold - rnew);
//					int gdiff = Math.abs(gold - gnew);
//					int bdiff = Math.abs(bold - bnew);

//					int rgbdiff = rdiff;
//					rgbdiff = (rgbdiff << 8) + gdiff;
//					rgbdiff = (rgbdiff << 8) + bdiff;
					if (rdiff < minThreshold)
						ep = new Point(x, y);
				}
			}
		}
		return ep;
	}

	private Point getInitialPoint(ImageRGB img) {

		Point p = null;
		int maxR = 50;
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {

				int rgb = img.getRGB(x, y);
				int r = (rgb >> 16) & 0xFF;
				if (r >= maxR) {
					p = new Point(x, y);
				}
			}
		}
		return p;
	}

	private void markLaserPointer(Point p, ImageRGB img, int r, int g, int b) {
		int x = p.x;
		int y = p.y;

		img.setRGB(x - 5, y - 5, r, g, b);
		img.setRGB(x + 5, y - 5, r, g, b);
		img.setRGB(x - 5, y + 5, r, g, b);
		img.setRGB(x + 5, y + 5,r, g, b);
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