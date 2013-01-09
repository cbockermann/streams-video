package stream.news.learner.anchorshotdetection;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

public class AnchorshotModelCreator extends AbstractImageProcessor {
	
	String model = "C:/Users/Matthias/Documents/SchulteSVN/Diplomarbeit/data/anchorshots/model.jpg";

	@Override
	public Data process(Data item, ImageRGB img) {
		
		Boolean isAnchorshot = (Boolean) item.get("@label:anchorshot");
		
		if (isAnchorshot) {
			
			ImageRGB modelImage = new ImageRGB(640, 360);
			BufferedImage newModel = new BufferedImage(modelImage.getWidth(), modelImage.getHeight(), BufferedImage.TYPE_INT_BGR);
			
			try {
			
			InputStream is = new BufferedInputStream( new FileInputStream(model) );
			BufferedImage temp = ImageIO.read(is);
			modelImage = new ImageRGB(temp);
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			for (int i = 0; i < modelImage.getWidth(); i++) {
				for (int j = 0; j < modelImage.getHeight(); j++) {

					int rgbmodel = modelImage.getRGB(i, j);
					int rgboriginal = img.getRGB(i, j);

					int rold = (rgbmodel >> 16) & 0xFF;
					int gold = (rgbmodel >> 8) & 0xFF;
					int bold = rgbmodel & 0xFF;

					int rnew = (rgboriginal >> 16) & 0xFF;
					int gnew = (rgboriginal >> 8) & 0xFF;
					int bnew = rgboriginal & 0xFF;
					
					int rdiff = Math.abs(rold - rnew);
					int gdiff = Math.abs(gold - gnew);
					int bdiff = Math.abs(bold - bnew);
					
					if ((rdiff < 65) && (gdiff < 65) && (bdiff < 65)) {
						newModel.setRGB(i, j, rgbmodel);
					} else {
						newModel.setRGB(i, j, 0);
					}
					
				}
			}
			
			File of = new File(model);
			try {
				ImageIO.write(newModel, "jpg", of);
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}
		
		
		return null;
	}

}
