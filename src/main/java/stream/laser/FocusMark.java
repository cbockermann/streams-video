/**
 * 
 */
package stream.laser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * @author chris
 * 
 */
public class FocusMark extends ADrawable {

	/**
	 * @see stream.laser.Drawable#draw(java.awt.Graphics2D)
	 */
	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.BLACK);

		int x = 512;
		int y = 384;
		g.setStroke(new BasicStroke(4.0f));
		drawCross(g, x, y, 20);
	}

	protected void drawCross(Graphics2D g, int x, int y, int size) {
		g.drawLine(x - size / 2, y, x + size / 2, y);
		g.drawLine(x, y - size / 2, x, y + size / 2);
	}
}
