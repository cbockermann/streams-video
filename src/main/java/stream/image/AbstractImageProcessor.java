/**
 * 
 */
package stream.image;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import stream.AbstractProcessor;
import stream.Data;
import stream.annotations.Parameter;

/**
 * @author chris
 * 
 */
public abstract class AbstractImageProcessor extends AbstractProcessor {

	String imageKey = "data";

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
			ImageRGB img = new ImageRGB(ImageIO.read(new ByteArrayInputStream(
					bytes)));
			Data result = process(input, img);
			return result;
		} catch (Exception e) {

		}

		return input;
	}

	public abstract Data process(Data item, ImageRGB img);
}
