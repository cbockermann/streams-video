/**
 * 
 */
package stream.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;

/**
 * @author chris
 * 
 */
public class CreateBufferedImage implements Processor {

	static Logger log = LoggerFactory.getLogger(CreateBufferedImage.class);
	String key = "image";
	String from = "data";

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		try {
			byte[] data = (byte[]) input.get("data");
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
			// input.put(key, img);
		} catch (Exception e) {
			log.error(e.getMessage());
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
		}
		return input;
	}
}
