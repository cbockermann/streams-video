/**
 * 
 */
package stream.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.annotations.Parameter;

/**
 * @author chris
 * 
 */
public class Crop extends AbstractImageProcessor {

	static Logger log = LoggerFactory.getLogger(Crop.class);
	String output = "frame:cropped";
	int x = 0;
	int y = 0;
	int width = 10;
	int height = 10;

	/**
	 * @return the output
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * @param output
	 *            the output to set
	 */
	@Parameter(description = "Key/name of the attribute into which the output cropped image is placed, default is 'frame:cropped'.")
	public void setOutput(String output) {
		this.output = output;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	@Parameter(description = "x coordinate of the lower-left corder of the rectangle for cropping, defaults to 0.")
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	@Parameter(description = "y coordinate of the lower-left corder of the rectangle for cropping, defaults to 0.")
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	@Parameter(description = "Width of the rectangle to crop, default is 10.")
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	@Parameter(description = "Height of the rectangle to crop, default is 10.")
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      stream.image.ImageRGB)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		ImageRGB cropped = new ImageRGB(width, height);
		// log.info("Original size is {} x {}", img.getWidth(),
		// img.getHeight());
		// log.info("New size is {} x {}", width, height);

		try {
			for (int i = 0; i < width && i + x < img.getWidth(); i++) {
				for (int j = 0; j < height && j + y < img.getHeight(); j++) {
					cropped.setRGB(i, j, img.getRGB(i + x, j + y));
				}
			}
		} catch (Exception e) {
			log.error("Failed to crop image: {}", e.getMessage());
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
		// log.info("Storing cropped image as '{}'", output);
		item.put(output, cropped);
		return item;
	}
}
