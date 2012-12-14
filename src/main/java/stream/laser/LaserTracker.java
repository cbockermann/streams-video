/**
 * 
 */
package stream.laser;

import java.awt.Point;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
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
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt; Hendrik
 *         Blom
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
	protected int frame = 0;
	protected String output;
	protected boolean skipWithoutPoint = false;
	protected DatagramSocket socket;
	protected String address;
	protected int port = 9105;
	protected InetAddress addr;
	protected DatagramPacket packet;
	protected int evalMagic;
	protected int initialMagic;

	public LaserTracker() {
		laserImage = null;
		lastImage = null;
		initialPoint = null;
		searchSize = 20;
		threshold = 20;
		output = imageKey;
		evalMagic = 0;
		initialMagic = 0;
	}

	/**
	 * @return the threshold
	 */
	public int getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            the threshold to set
	 */
	public void setThreshold(int threshold) {
		this.threshold = threshold;
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

	/**
	 * @return the skipWithoutPoint
	 */
	public boolean isSkipWithoutPoint() {
		return skipWithoutPoint;
	}

	/**
	 * @param skipWithoutPoint
	 *            the skipWithoutPoint to set
	 */
	public void setSkipWithoutPoint(boolean skipWithoutPoint) {
		this.skipWithoutPoint = skipWithoutPoint;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @see stream.AbstractProcessor#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);

		if (address != null) {
			socket = new DatagramSocket();
			addr = InetAddress.getByName(address);

			packet = new DatagramPacket(new byte[0], 0, addr, port);
		}
	}

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      stream.image.ImageRGB)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		if (frame > 0 && frame % 2 == 0) {
			return item;
		}

		if (initialPoint == null) {
			initialPoint = getInitialPoint(img);
			if (initialPoint != null) {
				initialRGB = img.getRGB(initialPoint.x, initialPoint.y);
				log.info(
						"********************* found initial point {} ***************************",
						initialPoint);
				this.sendUDP();
			}
			item.put(output, img);
			return item;
		}
		if (initialMagic < 3) {
			Point cp = getInitialPoint(img);
			if (cp == null) {
			} else {
				double magicDist = dist(initialPoint, cp);

				if (magicDist < 100)
					initialMagic++;
				else {
					initialMagic = 0;
					initialPoint = null;
				}
			}
			item.put(output, img);
			return item;
		}

		Point evalPoint = evaluateLaserPointer(initialPoint, initialRGB, img);
		if (evalPoint != null) {
			// log.info("Found laserPointer");
			initialPoint = evalPoint;
			initialRGB = img.getRGB(initialPoint.x, initialPoint.y);
			markLaserPointer(initialPoint, img, 255, 0, 0);
			item.put(output, img);
			item.put("laser:x", initialPoint.x);
			item.put("laser:y", initialPoint.y);
			this.sendUDP();
			return item;
		}
		//
		// if (skipWithoutPoint)
		// return null;

		// log.info("can't find laserPointer");
		initialPoint = null;
		initialRGB = -1;
		initialMagic = 0;
		return item;
	}

	private void sendUDP() {
		if (socket != null && packet != null && initialPoint != null) {

			try {
				byte[] buf = ("(" + initialPoint.x + "," + initialPoint.y + ")")
						.getBytes();

				if (socket != null) {
					packet.setData(buf);
					socket.send(packet);
				}
			} catch (Exception e) {
				if (log.isDebugEnabled())
					e.printStackTrace();
			}
		}
	}

	private Point evaluateLaserPointer(Point ep, int oldRGB, ImageRGB img) {
		int size = searchSize;

		int count = 10;
		Point[] points = new Point[count];

		int minThreshold = threshold;

		int minx = (ep.x - size > 0) ? ep.x - size : 0;
		int maxx = (ep.x + size > img.width - 1) ? img.width - 1
				: (ep.x + size);
		int miny = (ep.y - size > 0) ? ep.y - size : 0;
		int maxy = (ep.y + size > img.height - 1) ? img.height - 1
				: (ep.y + size);

		int[] pixels = img.pixels;
		for (int x = minx; x < maxx; x++) {
			for (int y = miny; y < maxy; y++) {
				int idx = y * img.width + x;
				int rgbnew = pixels[idx]; // img.getRGB(x, y);
				int rnew = (rgbnew >> 16) & 0xFF;

				if (rnew > 20) {
					int rold = (oldRGB >> 16) & 0xFF;
					int rdiff = Math.abs(rold - rnew);
					if (rdiff < minThreshold) {
						for (int i = count - 1; i > 0; i--) {
							points[i] = points[i - 1];
						}
						minThreshold = rdiff;
						points[0] = new Point(x, y);
					}
				}
			}
		}

		// return findMinDist(p,points);
		// return average(p,points);
		Point p = weightedAverage(ep, points);
		if (p == null) {
			if (evalMagic < 10) {
				evalMagic++;
				return ep;
			}
		}
		evalMagic = 0;
		return p;
	}

	private Point getInitialPoint(ImageRGB img) {

		int px = -1;
		int py = -1;
		int maxR = 50;
		int[] pixels = img.getPixels();
		for (int x = 0; x < img.width; x++) {
			for (int y = 0; y < img.height; y++) {

				int idx = y * img.getWidth() + x;
				int rgb = pixels[idx];
				// int rgb = img.getRGB(x, y);
				int r = (rgb >> 16) & 0xFF;
				if (r >= maxR) {
					px = x;
					py = y;
					// p = new Point(x, y);
				}
			}
		}
		if (px >= 0 && py >= 0)
			return new Point(px, py);

		return null;
	}

	private void markLaserPointer(Point p, ImageRGB img, int r, int g, int b) {
		int x = p.x;
		int y = p.y;

		// int color = 0xffffffff;
		int color = r;
		color = (color << 8) + g;
		color = (color << 8) + b;

		int idx = (y - 5) * img.width + x - 5;
		if (idx < img.pixels.length && idx >= 0)
			img.pixels[idx] = color;

		idx = (y + 5) * img.width + x - 5;
		if (idx < img.pixels.length && idx >= 0)
			img.pixels[idx] = color;

		idx = (y - 5) * img.width + x + 5;
		if (idx < img.pixels.length && idx >= 0)
			img.pixels[idx] = color;

		idx = (y + 5) * img.width + x + 5;
		if (idx < img.pixels.length && idx >= 0)
			img.pixels[idx] = color;
	}

	public double dist(Point p, Point q) {
		return p.distance(q.getX(), q.getY());
	}

	private Point findMinDist(Point p, Point[] points) {
		int minp = points.length;
		double dist = 0.0d;
		double minDist = Double.MAX_VALUE;
		for (int i = 0; i < points.length; i++) {
			Point ep = points[i];
			if (ep == null)
				continue;
			dist = dist(p, ep);
			// dist = Math.abs(p.x - p.x) + Math.abs(p.y - p.y);
			if (dist < minDist) {
				minp = i;
				minDist = dist;
			}
		}
		return (minp == points.length) ? null : points[minp];
	}

	private Point average(Point ep, Point[] points) {
		int x = 0;
		int y = 0;
		int count = 0;
		for (Point p : points) {
			if (p != null) {
				x += p.x;
				y += p.y;
			} else
				count++;
		}
		if (count == points.length)
			return null;
		x /= points.length;
		y /= points.length;
		Point r = new Point(x, y);
		log.info("found point {}", r);
		return r;
	}

	private Point weightedAverage(Point ep, Point[] points) {
		int x = 0;
		int y = 0;
		int count = 0;
		double dist = 0;
		for (Point p : points) {
			if (p != null) {
				double d = dist(ep, p);
				dist += (searchSize - d);
				x += p.x * (searchSize - d);
				y += p.y * (searchSize - d);
			} else
				count++;
		}
		if (count == points.length)
			return null;
		x /= dist;
		y /= dist;
		Point r = new Point(x, y);
		log.info("found point {}", r);
		return r;
	}

}
