package stream.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * This processor takes all incoming images and stores them as .jpg-images 
 * in one folder on the disk.
 * @author Matthias
 *
 */
public class StoreImageToFile extends AbstractImageProcessor {
	
	String filenamekey = "frame:id";
	String folder = "C:/Users/Matthias/Documents/SchulteSVN/Diplomarbeit/data/kapseln/video/"; 
	
	/**
	 * Defines the attribute, that is taken as the filename. Normally this
	 * will be the id. Hence the default value is "frame:id".
	 * @param filenamekey
	 */
	public void setFilenamekey(String filenamekey) {
		this.filenamekey = filenamekey;
	} 
	
	/**
	 * Returns the attribut that is taken as the filename at the moment.
	 * @return
	 */
	public String getFilenamekey() {
		return filenamekey;
	}
	
	/**
	 * Sets the folder, the images are stored in.
	 * @param folder Path of a folder
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * Return the path of the folder the images as currently stored in.
	 * @return
	 */
	public String getFolder() {
		return folder;
	}

	@Override
	public Data process(Data item, ImageRGB img) {
		BufferedImage image = img.createImage();
		
		Long filename = (Long) item.get(filenamekey);
		
		String file = folder + filename +".jpg"; 
		
		File of = new File(file);
		try {
			ImageIO.write(image, "jpg", of);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return item;
	}

}
