/**
 * 
 */
package stream.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;

/**
 * @author chris
 * 
 */
public class DisplayImage extends AbstractProcessor implements WindowListener {

	static Logger log = LoggerFactory.getLogger(DisplayImage.class);
	final JFrame frame;
	final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
	final ImagePanel imagePanel;
	final JLabel info = new JLabel();
	final SimpleDateFormat timeFormat = new SimpleDateFormat(
			"HH:mm:ss dd-MM-yyyy");

	String key = "frame:data";
	boolean onTop = true;
	boolean initialSize = false;
	String timestamp = "@timestamp";
	String onClose = "";
	boolean closing = false;
	AtomicBoolean stopped = new AtomicBoolean(false);
	AtomicLong frameNumber = new AtomicLong(0L);
	long id = 0;

	public DisplayImage() {
		frame = new JFrame();
		frame.setSize(640, 384);
		frame.getContentPane().setLayout(new BorderLayout());

		final JButton play = new JButton("start/stop");
		play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (stopped) {
					boolean val = stopped.get();
					stopped.set(!val);
					stopped.notifyAll();
				}

				if (stopped.get())
					play.setText("Start");
				else
					play.setText("Stop");
			}
		});
		buttons.add(play);
		frame.getContentPane().add(buttons, BorderLayout.NORTH);

		imagePanel = new ImagePanel();
		frame.getContentPane().add(imagePanel, BorderLayout.CENTER);
		frame.getContentPane().add(info, BorderLayout.SOUTH);
		frame.addWindowListener(this);

		info.setText("");

		imagePanel.addMouseMotionListener(new MouseAdapter() {
			/**
			 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				int x = e.getX();
				int y = e.getY();
				String fn = "frame: " + frameNumber.longValue() + ", ";
				if (imagePanel.frame != null) {
					int rgb = imagePanel.frame.getRGB(x, y);

					int red = (rgb >> 16) & 0xFF;
					int green = (rgb >> 8) & 0xFF;
					int blue = rgb & 0xFF;
					info.setText(fn + " x: " + x + ", y: " + y + ", RGB = ("
							+ red + " / " + green + " / " + blue + ")");
				} else {
					info.setText(fn + "x: " + x + ", y: " + y);
				}
			}
		});

	}

	/**
	 * @see stream.Processor#process(stream.data.Data)
	 */
	@Override
	public Data process(Data input) {

		if (closing)
			return input;

		Serializable val = input.get(key);
		if (val == null) {
			log.error("No image for key '{}' found!", key);
			return input;
		}

		BufferedImage image = null;

		if (val instanceof ImageRGB) {
			log.debug("Found image already as image object!");
			image = ((ImageRGB) val).createImage();
		}
		if (val.getClass().isArray()
				&& val.getClass().getComponentType() == byte.class) {
			try {
				log.debug("creating image from bytes");
				image = ImageIO.read(new ByteArrayInputStream((byte[]) val));

			} catch (Exception e) {
				log.error("Failed to read image from byte array: {}",
						e.getMessage());
				return input;
			}
		}

		if (image != null) {
			Long time = null;

			frameNumber.incrementAndGet();
			try {
				if (timestamp != null)
					time = new Long(input.get(timestamp).toString());
			} catch (Exception e) {
				time = null;
			}

			log.debug("Updating image...");
			imagePanel.setFrame(image, null);
			if (time != null) {
				info.setText(timeFormat.format(new Date(time)));
				info.repaint();
				info.validate();
			}
			frame.repaint();
			frame.validate();
			imagePanel.repaint();
			if (!initialSize) {
				// frame.setSize(image.getWidth(), image.getHeight() + 20);
				frame.pack();
				initialSize = true;
			}

			if (!frame.isVisible()) {

				frame.setVisible(true);
			}
		}

		while (this.stopped.get()) {
			synchronized (stopped) {
				try {
					stopped.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		return input;
	}

	/**
	 * @see stream.AbstractProcessor#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);

		if (onTop) {
			frame.setAlwaysOnTop(true);
		}

		frame.setVisible(true);
	}

	/**
	 * @see stream.AbstractProcessor#finish()
	 */
	@Override
	public void finish() throws Exception {
		super.finish();
		synchronized (stopped) {
			stopped.set(false);
			this.frame.setVisible(false);
		}
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	@Parameter(description = "The name/key of the attribute containing the RGB image object, defaults to `image`.")
	public void setImage(String img) {
		this.key = img;
	}

	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the onTop
	 */
	public boolean isOnTop() {
		return onTop;
	}

	/**
	 * @param onTop
	 *            the onTop to set
	 */
	public void setOnTop(boolean onTop) {
		this.onTop = onTop;
	}

	/**
	 * @return the onClose
	 */
	public String getOnClose() {
		return onClose;
	}

	/**
	 * @param onClose
	 *            the onClose to set
	 */
	@Parameter(description = "If set to `shutdown`, closing the window will shutdown the JVM (ie. the process container).")
	public void setOnClose(String onClose) {
		this.onClose = onClose;
	}

	public static class ImagePanel extends JPanel {

		/** The unique class ID */
		private static final long serialVersionUID = 3182958661267766150L;
		final SimpleDateFormat fmt = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");
		BufferedImage frame = null;
		Long timestamp = null;

		public ImagePanel() {
			setBackground(Color.WHITE);
		}

		public void paint(Graphics g) {

			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			super.paint(g);
			int offx = 0;
			int offy = 0;
			if (frame != null) {

				offx = Math.abs((frame.getWidth() - this.getWidth()) / 2);
				offy = Math.abs((frame.getHeight() - this.getHeight()) / 2);

				log.debug("Drawing frame {}", frame);
				g.drawImage(frame, offx, offy, null);

			}

			if (timestamp != null) {
				g.setColor(Color.WHITE);
				g.drawString(fmt.format(new Date(timestamp)), 4 + offx,
						20 + offy);
			}
		}

		public void setFrame(BufferedImage frame, Long timestamp) {
			this.timestamp = timestamp;
			this.frame = frame;
			this.repaint();
		}

		/**
		 * @see javax.swing.JComponent#getPreferredSize()
		 */
		@Override
		public Dimension getPreferredSize() {
			return getMaximumSize();
		}

		/**
		 * @see javax.swing.JComponent#getMaximumSize()
		 */
		@Override
		public Dimension getMaximumSize() {
			if (frame == null) {
				return new Dimension(320, 240);
			}
			return new Dimension(frame.getWidth(), frame.getHeight());
		}

		/**
		 * @see javax.swing.JComponent#getMinimumSize()
		 */
		@Override
		public Dimension getMinimumSize() {
			return getMaximumSize();
		}

	}

	/**
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowOpened(WindowEvent e) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		closing = true;
		log.info("windowClosing: {}", e);
		if ("shutdown".equalsIgnoreCase(onClose)) {
			log.info("Shutting down the container...");
			System.exit(0);
		} else {
			log.info("Window closed.");
		}
	}

	/**
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent e) {
		log.info("windowClosed: {}", e);
	}

	/**
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowIconified(WindowEvent e) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowActivated(WindowEvent e) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}