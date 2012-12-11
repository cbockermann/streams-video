/**
 * 
 */
package stream.image.laser;

import java.awt.Color;

import stream.Data;
import stream.annotations.Parameter;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * @author chris
 * 
 */
public class Rectangle extends AbstractImageProcessor {

	protected String output = imageKey;
	protected int x = 0;
	protected int y = 0;
	protected int width = 10;
	protected int height = 10;
	protected Color color = Color.BLACK;

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

		int[] orig = img.getPixels();

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int porig = (i + y) * img.getWidth() + (j + x);
				// copy[porig] = orig[porig];
				if (i == 0 || i == height || j == 0 || j == width)
					orig[porig] = 0;
			}
		}

		item.put(output, new ImageRGB(img.getWidth(), img.getHeight(), orig));
		return item;
	}
}
