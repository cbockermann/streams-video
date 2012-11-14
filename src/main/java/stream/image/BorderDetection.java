package stream.image;

import stream.Data;
import stream.annotations.Parameter;

/**
 * Border Detection Operator
 * 
 * @author Matthias
 * 
 */
public class BorderDetection extends AbstractImageProcessor {

	String output = "data";

	int tolerance = 0;

	public String getOutput() {
		return output;
	}

	/**
	 * @param output
	 *            The name/key of the output image is stored. If this name
	 *            equals the name of the input image, the input image is going
	 *            to be overwritten.
	 */
	@Parameter(description = "The name/key of the output image is stored. If this name equals the name of the input image, the input image is going to be overwritten.")
	public void setOutput(String output) {
		this.output = output;
	}

	public int getTolerance() {
		return tolerance;
	}

	@Parameter(description = "Tolerance")
	public void setTolerance(int tolerance) {
		this.tolerance = tolerance;
	}

	@Override
	public Data process(Data item, ImageRGB img) {

		final ImageRGB borderimage = new ImageRGB(img.getWidth(),
				img.getHeight());

		// Setting outer pixels to white
		for (int x = 0; x < borderimage.getWidth(); x++) {
			borderimage.setRGB(x, 0, 255, 255, 255);
			borderimage.setRGB(x, borderimage.getHeight() - 1, 255, 255, 255);
		}
		for (int y = 1; y < borderimage.getHeight() - 1; y++) {
			borderimage.setRGB(0, y, 255, 255, 255);
			borderimage.setRGB(borderimage.getWidth() - 1, y, 255, 255, 255);
		}

		// Compute borders for all inner pixels
		// border= black, no border= white
		for (int x = 1; x < img.getWidth() - 1; x++) {
			for (int y = 1; y < img.getHeight() - 1; y++) {

				int sameneighbors = 0;
				if (img.getRGB(x, y) == img.getRGB(x - 1, y)) {
					sameneighbors++;
				}
				if (img.getRGB(x, y) == img.getRGB(x + 1, y)) {
					sameneighbors++;
				}
				if (img.getRGB(x, y) == img.getRGB(x, y - 1)) {
					sameneighbors++;
				}
				if (img.getRGB(x, y) == img.getRGB(x, y + 1)) {
					sameneighbors++;
				}

				if (sameneighbors >= 4 - tolerance) {
					borderimage.setRGB(x, y, 255, 255, 255);
				} else {
					borderimage.setRGB(x, y, 0, 0, 0);
				}
			}
		}

		item.put(output, borderimage);
		return item;
	}
}