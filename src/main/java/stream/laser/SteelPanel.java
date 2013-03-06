/**
 * 
 */
package stream.laser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.data.DataFactory;
import stream.laser.game.LaserMazeResult;
import stream.laser.game.LaserMazeResult2;
import stream.laser.game.LevelScore;
import stream.laser.game.NameDialog;
import stream.laser.game.PointT;
import stream.laser.game.TrackListener;
import stream.net.NetworkPointer;
import stream.net.PointerListener;
import stream.runtime.ProcessContextImpl;

import com.lowagie.text.Font;

/**
 * @author chris
 * 
 */
public class SteelPanel extends JPanel implements PointerListener {

	/** The unique class ID */
	private static final long serialVersionUID = -1683540875635107889L;

	static Logger log = LoggerFactory.getLogger(SteelPanel.class);

	int level = 0;
	String[] levels = new String[] { "/laser/game/images/background8.png",
			"/laser/game/images/background9.png",
			"/laser/game/images/background10.png"
	// "/laser/game/images/background3.png",
	};
	final LaserMazeResult result = new LaserMazeResult();
	final LaserMazeResult2 scorer = new LaserMazeResult2();
	final LevelScore levelScore = new LevelScore();
	boolean debug = "true".equalsIgnoreCase(System.getProperty("debug"));
	TrackListener trackListener;
	String name = null;
	URL currentLevelUrl;
	LaserSword sword = new LaserSword();
	ImageIcon icon;
	ArrayList<PointT> cut = new ArrayList<PointT>();
	int flameWidth = 20;
	int flameHeight = 20;
	PointT lastPoint = null;
	final List<Drawable> drawables = new ArrayList<Drawable>();
	BufferedImage backgroundImage;

	Point start;
	Point end;
	int radius = 25;
	int state = 0;
	boolean flame = false;
	Double score = null;

	Long startTime = 0L;
	Long endTime = 0L;
	long errors = 0L;
	long onPath = 0L;
	Trapez trapez = null;
	boolean drawTrapez = false;
	LaserSound sound = new LaserSound();
	private List<Point> pts;
	private List<Point> chkPts;

	public SteelPanel() {
		this.addMouseMotionListener(sword);
		this.addMouseListener(sword);
		icon = sword.getFlame();
		pts = new ArrayList<Point>();
		chkPts = new ArrayList<Point>();

		this.setBackground(Color.BLUE);
		sound.init();

		try {
			this.trackListener = new TrackListener();
			this.trackListener.init(new ProcessContextImpl());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
		}
	}

	public void setTrapez(Trapez t) {
		this.trapez = t;
	}

	public void resetState() {
		state = 0;
		startTime = 0L;
		endTime = 0L;
		errors = 0;
		onPath = 0;
		lastPoint = null;
		repaint();
		validate();
		cut.clear();
	}

	public void setLevel(int lev) {
		level = lev % levels.length;
		URL url = SteelPanel.class.getResource(levels[level]);
		setBackgroundImage(url);
		resetState();
	}

	public void setBackgroundImage(URL url) {
		try {
			log.debug("Reading image from {}", url);
			backgroundImage = ImageIO.read(url);

			BufferedImage bi = new BufferedImage(this.getWidth(),
					this.getHeight(), backgroundImage.getType());
			Graphics2D g2 = bi.createGraphics();
			g2.drawImage(backgroundImage, 0, 0, bi.getWidth(), bi.getHeight(),
					null);
			g2.dispose();
			backgroundImage = bi;

			start = null;
			end = null;
			log.debug("Background image is: {}", backgroundImage);
			Set<String> colors = new TreeSet<String>();
			for (int x = 0; x < backgroundImage.getWidth(); x++) {
				for (int y = 0; y < backgroundImage.getHeight(); y++) {

					int argb = backgroundImage.getRGB(x, y);

					int r = (argb >> 16) & 0xff;
					int g = (argb >> 8) & 0xff;
					int b = argb & 0xff;
					String c = "(" + r + "," + g + "," + b + ")";
					colors.add(c);

					if (start == null && r == 181 && g == 0 && b == 18) {
						start = new Point(x, y);
						log.debug("Found start-point at {}", start);
					}

					if (start == null && r == 255 && g == 255 && b == 255) {
						start = new Point(x, y);
						log.debug("Found start-point at {}", start);
					}

					if (start == null && r == 226 && g == 45 && b == 45) {
						start = new Point(x, y);
						log.debug("Found start-point at {}", start);
					}

					if (end == null && r == 0 && g == 0 && b == 199) {
						end = new Point(x, y);
						log.debug("Found end-point at {}", end);
					}

					if (end == null && r == 45 && g == 45 && b == 226) {
						end = new Point(x, y);
						log.debug("Found end-point at {}", end);
					}

					drawRand(x, y, backgroundImage, pts);
				}
			}

			log.info("colors: {}", colors);

			repaint();
			validate();
			this.currentLevelUrl = url;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void drawRand(int x, int y, BufferedImage img, List<Point> pts) {

		int r = 0;
		int g = 255;
		int b = 255;
		int color = r;
		color = (color << 8) + g;
		color = (color << 8) + b;
		// Test TopRigj

		//
		if (test1(x, y, img)) {
			img.setRGB(x, y, color);
			pts.add(new Point(x, y));
			return;
		}
		if (test2(x, y, img)) {
			img.setRGB(x, y, color);
			pts.add(new Point(x, y));
			return;
		}

		if (test3(x, y, img)) {
			img.setRGB(x, y, color);
			pts.add(new Point(x, y));
			return;
		}

	}

	private boolean test1(int x, int y, BufferedImage img) {
		int xk = x - 1;
		int yk = y + 1;
		boolean t1 = testColor(xk, yk, 0, 0, 0, img);

		if (t1) {
			xk = x + 1;
			yk = y - 1;
			boolean t2 = testColor(xk, yk, 0, 0, 0, img);
			if (t2) {
				xk = x + 1;
				yk = y + 1;
				boolean t3 = testColor(xk, yk, 156, 156, 156, img);
				if (t3) {
					xk = x - 1;
					yk = y - 1;
					return testColor(xk, yk, 0, 0, 0, img);
				}
				return false;
			}
			return false;
		}
		return false;

	}

	private boolean test2(int x, int y, BufferedImage img) {
		int xk = x - 1;
		int yk = y - 1;
		boolean t1 = testColor(xk, yk, 0, 0, 0, img);

		if (t1) {
			xk = x + 1;
			yk = y + 1;
			boolean t2 = testColor(xk, yk, 0, 0, 0, img);
			if (t2) {
				xk = x + 1;
				yk = y - 1;
				boolean t3 = testColor(xk, yk, 156, 156, 156, img);
				if (t3) {
					xk = x - 1;
					yk = y + 1;
					return testColor(xk, yk, 0, 0, 0, img);
				}
				return false;
			}
			return false;
		}
		return false;

	}

	private boolean test3(int x, int y, BufferedImage img) {
		int xk = x + 1;
		int yk = y + 1;
		boolean t1 = testColor(xk, yk, 0, 0, 0, img);

		if (t1) {
			xk = x - 1;
			yk = y - 1;
			boolean t2 = testColor(xk, yk, 156, 156, 156, img);
			if (t2) {
				xk = x - 1;
				yk = y + 1;
				boolean t3 = testColor(xk, yk, 156, 156, 156, img);
				if (t3) {
					xk = x + 1;
					yk = y - 1;
					return testColor(xk, yk, 156, 156, 156, img);
				}
				return false;
			}
			return false;
		}
		return false;

	}

	private boolean testColor(int x, int y, int r, int g, int b,
			BufferedImage img) {
		if (x > -1 && y > -1 && x < img.getWidth() && y < img.getHeight()) {
			int argb = img.getRGB(x, y);

			int rt = (argb >> 16) & 0xff;
			int gt = (argb >> 8) & 0xff;
			int bt = argb & 0xff;

			if (rt == r && gt == g && bt == b)
				return true;
		}
		return false;

	}

	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;

		if (backgroundImage != null) {
			g2.drawImage(backgroundImage, 0, 0, this.getWidth(),
					this.getHeight(), null);
		}

		PointT last = null;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(2.75f));

		if (start != null) {
			g2.setColor(Color.BLUE);
			g2.drawOval(start.x - radius, start.y - radius, radius * 2,
					radius * 2);

			if (lastPoint != null && state == 0) {
				if (start.distance(lastPoint) < radius) {
					state = 1;
				}
			}
		}

		if (end != null) {
			g2.setColor(Color.RED);
			g2.drawOval(end.x - radius, end.y - radius, radius * 2, radius * 2);
		}

		g.setColor(Color.white);
		String nameInfo = "Name: " + name;
		Rectangle2D rect = g2.getFont().getStringBounds(nameInfo,
				g2.getFontRenderContext());

		g2.drawString(nameInfo, getWidth() - (int) rect.getWidth() - 4,
				0 + (int) rect.getHeight() + 0);

		String info = "State is: " + state + " (Level " + level + ")";
		rect = g2.getFont().getStringBounds(info, g2.getFontRenderContext());
		g2.drawString(info, getWidth() - (int) rect.getWidth() - 4,
				0 + (int) rect.getHeight() + 14);

		if (startTime > 0) {
			if (state == 2) {
				Long elapsed = System.currentTimeMillis() - startTime;
				if (endTime > 0)
					elapsed = endTime - startTime;
				if (debug) {
					g2.drawString("Time elapsed: " + elapsed + " ms",
							getWidth() - 300, 45);
					g2.drawString("Errors: " + errors, getWidth() - 300, 60);
					g2.drawString("On path: " + onPath, getWidth() - 300, 75);
				}
				if (score != null) {
					java.awt.Font bold = g2.getFont().deriveFont(Font.BOLD)
							.deriveFont(36.0f);
					g2.setFont(bold);

					DecimalFormat fmt = new DecimalFormat("0");
					String scoreString = "Your Score: " + fmt.format(score);

					rect = g2.getFont().getStringBounds(scoreString,
							g2.getFontRenderContext());

					g2.drawString(scoreString,
							(getWidth() / 2) - ((int) rect.getWidth() / 2),
							getHeight() / 2);
				}
			}
		}

		g.setColor(Color.RED);
		ArrayList<PointT> points = this.cut;
		for (int i = 0; i < points.size(); i++) {
			PointT p = points.get(i);
			if (last == null)
				last = p;

			g.drawLine(last.x, last.y, p.x, p.y);
			last = p;
			if (i + 1 >= points.size()) {
				if (icon != null && flame) {
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

		if (trapez != null && drawTrapez) {
			trapez.draw(g2);
		}
		// for (int i = 0; i < pts.size(); i++) {
		// Point pi = pts.get(i);
		// g2.drawOval(pi.x, pi.y, 10, 10);
		// }

	}

	public static void main(String[] args) throws Exception {

		final JFrame frame = new JFrame();
		// frame.setUndecorated(true);
		System.setProperty("trapezKorrektur", "true");

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		screen = new Dimension(1024, 768);

		final SteelPanel panel = new SteelPanel();

		InetSocketAddress addr = null;
		if (args.length > 1) {
			addr = new InetSocketAddress(args[0], new Integer(args[1]));
		}

		if (args.length > 0) {
			addr = new InetSocketAddress(args[0], 9100);
		}

		int marginTop = new Integer(System.getProperty("marginTop", "36"));
		Integer w = new Integer(System.getProperty("width", "" + screen.width));
		Integer h = new Integer(System.getProperty("height", ""
				+ (screen.height - marginTop)));
		Integer x = new Integer(System.getProperty("x", "0"));
		Integer y = new Integer(System.getProperty("y", "" + (0 + marginTop)));

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

		panel.setLevel(0);

		final Calibration c = new Calibration(panel);
		if (pointer != null)
			pointer.addListener(c);

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == 'c' || e.getKeyChar() == 'r') {
					panel.clearChkPts();
					log.info("Performing clear-action...");
					panel.resetState();
				}

				if (e.getKeyChar() == 'C') {
					log.info("Performing calibration-action...");
					c.nextMark();
				}

				if (e.getKeyChar() == 's') {
					panel.state = (panel.state + 1) % 3;
					panel.repaint();
					panel.validate();
				}

				if (e.getKeyChar() == 'p') {
					log.info("Enable Piatkowski-Correction...");
					System.setProperty("trapezKorrektur", "true");
				}

				if (e.getKeyChar() == 't') {
					panel.drawTrapez = !panel.drawTrapez;
					panel.repaint();
					panel.validate();
				}

				if (e.getKeyChar() == 'f') {
					panel.flame = !panel.flame;
				}

				if (e.getKeyChar() == 'n') {
					NameDialog d = new NameDialog(frame);
					d.center();
					d.setVisible(true);
					panel.setUserName(d.getName());
				}

				if (e.getKeyChar() == 'l') {
					panel.clearPts();
					panel.level = (panel.level + 1) % panel.levels.length;
					panel.setLevel(panel.level);
					panel.repaint();
					panel.validate();
				}
			}
		});

		panel.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				// System.out.println("Mouse at " + e);
				if (e.isShiftDown()) {
					if (c != null) {
						c.pointingAt(e.getX(), e.getY());
					}
				}
			}
		});
	}

	private void clearPts() {
		pts.clear();
	}

	private void clearChkPts() {
		chkPts.clear();
	}

	public void setUserName(String n) {
		this.name = n;
		repaint();
		validate();
	}

	/**
	 * @see stream.net.PointerListener#pointingAt(int, int)
	 */
	@Override
	public void pointingAt(int x, int y) {
		// log.info("Pointer at {},{}", x, y);

		lastPoint = new PointT(x, y);
		boolean inCircle = false;
		sword.active = true;

		Data item = DataFactory.create();

		if (name != null) {
			item.put("user", name);
		}

		item.put("level:", this.level);

		item.put("track:x", x);
		item.put("track:y", y);

		if (start != null && state == 0) {
			// log.info("Checking point {} against start cirlce {}", lastPoint,
			// start);
			if (start.distance(new Point(x, y)) < radius) {
				chkPts.clear();
				chkPts.addAll(pts);
				log.info("Pointer in start-circle!");
				state = 1;
				startTime = System.currentTimeMillis();
				inCircle = inCircle || true;
				sound.play("on0");

				if (currentLevelUrl != null && backgroundImage != null) {
					item.put("image:url", currentLevelUrl.toString());
					item.put("image:width", backgroundImage.getWidth());
					item.put("image:height", backgroundImage.getHeight());
				}

				item.put("track:point", "start");
			}
		}

		if (end != null && state == 1) {

			if (end.distance(new Point(x, y)) < radius
					&& Math.abs(chkPts.size() - pts.size()) > 10) {
				log.info("#chkPoints {}", chkPts.size());
				log.info("Point in end-circle!");
				state = 2;
				endTime = System.currentTimeMillis();
				inCircle = inCircle || true;
				sound.play("off0");
				item.put("level", "" + this.level);
				item.put("track:point", "end");
				item.put("time", (endTime - startTime));
				item.put("onpath", this.onPath);
				item.put("error", this.errors);
				item = result.process(item);
				item = scorer.process(item);
				log.info("item: {}", item);
				item = levelScore.process(item);
				log.info("mapped: {}", item);

				try {
					if (item.containsKey("@punkte")) {
						score = new Double("" + item.get("@punkte"));
					} else {
						score = Double.NaN;
					}
					// this.score = new Double("" + item.get("@result2"));
				} catch (Exception e) {
					this.score = null;
				}

				if (trackListener != null)
					trackListener.process(item);
			}
		}

		if (state == 1)
			this.cut.add(lastPoint);

		if (backgroundImage != null) {

			if (x < 0 || x > backgroundImage.getWidth())
				return;

			if (y < 0 || y > backgroundImage.getHeight()) {
				return;
			}

			try {
				checkPoints(x, y);

				// log.info("Color: (" + r + "," + g + "," + b + ")");

				if (!inCircle && state == 1) {

					int argb = backgroundImage.getRGB(x, y);

					int r = (argb >> 16) & 0xff;
					int g = (argb >> 8) & 0xff;
					int b = argb & 0xff;

					if (r == 156 && g == 156 && b == 156) {
						onPath++;
						// sound.play("");
					} else {
						int count = calcErrorIntensity(x, y, backgroundImage);
						if (count > 0)
							errors++;
						else
							errors = errors + 3;
						// log.info("Playing swing-sound...");
						// sound.play("swing7");
					}
				}
			} catch (Exception e) {

			}
		}

		if (state == 1 && trackListener != null)
			trackListener.process(item);

		this.repaint();
		this.validate();
	}

	private void checkPoints(int x, int y) {
		int r = -1;
		Point p = new Point(x, y);
		for (int i = 0; i < chkPts.size(); i++) {
			Point pi = chkPts.get(i);
			if (pi.distance(p) < 60) {
				r = i;
				break;
			}

		}
		if (r > -1) {
			chkPts.remove(r);
		}

	}

	private int calcErrorIntensity(int x, int y, BufferedImage img) {
		int size = 10;
		int count = 0;

		// right
		int xk = x + size;
		int yk = y;
		if (x < img.getWidth()) {
			int argb = img.getRGB(xk, yk);

			int r = (argb >> 16) & 0xff;
			int g = (argb >> 8) & 0xff;
			int b = argb & 0xff;
			if (!(r == 156 && g == 156 && b == 156))
				count++;
		}
		// left
		xk = x - size;
		yk = y;
		if (x > 0) {
			int argb = img.getRGB(xk, yk);
			int r = (argb >> 16) & 0xff;
			int g = (argb >> 8) & 0xff;
			int b = argb & 0xff;
			if (!(r == 156 && g == 156 && b == 156))
				count++;
		}
		// top
		xk = x;
		yk = y + size;
		if (y < img.getHeight()) {
			int argb = img.getRGB(xk, yk);
			int r = (argb >> 16) & 0xff;
			int g = (argb >> 8) & 0xff;
			int b = argb & 0xff;
			if (!(r == 156 && g == 156 && b == 156))
				count++;
		}
		// bottom
		xk = x;
		yk = y - size;
		if (y > 0) {
			int argb = img.getRGB(xk, yk);
			int r = (argb >> 16) & 0xff;
			int g = (argb >> 8) & 0xff;
			int b = argb & 0xff;
			if (!(r == 156 && g == 156 && b == 156))
				count++;
		}
		return count;
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
