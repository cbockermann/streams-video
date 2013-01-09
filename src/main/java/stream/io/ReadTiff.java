/**
 * 
 */
package stream.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class ReadTiff {

	static Logger log = LoggerFactory.getLogger(ReadTiff.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			FileInputStream in = new FileInputStream(new File(
					"/Volumes/RamDisk/test.tif"));
			BufferedImage image = ImageIO.read(in);
			log.info("Image: {}", image);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
