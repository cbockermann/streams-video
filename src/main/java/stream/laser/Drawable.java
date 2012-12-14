/**
 * 
 */
package stream.laser;

import java.awt.Graphics2D;

/**
 * @author chris
 * 
 */
public interface Drawable {

	public boolean isVisible();

	public void draw(Graphics2D g);
}
