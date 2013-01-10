/**
 * 
 */
package stream.laser;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.laser.SteelPanel.PointT;
import stream.net.PointerListener;

/**
 * @author Christian Bockermann
 * 
 */
public class Calibration implements PointerListener, Runnable {

	static Logger log = LoggerFactory.getLogger(Calibration.class);
	final SteelPanel panel;
	Marker mark;
	boolean running = false;

	Long last = 0L;
	PointT lastPoint = null;

	double offX = 0;
	double offY = 0;
	double scaleX = 1.0;
	double scaleY = 1.0;
	int lastMark = -1;
	List<Marker> calibrationMarks = new ArrayList<Marker>();
	Map<Marker, PointT> calibrationPoints = new LinkedHashMap<Marker, PointT>();

	public Calibration(SteelPanel panel) {
		this.panel = panel;

		int border = 8;

		int w = panel.getWidth();
		int h = panel.getHeight();

		log.info("Panel size is: {} x {}", panel.getWidth(), panel.getHeight());

		calibrationMarks.add(new Marker(border, border, Color.white));
		calibrationMarks.add(new Marker(border, h - border, Color.WHITE));
		calibrationMarks.add(new Marker(w - border, border, Color.WHITE));
		calibrationMarks.add(new Marker(w - border, h - border, Color.WHITE));
		calibrationMarks.add(new Marker(-1, -1, Color.white));

		// panel.addMouseMotionListener(new MouseAdapter() {
		// /**
		// * @see
		// java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
		// */
		// @Override
		// public void mouseMoved(MouseEvent e) {
		// super.mouseMoved(e);
		// System.out.println(e);
		// }
		// });
	}

	public void nextMark() {
		lastMark++;
		if (mark != null)
			panel.remove(mark);

		mark = calibrationMarks.get(lastMark % calibrationMarks.size());
		if (mark.x < 0 && mark.y < 0) {
			log.info("Skipping empty calibration mark...");
			log.info("Calibration marks are finished, current setup is:");

			for (Marker mark : this.calibrationPoints.keySet()) {
				log.info("   Marker: {}", mark);
				log.info("   PointT: {}", calibrationPoints.get(mark));
			}

			Marker tlm = calibrationMarks.get(0);
			Marker blm = calibrationMarks.get(1);
			Marker trm = calibrationMarks.get(2);
			Marker brm = calibrationMarks.get(3);

			PointT tl = calibrationPoints.get(tlm);
			PointT bl = calibrationPoints.get(blm);
			PointT tr = calibrationPoints.get(trm);
			PointT br = calibrationPoints.get(brm);

			offX = tl.x;
			offY = tl.y;

			scaleX = 1.0d / ((tr.x - tl.x) / (trm.x - tlm.x));
			scaleY = 1.0d / ((bl.y - tl.y) / (blm.y - tlm.y));

			log.info("off-x: {}, off-y: {}", offX, offY);
			log.info("scale-x: {}, scale-y: {}", scaleX, scaleY);

		} else {
			log.info("Current mark is at: {},{}", mark.x, mark.y);
			panel.add(mark);
		}
		panel.drawableChanged();
	}

	/**
	 * @see stream.net.PointerListener#pointingAt(int, int)
	 */
	@Override
	public void pointingAt(int x, int y) {
		// if (last > 0)
		// log.info("Last point was {} ms ago", System.currentTimeMillis()
		// - last);
		// log.info("------------------------------------------------------------");
		// log.info("   mark at {},{}", mark.x, mark.y);
		// log.info("   laser at {},{}", lastPoint.x, lastPoint.y);
		// log.info("------------------------------------------------------------");

		PointT orig = new PointT(x, y);
		PointT point = new PointT((x - offX) * scaleX, (y - offY) * scaleY);
		log.info("Mapping {} to {}", orig, point);

		if (mark != null && mark.x >= 0 && mark.y >= 0) {
			log.info("Associating mark {} with {}", mark, lastPoint);
			calibrationPoints.put(mark, lastPoint);
		}

		panel.cut.add(point);
		// if (panel.cut.size() > 30) {
		// panel.cut.remove(0);
		// }

		lastPoint = new PointT(x, y);
		panel.drawableChanged();
		last = System.currentTimeMillis();
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		int i = 0;
		Random rnd = new Random();
		panel.add(mark);
		running = true;
		int x = mark.x.intValue();
		int y = mark.y.intValue();
		int sleep = 200;

		int dirx = 1;
		int diry = 1;

		while (running) {

			// if (++i % 2 == 0) {
			// mark.setColor(panel.getBackground());
			// } else {
			// mark.setColor(Color.RED);
			// }
			mark.setColor(Color.RED);

			int dx = rnd.nextInt(5);
			int dy = rnd.nextInt(5);
			if (mark.x < 10)
				dirx = 1;

			if (mark.x > 1014)
				dirx = -1;

			if (mark.y < 10)
				diry = 1;
			if (mark.y > 758)
				diry = -1;

			mark.translate(dirx * dx, diry * dy);
			// log.info("Painting mark at {},{}", mark.x, mark.y);
			panel.drawableChanged();
			sleep(10);
		}
		panel.remove(mark);
		panel.drawableChanged();
	}

	protected void sleep(int s) {
		try {
			Thread.sleep(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		running = false;
	}
}