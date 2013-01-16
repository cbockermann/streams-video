/**
 * 
 */
package stream.laser;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.net.PointerListener;

/**
 * @author chris
 * 
 */
public class FakePointer extends JFrame implements PointerListener {

	/** The unique class ID */
	private static final long serialVersionUID = 2382902890546415714L;

	static Logger log = LoggerFactory.getLogger(FakePointer.class);
	PointerListener plistener;

	public FakePointer(PointerListener pl) {
		plistener = pl;
		TouchPanel tp = new TouchPanel(plistener);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tp, BorderLayout.CENTER);
	}

	/**
	 * @see stream.net.PointerListener#pointingAt(int, int)
	 */
	@Override
	public void pointingAt(int x, int y) {
		log.info("Pointing to {},{}", x, y);
	}

	public class TouchPanel extends JPanel {
		/** The unique class ID */
		private static final long serialVersionUID = -7190383631437977979L;
		final PointerListener listener;
		Point pointer;

		public TouchPanel(PointerListener l) {
			listener = l;

			MouseAdapter ml = new MouseAdapter() {
				/**
				 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
				 */
				@Override
				public void mouseMoved(MouseEvent e) {
					listener.pointingAt(e.getX(), e.getY());
					pointer = e.getPoint();
					repaint();
					validate();
				}

				/**
				 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
				 */
				@Override
				public void mouseExited(MouseEvent e) {
					pointer = null;
					repaint();
					validate();
				}
			};
			addMouseMotionListener(ml);
		}

		public void paint(Graphics g) {
			super.paint(g);

			if (pointer != null) {
				g.fillOval(pointer.x - 1, pointer.y - 1, 3, 3);
			}
		}
	}

	public static void main(String[] args) {

		FakePointer fp = new FakePointer(new PointerListener() {
			@Override
			public void pointingAt(int x, int y) {
				log.info("TouchPanel pointing at {},{}", x, y);
			}
		});

		fp.setSize(300, 125);
		fp.setVisible(true);

	}
}