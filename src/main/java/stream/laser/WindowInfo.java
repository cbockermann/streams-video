/**
 * 
 */
package stream.laser;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;

/**
 * @author chris
 * 
 */
public class WindowInfo extends ComponentAdapter {

	final JFrame frame;

	public WindowInfo(JFrame frame) {
		this.frame = frame;
	}

	/**
	 * @see java.awt.event.ComponentAdapter#componentMoved(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentMoved(ComponentEvent arg0) {
		super.componentMoved(arg0);
		frame.setTitle("Frame @ " + frame.getLocation().x + " "
				+ frame.getLocation().y);
	}
}
