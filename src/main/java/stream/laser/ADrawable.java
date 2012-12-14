/**
 * 
 */
package stream.laser;


/**
 * @author chris
 * 
 */
public abstract class ADrawable implements Drawable {

	boolean visible = true;

	/**
	 * @see stream.laser.Drawable#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean b) {
		this.visible = b;
	}
}
