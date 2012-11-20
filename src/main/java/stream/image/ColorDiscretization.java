package stream.image;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;

/**
 * The Color Discretization Operator discretizes the color space of the input image.
 * 
 * @author Matthias
 *
 */
public class ColorDiscretization extends AbstractImageProcessor {

	String output = "data";

	Integer valuesPerChannel = 4;

	private Integer[] borders;

	/**
	 * @see stream.AbstractProcessor#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);

		borders = new Integer[valuesPerChannel];
		for (int i = 0; i < valuesPerChannel; i++) {
			borders[i] = (255 / valuesPerChannel) * i;
		}
	}

	/**
	 * @return name The name/key under which the quantilized image is stored.
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * Setter for the Parameter Output
	 * @param output The name/key under which the quantilized Image is stored. 
	 * 					If this name equals the name of the input image, the input image
	 * 					is going to be overwritten.
	 */
	@Parameter(description ="The name/key of the output image is stored. If this name equals the name of the input image, the input image is going to be overwritten.")
	public void setOutput(String output) {
		this.output = output;
	}

	/**
	 * @return Number of discrete color values, each channel is divided into
	 */
	public Integer getQuantilesPerChannel() {
		return valuesPerChannel;
	}

	/**
	 * @param quantilesPerChannel
	 *            Set the number of discrete color values, each channel in divided into
	 */
	@Parameter(description = "Set the number of discrete color values, each channel in divided into.")
	public void setQuantilesPerChannel(Integer quantilesPerChannel) {
		this.valuesPerChannel = quantilesPerChannel;
	}

	@Override
	public Data process(Data item, ImageRGB img) {

		ImageRGB discretizedImage = null;

		if (imageKey.equals(output)) {
			discretizedImage = img;
		} else {
			discretizedImage = new ImageRGB(img.getWidth(), img.getHeight());
		}

		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {

				// Discretize red channel
				int red = img.getRED(x, y);

				int i = 1;
				while (i < valuesPerChannel) {
					if (red < borders[i]) {
						red = borders[i - 1] + (borders[i] - borders[i - 1])
								/ 2;
						i = valuesPerChannel;
					} else {
						i++;
					}
				}

				discretizedImage.setRED(x, y, red);

				// Discretize green channel
				int green = img.getGREEN(x, y);

				i = 1;
				while (i < valuesPerChannel) {
					if (green < borders[i]) {
						green = borders[i - 1] + (borders[i] - borders[i - 1])
								/ 2;
						i = valuesPerChannel;
					} else {
						i++;
					}
				}

				discretizedImage.setGREEN(x, y, green);

				// Discretize blue channel
				int blue = img.getBLUE(x, y);

				i = 1;
				while (i < valuesPerChannel) {
					if (blue < borders[i]) {
						blue = borders[i - 1] + (borders[i] - borders[i - 1])
								/ 2;
						i = valuesPerChannel;
					} else {
						i++;
					}
				}

				discretizedImage.setBLUE(x, y, blue);

			}
		}

		
		item.put(output, discretizedImage);

		return item;
	}

}
