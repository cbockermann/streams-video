/**
 * 
 */
package stream.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
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
public class DisplayImage extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(DisplayImage.class);
	final JFrame frame;
	final ImagePanel imagePanel;
	final JLabel info = new JLabel();
	final SimpleDateFormat timeFormat = new SimpleDateFormat(
			"hh:mm:ss dd-MM-yyyy");

	String key = "frame:data";
	boolean onTop = true;
	boolean initialSize = false;
	String timestamp = "@timestamp";

	public DisplayImage() {
		frame = new JFrame();
		frame.setSize(640, 384);
		frame.getContentPane().setLayout(new BorderLayout());

		imagePanel = new ImagePanel();
		frame.getContentPane().add(imagePanel, BorderLayout.CENTER);
		frame.getContentPane().add(info, BorderLayout.SOUTH);
	}

	/**
	 * @see stream.Processor#process(stream.data.Data)
	 */
	@Override
	public Data process(Data input) {

		Serializable val = input.get(key);
		if (val == null)
			return input;

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

			try {
				if (timestamp != null)
					time = new Long(input.get(timestamp).toString());
			} catch (Exception e) {
				time = null;
			}

			imagePanel.setFrame(image, null);
			if (time != null)
				info.setText(timeFormat.format(new Date(time)));
			frame.repaint();
			frame.validate();
			if (!initialSize) {
				// frame.setSize(image.getWidth(), image.getHeight() + 20);
				frame.pack();
				initialSize = true;
			}
			if (!frame.isVisible()) {

				frame.setVisible(true);
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

	public static class ImagePanel extends JPanel {

		/** The unique class ID */
		private static final long serialVersionUID = 3182958661267766150L;
		final SimpleDateFormat fmt = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");
		BufferedImage frame = null;
		Long timestamp = null;

		public void paint(Graphics g) {
			super.paint(g);
			if (frame != null) {
				// log.info("Drawing frame {}", frame);
				g.drawImage(frame, 0, 0, null);
			}

			if (timestamp != null) {
				g.setColor(Color.WHITE);
				g.drawString(fmt.format(new Date(timestamp)), 4, 20);
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
}