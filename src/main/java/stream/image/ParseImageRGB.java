/**
 * 
 */
package stream.image;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;

/**
 * @author chris
 * 
 */
public class ParseImageRGB implements Processor {

	static Logger log = LoggerFactory.getLogger(ParseImageRGB.class);
	String key = "data";
	String image = "data";

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		Serializable value = input.get(key);
		if (value == null) {
			return input;
		}

		try {
			byte[] bytes = (byte[]) value;
			ImageRGB img = new ImageRGB(ImageIO.read(new ByteArrayInputStream(
					bytes)));
			input.put(image, img);
		} catch (Exception e) {
			log.error("Error processing image: {}", e.getMessage());
			if (log.isDebugEnabled())
				e.printStackTrace();
			return input;
		}

		return input;
	}
}
