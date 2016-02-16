package stream.io;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.annotations.Parameter;
import stream.expressions.ExpressionResolver;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * @author Jan Adam
 * 
 */
public class PNGWriter extends AbstractImageProcessor {

	static Logger log = LoggerFactory.getLogger(PNGWriter.class);
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
			
			ImageOutputStream  ios =  ImageIO.createImageOutputStream(file);
		    ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
		    ImageWriteParam iwp = writer.getDefaultWriteParam();
		    writer.setOutput(ios);
		    writer.write(null, new IIOImage(bi,null,null),iwp);
		    writer.dispose();
		    ios.close();
			
		} catch (Exception e) {
			log.error("Failed to write image to {}: {}", out, e.getMessage());
			if (log.isDebugEnabled())
				e.printStackTrace();
		}

		return item;
	}
}