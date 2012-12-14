/**
 * 
 */
package stream.laser;

import java.awt.Color;
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
	final Marker mark;
	boolean running = false;

	public Calibration(SteelPanel panel) {
		this.panel = panel;

		mark = new Marker(512, 384, Color.WHITE);
	}

	/**
	 * @see stream.net.PointerListener#pointingAt(int, int)
	 */
	@Override
	public void pointingAt(int x, int y) {
		PointT marker = new PointT(mark.x.intValue(), mark.y.intValue());
		PointT point = new PointT(x, y);
		panel.cut.add(point);
		if (panel.cut.size() > 30) {
			// panel.cut.remove(0);
		}
		panel.drawableChanged();
		log.info("------------------------------------------------------------");
		log.info("   mark at {},{}", marker.x, marker.y);
		log.info("   laser at {},{}", x, y);
		log.info("------------------------------------------------------------");
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