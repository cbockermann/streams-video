/**
 * 
 */
package stream.laser.game;

import java.awt.Point;

/**
 * @author chris
 * 
 */
public class PointT extends Point {
	/** The unique class ID */
	private static final long serialVersionUID = -523944637124270542L;
	public final long timestamp;

	public PointT(Point p) {
		super(p);
		timestamp = System.currentTimeMillis();
	}

	public PointT(int x, int y) {
		super(x, y);
		timestamp = System.currentTimeMillis();
	}

	public PointT(java.lang.Double x, java.lang.Double y) {
		this(x.intValue(), y.intValue());
	}

	public long age() {
		return System.currentTimeMillis() - timestamp;
	}

	public String toString() {
		return "PointT(" + this.x + "," + this.y + ")@" + timestamp;
	}
}
