/**
 * 
 */
package stream.laser;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * @author chris
 * 
 */
public class Trapez extends ADrawable {

	public final Point tl;
	public final Point tr;
	public final Point bl;
	public final Point br;

	Color color = Color.YELLOW;

	public Trapez(Point tl, Point tr, Point bl, Point br) {
		this.tl = tl;
		this.tr = tr;
		this.bl = bl;
		this.br = br;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color
	 *            the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @see stream.laser.Drawable#draw(java.awt.Graphics2D)
	 */
	@Override
	public void draw(Graphics2D g) {
		if (color != null)
			g.setColor(color);
		g.drawLine(tl.x, tl.y, tr.x, tr.y);
		g.drawLine(tr.x, tr.y, br.x, br.y);
		g.drawLine(br.x, br.y, bl.x, bl.y);
		g.drawLine(bl.x, bl.y, tl.x, tl.y);
	}
}
