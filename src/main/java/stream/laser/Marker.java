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

	public Point getPoint() {
		return new Point(x.intValue(), y.intValue());
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
		g.fillOval(x.intValue() - radius / 2, y.intValue() - radius / 2,
				radius, radius);
		g.setColor(old);
	}

	public String toString() {
		return "Marker(" + this.x + "," + this.y + ")";
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o instanceof Marker) {
			Marker other = (Marker) o;
			return toString().equals(other.toString());
		}

		return false;
	}
}