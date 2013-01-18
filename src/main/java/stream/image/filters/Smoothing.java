package stream.image.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * The Smooting Operator is a filter to reduce the noise in an input image. Each
 * pixel gets replaced by the average of pixels in a square window surrounding
 * this pixel.
 * 
 * @author Matthias
 * 
 */
public class Smoothing extends AbstractImageProcessor {

	static Logger log = LoggerFactory.getLogger(Smoothing.class);
	String output = "smooth";

	Integer windowSize = 3;

	Boolean weighted = false;

	int[][] weightingMatrix = null;
	int weightsum = 0;

	/**
	 * @return the windowSize
	 */
	public Integer getWindowSize() {
		return windowSize;
	}

	/**
	 * @param windowSize
	 *            the windowSize to set
	 */
	@Parameter(description="Sets the window size. The window size determines the neighboring pixels for each pixel, that are averaged. A windowSize of 3 means that 3 times 3 = 9 pixels are taken into account.")
	public void setWindowSize(Integer windowSize) {
		this.windowSize = windowSize;
	}

	@Parameter(description="If a weighted smoothing technique is selected, neighbors closer to the pixel to be smoothend are counted with a higher weight.")
	public void setWeighted(Boolean weighted) {
		this.weighted = weighted;
	}
	
	/**
	 * @return name The name/key under which the smoothed image is stored.
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * Setter for the Parameter Output
	 * 
	 * @param output
	 *            The name/key under which the smoothed Image is stored. If this
	 *            name equals the name of the input image, the input image is
	 *            going to be overwritten.
	 */
	@Parameter(description = "The name/key of the output image is stored. If this name equals the name of the input image, the input image is going to be overwritten.")
	public void setOutput(String output) {
		this.output = output;
	}

	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);

		weightingMatrix = new int[windowSize][windowSize];
		weightsum = 0;

		if (!weighted) {
			for (int i = 0; i < weightingMatrix.length; i++) {
				for (int j = 0; j < weightingMatrix[i].length; j++) {
					weightingMatrix[i][j] = 1;
					weightsum++;
				}
			}
		} else {
			// TODO: Not yet implemented.
		}
	}

	@Override
	public Data process(Data item, ImageRGB img) {

		ImageRGB smoothedImage = new ImageRGB(img.getWidth(), img.getHeight());
		int border = (windowSize / 2);

		// TODO: Noch sehr unsch�n durch das von 1/1 bis Width-1/Height-1
		for (int x = border; x < smoothedImage.getWidth() - border; x++) {
			for (int y = border; y < smoothedImage.getHeight() - border; y++) {

				int red = 0;
				for (int i = 0; i < windowSize; i++) {
					for (int j = 0; j < windowSize; j++) {
						red = red + weightingMatrix[i][j]
								* img.getRED(x - border + i, y - border + j);
					}
				}
				red = red / weightsum;

				int green = 0;
				for (int i = 0; i < windowSize; i++) {
					for (int j = 0; j < windowSize; j++) {
						green = green + weightingMatrix[i][j]
								* img.getGREEN(x - border + i, y - border + j);
					}
				}
				green = green / weightsum;

				int blue = 0;
				for (int i = 0; i < windowSize; i++) {
					for (int j = 0; j < windowSize; j++) {
						blue = blue + weightingMatrix[i][j]
								* img.getBLUE(x - border + i, y - border + j);
					}
				}
				blue = blue / weightsum;

				smoothedImage.setRGB(x, y, red, green, blue);
			}
		}

		log.info("emitting smoothed image as attribute '{}'", output);
		item.put(output, smoothedImage);
		return item;
	}
}
