package stream.image;

import stream.Data;

public class DiffImage extends AbstractImageProcessor {
	
	ImageRGB lastImage = new ImageRGB(0, 0);

	public DiffImage() {
	// TODO Auto-generated constructor stub
	}
	
	@Override
	public Data process(Data item, ImageRGB img) {
		
		ImageRGB diffImage = img;
		
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
					
					int rgbdiff = Math.abs(rold - rnew) <<16 + Math.abs(gold - gnew) << 8 + Math.abs(bold - bnew);
					
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
