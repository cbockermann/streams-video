package stream.image.features;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * This processor computes the standard deviation for all three RGB channels.
 * Requires the Average (=Mean) value for all RGB channels to be included already. This can for example be done
 * by using the {@link AverageRGB} processor.
 * 
 * @author Matthias
 *
 */
public class StandardDeviationRGB extends AbstractImageProcessor {

	String averageRedKey = "frame:red:average";
	String averageGreenKey = "frame:green:average";
	String averageBlueKey = "frame:blue:average";
	
	@Override
	public Data process(Data item, ImageRGB img) {
		
		Double averageRed = (Double) item.get(averageRedKey);
		Double averageGreen = (Double) item.get(averageGreenKey);
		Double averageBlue = (Double) item.get(averageBlueKey);
		
		double r = 0.0;
		double g = 0.0;
		double b = 0.0;
		double px = img.pixels.length;
		
		for (int i = 0; i < img.pixels.length; i++) {
			int argb = img.pixels[i];

			int red = (argb >> 16) & 0xFF;
			int green = (argb >> 8) & 0xFF;
			int blue = argb & 0xFF;

			r += Math.abs(red - averageRed);
			g += Math.abs(green - averageGreen);
			b += Math.abs(blue - averageBlue);
		}
		
		item.put("frame:red:standardDeviation", (double) r / px);
		item.put("frame:blue:standardDeviation", (double) b / px);
		item.put("frame:green:standardDeviation", (double) g / px);
		
		return item;
	}

}
