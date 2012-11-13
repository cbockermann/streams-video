package stream.image;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;

public class QuantilizedImage extends AbstractImageProcessor {

	Boolean overwriteInputImage = false;

	/**
	 * Output Name gets ignored if overwriteInputImage is true
	 */
	String output = "frame:quantilizedImage";

	Integer quantilesPerChannel = 4;
	Integer[] borders;

	/**
	 * @see stream.AbstractProcessor#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);

		borders = new Integer[quantilesPerChannel];
		for (int i = 0; i < quantilesPerChannel; i++) {
			borders[i] = (255 / quantilesPerChannel) * i;
		}
	}

	/**
	 * @param overwriteInputImage
	 *            Parameter to define whether the original image should be kept
	 *            or not
	 */
	public void setOverwriteInputImage(Boolean overwriteInputImage) {
		this.overwriteInputImage = overwriteInputImage;
	}

	/**
	 * @return Should the original image be kept or not
	 */
	public Boolean getOverwriteInputImage() {
		return overwriteInputImage;
	}

	/**
	 * @return name of the output image
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * @param output
	 *            the name of the output image. This parameter gets ignored when
	 *            overwriteInputImage is true.
	 */
	public void setOutput(String output) {
		this.output = output;
	}

	/**
	 * @return Number of Quantiles each channel is divided into
	 */
	public Integer getQuantilesPerChannel() {
		return quantilesPerChannel;
	}

	/**
	 * @param quantilesPerChannel
	 *            Set the number of Quantiles each channel in divided into
	 */
	@Parameter(description = "Set the number of Quantiles each channel in divided into.")
	public void setQuantilesPerChannel(Integer quantilesPerChannel) {
		this.quantilesPerChannel = quantilesPerChannel;
	}

	@Override
	public Data process(Data item, ImageRGB img) {

		ImageRGB quantilizedImage = null;

		if (imageKey.equals(output)) {
			quantilizedImage = img;
		} else {
			quantilizedImage = new ImageRGB(img.getWidth(), img.getHeight());
		}

		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {

				// Quantilize red channel
				int red = img.getRED(x, y);

				int i = 1;
				while (i < quantilesPerChannel) {
					if (red < borders[i]) {
						red = borders[i - 1] + (borders[i] - borders[i - 1])
								/ 2;
						i = quantilesPerChannel;
					} else {
						i++;
					}
				}

				quantilizedImage.setRED(x, y, red);

				// Quantilize green channel
				int green = img.getGREEN(x, y);

				i = 1;
				while (i < quantilesPerChannel) {
					if (green < borders[i]) {
						green = borders[i - 1] + (borders[i] - borders[i - 1])
								/ 2;
						i = quantilesPerChannel;
					} else {
						i++;
					}
				}

				quantilizedImage.setGREEN(x, y, green);

				// Quantilize blue channel
				int blue = img.getBLUE(x, y);

				i = 1;
				while (i < quantilesPerChannel) {
					if (blue < borders[i]) {
						blue = borders[i - 1] + (borders[i] - borders[i - 1])
								/ 2;
						i = quantilesPerChannel;
					} else {
						i++;
					}
				}

				quantilizedImage.setBLUE(x, y, blue);

			}
		}

		if (!overwriteInputImage) {
			item.put(output, quantilizedImage);
		}

		return item;
	}

}
