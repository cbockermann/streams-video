package stream.image;

import stream.Data;

public class ColorToGrayscale extends AbstractImageProcessor {

	
	
	@Override
	public Data process(Data item, ImageRGB img) {
		
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				
				int red = img.getRED(x, y);
				int green = img.getGREEN(x, y);
				int blue = img.getBLUE(x, y);
				
				
				int gray = Math.round((1/3)*red+(1/3)*green+(1/3)+blue);
				
				img.setRGB(x, y, gray, gray, gray);
				
			}
		}
		
		item.put("data", img);
		return item;
	}

}
