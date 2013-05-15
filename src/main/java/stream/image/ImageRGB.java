/**
 * 
 */
package stream.image;

import java.awt.image.BufferedImage;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 * 
 */
public class ImageRGB implements Serializable {

	static Logger log = LoggerFactory.getLogger(ImageRGB.class);
	/** The unique class ID */
	private static final long serialVersionUID = -2042395350522979787L;
	public final int height;
	public final int width;
	public final int[] pixels;

	/**
	 * Creates an empty image with the specified height and width.
	 * 
	 * @param height
	 * @param width
	 */
	public ImageRGB(int width, int height) {
		this.height = height;
		this.width = width;
		pixels = new int[height * width];
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = 0;
		}
	}

	public ImageRGB(int width, int height, int[] data) {
		this.height = height;
		this.width = width;
		pixels = data;
	}

	public ImageRGB(BufferedImage img) {
		this.height = img.getHeight();
		this.width = img.getWidth();
		int[] px;
		try {
			px = new int[width * height];
		} catch (Exception e) {
			log.error(
					"Failed to create image of size {}x{}: " + e.getMessage(),
					width, height);
			px = new int[0];
		}
		pixels = px;
		img.getRGB(0, 0, width, height, pixels, 0, width);
	}

	/**
	 * Returns the RGB(a) value of the specified pixel.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getRGB(int x, int y) {
		int idx = y * width + x;
		return pixels[idx];
	}

	/**
	 * This sets the RGB value for the specified pixel.
	 * 
	 * @param x
	 * @param y
	 * @param rgba
	 * @return
	 */
	public int setRGB(int x, int y, int rgba) {
		int idx = y * width + x;
		pixels[idx] = rgba;
		return pixels[idx];
	}

	// TODO bockermann: Why does it have to return the position in the array as
	// a int?
	// Answer (from bockermann): It doesn't. The value returned is the RGB-value
	// of that pixel as 32-bit integer. NOT the index of the pixel.
	/**
	 * This functions sets the RGB value for the specified pixel.
	 * 
	 * @param x
	 * @param y
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	public int setRGB(int x, int y, int red, int green, int blue) {
		int rgba = red;
		rgba = (rgba << 8) + green;
		rgba = (rgba << 8) + blue;

		return setRGB(x, y, rgba);
	}

	/**
	 * Returns the value of the red color channel
	 * 
	 * @author Matthias
	 * @param x
	 * @param y
	 * @return
	 */
	public int getRED(int x, int y) {
		int rgb = getRGB(x, y);
		int red = (rgb >> 16) & 0xFF;
		return red;
	}

	public void setRED(int x, int y, int red) {
		int rgb = getRGB(x, y);
		int green = (rgb >> 8) & 0xFF;
		int blue = rgb & 0xFF;

		int rgbnew = red;
		rgbnew = (rgbnew << 8) + green;
		rgbnew = (rgbnew << 8) + blue;

		setRGB(x, y, rgbnew);
	}

	/**
	 * Returns the value of the green color channel
	 * 
	 * @author Matthias
	 * @param x
	 * @param y
	 * @return
	 */
	public int getGREEN(int x, int y) {
		int rgb = getRGB(x, y);
		int green = (rgb >> 8) & 0xFF;
		return green;
	}

	public void setGREEN(int x, int y, int green) {
		int rgb = getRGB(x, y);
		int red = (rgb >> 16) & 0xFF;
		int blue = rgb & 0xFF;

		int rgbnew = red;
		rgbnew = (rgbnew << 8) + green;
		rgbnew = (rgbnew << 8) + blue;

		setRGB(x, y, rgbnew);
	}

	/**
	 * Returns the value of the blue color channel
	 * 
	 * @author Matthias
	 * @param x
	 * @param y
	 * @return
	 */
	public int getBLUE(int x, int y) {
		int rgb = getRGB(x, y);
		int blue = rgb & 0xFF;
		return blue;
	}

	public void setBLUE(int x, int y, int blue) {
		int rgb = getRGB(x, y);
		int green = (rgb >> 8) & 0xFF;
		int red = (rgb >> 16) & 0xFF;

		int rgbnew = red;
		rgbnew = (rgbnew << 8) + green;
		rgbnew = (rgbnew << 8) + blue;

		setRGB(x, y, rgbnew);
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return The pixels of this image as concatenated rows.
	 */
	public int[] getPixels() {
		return pixels;
	}

	public BufferedImage createImage() {
		BufferedImage buf = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		buf.setRGB(0, 0, width, height, pixels, 0, width);
		return buf;
	}
}