/**
 * 
 */
package stream.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;
import stream.annotations.Parameter;

/**
 * @author chris
 * 
 */
public abstract class AbstractImageProcessor extends AbstractProcessor
		implements ImageProcessor {

	static Logger log = LoggerFactory.getLogger(AbstractImageProcessor.class);
	protected String imageKey = DEFAULT_IMAGE_KEY;

	/**
	 * @return the data
	 */
	public String getImage() {
		return imageKey;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	@Parameter(description = "The name of the attribute that contains the byte array data of the image.", required = true)
	public void setImage(String data) {
		this.imageKey = data;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		Serializable value = input.get(imageKey);
		if (value instanceof ImageRGB) {
			Data result = process(input, (ImageRGB) value);
			return result;
		}

		byte[] bytes = (byte[]) input.get(imageKey);
		if (bytes == null) {
			return input;
		}

		try {
			BufferedImage bufferedImage = ImageIO
					.read(new ByteArrayInputStream(bytes));
			if (bufferedImage == null) {
				log.debug("No valid JPEG image!");
				return null;
			}
			ImageRGB img = new ImageRGB(bufferedImage);
			Data result = process(input, img);
			return result;
		} catch (Exception e) {
			log.error("Error processing image: {}", e.getMessage());
			if (log.isDebugEnabled())
				e.printStackTrace();
			return input;
		}
	}

	public abstract Data process(Data item, ImageRGB img);
}
