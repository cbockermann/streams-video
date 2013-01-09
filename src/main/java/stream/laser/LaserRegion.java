/**
 * 
 */
package stream.laser;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * @author Christian Bockermann
 * 
 */
public class LaserRegion extends ADrawable {

	Point p;
	int radius = 25;

	public LaserRegion(int x, int y) {
		p = new Point(x, y);
	}

	public void setLocation(int x, int y) {
		p.setLocation(x, y);
	}

	/**
	 * @see stream.laser.Drawable#draw(java.awt.Graphics2D)
	 */
	@Override
	public void draw(Graphics2D g) {
		Color c = g.getColor();
		g.setColor(Color.GREEN);
		g.drawOval(p.x - radius, p.y - radius, p.x + radius, p.y + radius);
		g.setColor(c);
	}
}
