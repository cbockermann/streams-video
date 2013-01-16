package stream.coffee.eventdetection;

import stream.AbstractProcessor;
import stream.Data;

public class EventDetectionEvaluation extends AbstractProcessor {
	
	Long lastrealevent = 0L;
	Long lastpredictedevent = 0L;
	
	Integer trueprediction = 0;
	Integer falseprediction = 0;
	Integer predictedEvents = 0;

	@Override
	public Data process(Data input) {
		
		String label = (String) input.get("@label:event");
		String prediction = (String) input.get("@prediction:event");
		Long frameid = (Long) input.get("frame:id");
		
		if (!label.equalsIgnoreCase("no_event")) {
			if (Math.abs(frameid-lastrealevent) > 6) {
				lastrealevent = frameid;
			}
		}
		
		if (prediction.equalsIgnoreCase("event")) {
			
			if (Math.abs(lastpredictedevent-frameid) > 6) {
				lastpredictedevent = frameid;
				predictedEvents++;
				
				if (Math.abs(lastrealevent-frameid) <6) {
					trueprediction++;
					System.out.println("Real event on frames " + lastrealevent + " - " + (lastrealevent+6) + ". Predicted correctly on frame " + frameid);
				} else {
					falseprediction++;
					System.out.println("Event predicted incorrectly on frame " + frameid);
				}
			}
			
		}
		
		return input;
	}
	
	@Override
	public void finish() throws Exception {
		System.out.println("Total amount of predicted events: " + predictedEvents);
		System.out.println("True Predictions: " + trueprediction + "; False Predictions: " + falseprediction);
		super.finish();
	}

}
