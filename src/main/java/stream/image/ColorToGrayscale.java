package stream.image;

import stream.Data;
import stream.annotations.Parameter;

public class ColorToGrayscale extends AbstractImageProcessor {

	String output = "data";
	
	@Parameter(description="The name/key under which the output image is stored. If this name equals the name of the input image, the input image is going to be overwritten.")
	public void setOutput(String output) {
		this.output = output;
	}
	
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
		
		item.put(output, img);
		return item;
	}

}