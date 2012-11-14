/**
 * 
 */
package stream.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.annotations.Parameter;
import stream.expressions.ExpressionResolver;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * @author chris
 * 
 */
public class JPEGWriter extends AbstractImageProcessor {

	static Logger log = LoggerFactory.getLogger(JPEGWriter.class);
	String output;

	/**
	 * @return the output
	 */
	public String getFile() {
		return output;
	}

	/**
	 * @param output
	 *            the output to set
	 */
	@Parameter(description = "This parameter allows for specifying the output format, allows to specify a runtime expression.", required = true)
	public void setFile(String output) {
		this.output = output;
	}

	/**
	 * @see stream.image.AbstractImageProcessor#process(stream.Data,
	 *      stream.image.ImageRGB)
	 */
	@Override
	public Data process(Data item, ImageRGB img) {

		if (output == null) {
			log.debug("No output property set, skipping item.");
			return item;
		}

		BufferedImage bi = img.createImage();
		String out = ExpressionResolver.expand(output, context, item);
		try {
			log.info("Writing JPG image to {}", out);
			File file = new File(out);
			if (file.getParentFile() != null
					&& !file.getParentFile().isDirectory()) {
				file.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(file);
			ImageIO.write(bi, "JPG", fos);
			fos.close();
		} catch (Exception e) {
			log.error("Failed to write image to {}: {}", out, e.getMessage());
			if (log.isDebugEnabled())
				e.printStackTrace();
		}

		return item;
	}
}