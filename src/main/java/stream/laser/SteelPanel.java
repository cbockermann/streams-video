/**
 * 
 */
package stream.laser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

		this.setBackground(Color.BLACK);
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
		ArrayList<PointT> points = this.cut; // sword.getTrace();
		for (int i = 0; i < points.size(); i++) {
			PointT p = points.get(i);
			if (last == null)
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

		public long age() {
			return System.currentTimeMillis() - timestamp;
		}
	}

	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();

		final SteelPanel panel = new SteelPanel();

		NetworkPointer pointer = new NetworkPointer(9100);
		// pointer.addListener(panel);
		pointer.setDaemon(true);
		pointer.start();

		final Calibration c = new Calibration(panel);
		pointer.addListener(c);

		frame.getContentPane().add(panel);
		frame.setSize(1024, 768);
		frame.addComponentListener(new WindowInfo(frame));
		frame.setLocation(1400, 0);
		frame.setVisible(true);

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

		final JButton cal = new JButton("Start Calibration");
		cal.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (c.running) {
					c.stop();
				} else {
					Thread t = new Thread(c);
					t.setDaemon(true);
					t.start();
					cal.setText("Stop Calibration");
				}
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