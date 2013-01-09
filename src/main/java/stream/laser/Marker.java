/**
 * 
 */
package stream.laser;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * @author Christian Bockermann
 * 
 */
public class Marker extends ADrawable {

	Float x;
	Float y;
	Color color;
	int radius = 10;

	public Marker(int x, int y, Color color) {
		this.x = Float.valueOf(x);
		this.y = Float.valueOf(y);
		this.color = color;
	}

	public void setPosition(int x, int y) {
		this.x = Float.valueOf(x);
		this.y = Float.valueOf(y);
	}

	public void setColor(Color c) {
		this.color = c;
	}

	public void translate(float dx, float dy) {
		x += dx;
		y += dy;
	}

	/**
	 * @see stream.laser.Drawable#draw(java.awt.Graphics2D)
	 */
	@Override
	public void draw(Graphics2D g) {
		if (color == null)
			return;

		Color old = g.getColor();
		g.setColor(color);
		g.fillOval(x.intValue() - radius, y.intValue() - radius, radius, radius);
		g.setColor(old);
	}
}