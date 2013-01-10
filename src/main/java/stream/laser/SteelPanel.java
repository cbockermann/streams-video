/**
 * 
 */
package stream.laser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.net.NetworkPointer;
import stream.net.PointerListener;

/**
 * @author chris
 * 
 */
public class SteelPanel extends JPanel implements PointerListener {

	/** The unique class ID */
	private static final long serialVersionUID = -1683540875635107889L;

	static Logger log = LoggerFactory.getLogger(SteelPanel.class);

	LaserSword sword = new LaserSword();
	ImageIcon icon;
	ArrayList<PointT> cut = new ArrayList<PointT>();
	int flameWidth = 20;
	int flameHeight = 20;
	PointT lastPoint = null;
	final List<Drawable> drawables = new ArrayList<Drawable>();

	public SteelPanel() {
		this.addMouseMotionListener(sword);
		this.addMouseListener(sword);
		icon = sword.getFlame();
		icon = null;

		this.setBackground(Color.BLUE);
	}

	public void paint(Graphics g) {
		super.paint(g);

		PointT last = null;

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(2.75f));
		// g.setColor(Color.GREEN);
		g.setColor(new Color(0, 174, 162));
		g.setColor(Color.DARK_GRAY);
		g.setColor(Color.GREEN);
		ArrayList<PointT> points = this.cut; // sword.getTrace();
		for (int i = 0; i < points.size(); i++) {
			PointT p = points.get(i);
			if (last == null || p.timestamp - last.timestamp > 200)
				last = p;

			g.drawLine(last.x, last.y, p.x, p.y);
			last = p;
			if (i + 1 >= points.size()) {
				if (icon != null && sword.active()) {
					System.out.println("Drawing flame...");
					g.drawImage(icon.getImage(), p.x - (flameWidth / 2), p.y
							- (flameHeight), flameWidth, flameHeight,
							icon.getImageObserver());
				}
			}
			if (!cut.contains(p))
				cut.add(p);
		}

		for (Drawable d : drawables) {
			if (d.isVisible())
				d.draw(g2);
		}
	}

	public static class PointT extends Point {
		/** The unique class ID */
		private static final long serialVersionUID = -523944637124270542L;
		long timestamp;

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

	public static void main(String[] args) throws Exception {

		JFrame frame = new JFrame();
		frame.setUndecorated(true);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

		final SteelPanel panel = new SteelPanel();

		InetSocketAddress addr = null;
		if (args.length > 1) {
			addr = new InetSocketAddress(args[0], new Integer(args[1]));
		}

		if (args.length > 0) {
			addr = new InetSocketAddress(args[0], 9100);
		}

		Integer w = new Integer(System.getProperty("width", "" + screen.width));
		Integer h = new Integer(
				System.getProperty("height", "" + screen.height));
		Integer x = new Integer(System.getProperty("x", "0"));
		Integer y = new Integer(System.getProperty("y", "0"));

		NetworkPointer pointer = null;
		if (addr != null) {
			pointer = new NetworkPointer(addr);
			// pointer.addListener(panel);
			pointer.setDaemon(true);
			pointer.start();
		}

		frame.getContentPane().add(panel);
		frame.setSize(w, h);
		frame.addComponentListener(new WindowInfo(frame));
		frame.setLocation(x, y);
		frame.setVisible(true);

		final Calibration c = new Calibration(panel);
		if (pointer != null)
			pointer.addListener(c);

		JDialog control = new JDialog();
		JButton clear = new JButton("clear");
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.lastPoint = null;
				panel.cut.clear();
				panel.repaint();
				panel.validate();
			}
		});

		final JButton cal = new JButton("Calibration");
		cal.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				c.nextMark();
			}
		});

		final FocusMark fm = new FocusMark();
		final JButton focus = new JButton("Add Focus Mark");
		focus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (panel.drawables.contains(fm)) {
					panel.remove(fm);
					focus.setText("Add Focus Mark");
				} else {
					panel.add(fm);
					focus.setText("Remove Focus Mark");
				}
				panel.drawableChanged();
			}
		});

		JPanel buttons = new JPanel(new FlowLayout());
		buttons.add(clear);
		buttons.add(cal);
		buttons.add(focus);

		control.getContentPane().add(buttons);
		control.setModal(false);
		control.pack();
		control.setVisible(true);

	}

	/**
	 * @see stream.net.PointerListener#pointingAt(int, int)
	 */
	@Override
	public void pointingAt(int x, int y) {
		log.info("Pointer at {},{}", x, y);

		if (lastPoint != null && lastPoint.distanceSq(x, y) > 4000) {
			return;
		}

		lastPoint = new PointT(x, y);
		this.cut.add(lastPoint);
		if (cut.size() > 20)
			cut.remove(0);
		this.repaint();
		this.validate();
	}

	public void add(Drawable d) {
		drawables.add(d);
	}

	public void remove(Drawable d) {
		drawables.remove(d);
	}

	public void drawableChanged() {
		repaint();
		validate();
	}
}