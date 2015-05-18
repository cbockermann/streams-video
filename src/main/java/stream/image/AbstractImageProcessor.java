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
	String output = "data";

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
	@Parameter(description = "The name of the attribute that contains the byte array data of the encoded image or the ImageRGB object (if previously been decoded). Default value is: `"
			+ ImageProcessor.DEFAULT_IMAGE_KEY + "`.", required = false)
	public void setImage(String data) {
		this.imageKey = data;
	}

	
	
	@Parameter(description="The name/key under which the output image is stored. If this name equals the name of the input image, the input image is going to be overwritten.")
	public void setOutput(String output) {
		this.output = output;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		Serializable value = input.get(imageKey);
		if (value instanceof ImageRGB) {
			if(output.equals(imageKey)){	//are both names equal? then overwrite original
				Data result = process(input, (ImageRGB) value);
				return result;
			}
			else	//names are not equal --> create a new image so that original won't be overwritten
			{
				ImageRGB img = (ImageRGB) value;
				// create deep copy
				int width = img.width;
				int height = img.height;
				int[] array = new int[img.pixels.length];
				System.arraycopy(img.pixels, 0, array, 0, array.length);
				
				// do all further stuff with deep copy instead
				Data result = process(input, new ImageRGB(width, height, array));
				return result;
			}
			
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
