/**
 * 
 */
package stream.laser;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;

import stream.laser.SteelPanel.PointT;

/**
 * @author chris
 * 
 */
public class LaserSword extends MouseAdapter {

	final ArrayList<PointT> trace = new ArrayList<PointT>();
	ImageIcon icon;
	boolean active = false;

	public LaserSword() {
		try {
			icon = new ImageIcon(
					LaserSword.class.getResource("/animated-flame.gif"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		active = true;
		e.getComponent().repaint();
		e.getComponent().validate();
	}

	/**
	 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		active = false;
		e.getComponent().repaint();
		e.getComponent().validate();
	}

	/**
	 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);
		trace.add(new PointT(e.getPoint()));

		/*
		 */
		Iterator<PointT> it = trace.iterator();
		while (it.hasNext()) {
			PointT p = it.next();
			if (p.age() > 5000) {
				System.out.println("Removing point with age=" + p.age());
				it.remove();
			}
		}

		e.getComponent().repaint();
		e.getComponent().validate();
	}

	public boolean active() {
		return active;
	}

	public ImageIcon getFlame() {
		return icon;
	}

	public ArrayList<PointT> getTrace() {
		return trace;
	}
}
