/**
 * 
 */
package stream.image;

import stream.Data;
import stream.Processor;

/**
 * @author chris
 * 
 */
public interface ImageProcessor extends Processor {

	public final static String DEFAULT_IMAGE_KEY = "frame:image";

	public Data process(Data item, ImageRGB image);
}
