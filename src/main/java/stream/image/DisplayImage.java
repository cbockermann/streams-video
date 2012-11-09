/**
 * 
 */
package stream.image;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
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

	String key = "frame:data";
	boolean onTop = true;

	public DisplayImage() {
		frame = new JFrame();
		frame.setSize(640, 384);
		frame.getContentPane().setLayout(new BorderLayout());

		imagePanel = new ImagePanel();
		frame.getContentPane().add(imagePanel, BorderLayout.CENTER);
	}

	/**
	 * @see stream.Processor#process(stream.data.Data)
	 */
	@Override
	public Data process(Data input) {

		Serializable val = input.get(key);
		if (val == null)
			return input;

		Image image = null;

		if (val instanceof RGBImage) {
			log.info("Found image already as image object!");
			image = ((RGBImage) val).createImage();
		}
		if (val.getClass().isArray()
				&& val.getClass().getComponentType() == byte.class) {
			try {
				log.info("creating image from bytes");
				image = ImageIO.read(new ByteArrayInputStream((byte[]) val));
			} catch (Exception e) {
				log.error("Failed to read image from byte array: {}",
						e.getMessage());
			}
		}

		if (image != null) {
			imagePanel.setFrame(image);
			frame.repaint();
			frame.validate();
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
		Image frame = null;

		public void paint(Graphics g) {
			super.paint(g);
			if (frame != null) {
				// log.info("Drawing frame {}", frame);
				g.drawImage(frame, 0, 0, null);
			}
		}

		public void setFrame(Image frame) {
			this.frame = frame;
			this.repaint();
		}
	}
}