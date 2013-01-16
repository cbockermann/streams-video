package stream.coffee.helper;

import stream.AbstractProcessor;
import stream.Data;

public class DatasetGenerator extends AbstractProcessor {

	Data lastData = null;
	
	@Override
	public Data process(Data input) {
		
		if (lastData != null){
			
			String lastDataPrediction = (String) lastData.get("@prediction:event");
			String inputDataPrediction = (String) input.get("@prediction:event");
			
			if (lastDataPrediction.equalsIgnoreCase("event") && inputDataPrediction.equalsIgnoreCase("no_event")) {
				Data temp = lastData.createCopy();
				lastData = input;
				return temp;
			} else {
				lastData = input;
			}
		} else {
			lastData = input;
		}
		
		return null;
	}

}
