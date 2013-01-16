package stream.news.learner.anchorshotdetection;

import stream.AbstractProcessor;
import stream.Data;

/**
 * This processor classifies a news video into anchorshots and news report shots, by applying a
 * decision tree to each example.
 * 
 * @author Matthias
 */
public class ApplyDecisionTreeModel extends AbstractProcessor {
	
	String predictionkey = "@prediction:anchorshot";
	
	/**
	 * Sets the key under which the classifier shall store the predicted label.
	 * @param predictionkey
	 */
	public void setPredictionkey(String predictionkey) {
		this.predictionkey = predictionkey;
	}
	
	/**
	 * Delivers the key under which the classifier currently stores the predicted label.
	 * @return
	 */
	public String getPredictionkey() {
		return predictionkey;
	}

	@Override
	public Data process(Data input) {
		
		Integer red_median = (Integer) input.get("frame:red:median");
		Double blue_sd = (Double) input.get("frame:blue:standardDeviation");
		Integer blue_median = (Integer) input.get("frame:blue:median");
		
		if ((red_median < 63) && (blue_sd > 38.823) && (blue_median > 102)) {
			input.put(predictionkey, true);
		} else {
			input.put(predictionkey, false);
		}
		
		return input;
	}

}
