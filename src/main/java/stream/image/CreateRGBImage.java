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
import stream.annotations.Parameter;

/**
 * <p>
 * This processor tries to decode a chunk of bytes from a byte array attribute
 * into an RGB image object.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 * 
 */
public class CreateRGBImage implements Processor {

	/* A global logger for this class */
	static Logger log = LoggerFactory.getLogger(CreateRGBImage.class);

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
			ImageRGB image = new ImageRGB(img);
			input.put(key, image);
		} catch (Exception e) {
			log.error(e.getMessage());
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
		}
		return input;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	@Parameter(description = "The name/key of the attribute to which the RGBImage object is being output, defaults to `image`.")
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from
	 *            the from to set
	 */
	@Parameter(description = "The name/key of the attribute which holds a byte array of the image data, defaults to `data`.")
	public void setFrom(String from) {
		this.from = from;
	}
}
