package stream.image.filters;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * The Smooting Operator is a filter to reduce the noise in an input image.
 * Each pixel gets replaced by the average of pixels in a square window surrounding this pixel.
 * @author Matthias
 *
 */
public class Smoothing extends AbstractImageProcessor {

	String output = "smooth";
	
	Integer windowSize = 3;
	
	Boolean weighted = false;
	
	int[][] weightingMatrix = null;
	int weightsum = 0;
	
	/**
	 * @return name The name/key under which the smoothed image is stored.
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * Setter for the Parameter Output
	 * @param output The name/key under which the smoothed Image is stored. 
	 * 					If this name equals the name of the input image, the input image
	 * 					is going to be overwritten.
	 */
	@Parameter(description ="The name/key of the output image is stored. If this name equals the name of the input image, the input image is going to be overwritten.")
	public void setOutput(String output) {
		this.output = output;
	}
	
	
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
		
		weightingMatrix = new int[windowSize][windowSize];
		weightsum = 0;
		
		if (!weighted) {
			for (int i = 0; i < weightingMatrix.length; i++) {
				for (int j = 0; j < weightingMatrix[i].length; j++) {
					weightingMatrix[i][j] = 1;
					weightsum++;
				}
			}
		} else {
			//TODO: Not yet implemented.
		}
	}
	
	@Override
	public Data process(Data item, ImageRGB img) {
		
		ImageRGB smoothedImage = new ImageRGB(img.getWidth(), img.getHeight());
		
		//TODO: Noch sehr unschön durch das von 1/1 bis Width-1/Height-1
		for (int x=1; x < smoothedImage.getWidth()-1; x++) {
			for (int y=1; y < smoothedImage.getHeight()-1; y++) {
				
				int red = 0;
				for (int i=0; i<windowSize; i++) {
					for (int j=0; j<windowSize; j++) {
						red = red + weightingMatrix[x-1+i][y-1+i]*img.getRED(x-1+i, y-1+i);
					}
				}
				red = red / weightsum;
				
				int green = 0;
				for (int i=0; i<windowSize; i++) {
					for (int j=0; j<windowSize; j++) {
						green = green + weightingMatrix[x-1+i][y-1+i]*img.getGREEN(x-1+i, y-1+i);
					}
				}
				green = green / weightsum;
				
				int blue = 0;
				for (int i=0; i<windowSize; i++) {
					for (int j=0; j<windowSize; j++) {
						blue = blue + weightingMatrix[x-1+i][y-1+i]*img.getBLUE(x-1+i, y-1+i);
					}
				}
				blue = blue / weightsum;
				
				smoothedImage.setRGB(x, y, red, green, blue);
			}
		}
		
		item.put(output, smoothedImage);
		
		return item;
	}

}
