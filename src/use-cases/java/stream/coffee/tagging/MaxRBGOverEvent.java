package stream.coffee.tagging;

import stream.AbstractProcessor;
import stream.Data;

public class MaxRBGOverEvent extends AbstractProcessor {
	
	Double minRed;
	Double minGreen;
	Double minBlue;

	@Override
	public Data process(Data input) {
		
		String prediction = (String) input.get("@prediction:event");
		if (prediction.equalsIgnoreCase("event")) {
			
			Double red = (Double) input.get("frame:red:average");
			Double green = (Double) input.get("frame:green:average");
			Double blue = (Double) input.get("frame:blue:average");
		
			if (red < minRed) {
				minRed = red;
			}
			if (green < minGreen) {
				minGreen = green;
			}
			if (blue < minBlue) {
				minBlue = blue;
			}
			
			input.put("event:red:min", minRed);
			input.put("event:green:min", minGreen);
			input.put("event:blue:min", minBlue);
			
		} else {
			minRed = 255.0;
			minGreen = 255.0;
			minBlue = 255.0;
		}
		
		return input;
	}

}
