package stream.image;

import stream.Data;
import stream.annotations.Parameter;

public class ChangedPixelRatio extends AbstractImageProcessor {

	String output = "frame:diff:changeratio";

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
	@Parameter(description = "This parameter sets the key under which the changed pixel ratio is stored in the output data item.")
	public void setOutput(String output) {
		this.output = output;
	}

	@Override
	/**
	 * This operator expects img to be a DiffImage and counts the ratio of pixels, that have
	 * changed significantly. A significant changes is every change, where the value of the
	 * red + green + blue-channel has changes by at least 25.
	 */
	public Data process(Data item, ImageRGB img) {

		int counter = 0;

		for (int i = 0; i < img.getWidth(); i++) {
			for (int j = 0; j < img.getHeight(); j++) {

				int red = img.getRED(i, j);
				int green = img.getGREEN(i, j);
				int blue = img.getBLUE(i, j);

				if (red + green + blue > 25) {
					counter++;
				}

			}
		}

		Double ratio = (double) counter
				/ (double) (img.getHeight() * img.getWidth());

		item.put(output, ratio);

		return item;
	}

}
