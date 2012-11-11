package stream.image;

import stream.Data;

public class ChangesPixelCounter extends AbstractImageProcessor {

	@Override
	/**
	 * This operator expects img to be a DiffImage and counts the ratio of pixels, that have
	 * changed significantly. A significant changes is every change, where the value of the
	 * red + green + blue-channel has changes by at least 20.
	 */
	public Data process(Data item, ImageRGB img) {
		
		int counter = 0;
		
		for (int i = 0; i < img.getWidth(); i++) {
			for (int j = 0; j < img.getHeight(); j++) {
				
				int red = img.getRED(i, j);
				int green = img.getGREEN(i, j);
				int blue = img.getBLUE(i, j);
				
				if (red+green+blue > 20) { counter++; }

			}
		}
		
		item.put("frame:diff:changeratio", counter / (img.getHeight() * img.getWidth()));
		
		return item;
	}

}
