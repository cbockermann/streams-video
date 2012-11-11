package stream.image;

import java.awt.Color;

import stream.Data;

public class DiffImage extends AbstractImageProcessor {
	
	ImageRGB lastImage = new ImageRGB(0, 0);
	
	@Override
	public Data process(Data item, ImageRGB img) {
		
		ImageRGB diffImage = new ImageRGB(img.getHeight(), img.getWidth());
		for (int i=0; i<diffImage.getWidth(); i++) {
			for (int j=0; j<diffImage.getHeight(); j++) {
				diffImage.setRGB(i, j, img.getRGB(i, j));
			}
		}
		
		if (diffImage.getHeight() == lastImage.getHeight() && diffImage.getWidth() == lastImage.getWidth()) {
			for (int i=0; i<diffImage.getWidth(); i++) {
				for (int j=0; j<diffImage.getHeight(); j++) {
					
					int rgbold = lastImage.getRGB(i, j);
					int rgbnew = diffImage.getRGB(i, j);
					
					int rold = (rgbold >> 16) & 0xFF;
					int gold = (rgbold >> 8) & 0xFF;
					int bold = rgbold & 0xFF;
					
					int rnew = (rgbnew >> 16) & 0xFF;
					int gnew = (rgbnew >> 8) & 0xFF;
					int bnew = rgbnew & 0xFF; 
					
					int rdiff = Math.abs(rold - rnew);
					int gdiff = Math.abs(gold - gnew);
					int bdiff = Math.abs(bold - bnew);
					
					rdiff = 255 - rdiff;
					gdiff = 255 - gdiff;
					bdiff = 255 - bdiff;
					
					if (rdiff < 20) { rdiff = 0; }
					if (gdiff < 20) { gdiff = 0; }
					if (bdiff < 20) { bdiff = 0; }
					
					//int rgbdiff = rdiff * 65536 + gdiff * 265 + bdiff;
					int rgbdiff = rdiff;
					rgbdiff = (rgbdiff << 8) + gdiff;
					rgbdiff = (rgbdiff << 8) + bdiff; 
					
					diffImage.setRGB(i, j, rgbdiff);
				}
			}
		}
		
		lastImage = img;
		
		item.remove("data");
		item.put("data", diffImage);
		
		return item;
	}

}