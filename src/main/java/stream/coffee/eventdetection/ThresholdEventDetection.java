package stream.coffee.eventdetection;

import stream.AbstractProcessor;
import stream.Data;

public class ThresholdEventDetection extends AbstractProcessor {
	
	String attribute = "frame:red:average";
	
	Integer standardvalue = 120;
	Integer t = 50;
	
	String predictionkey = "@prediction:shotboundary";
	
	Long lastpredictedevent = 0L;
	
	/**
	 * Sets the threshold t to a new value. If the value of the attribute "attribute"
	 * differs more than t from the expected value "standardvalue", the frame is classified
	 * as an event.
	 * @param t Threshold
	 */
	public void setT(Integer t) {
		this.t = t;
	}
	
	/**
	 * Returns the current threshold the processor is working with.
	 * @return Threshold
	 */
	public Integer getT() {
		return t;
	}
	
	/**
	 * Tells the processor, on which attribute to base the event detection on.
	 * @param graykey
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	/**
	 * Delivers the name of the attribute, the processor actually bases the event detection on.
	 * @return
	 */
	public String getAttribute() {
		return attribute;
	}
	
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
	
	/**
	 * Sets the value, attribute "attribute" has in random frames
	 * @param standardvalue
	 */
	public void setStandardvalue(Integer standardvalue) {
		this.standardvalue = standardvalue;
	}
	
	/**
	 * Delivers the value, the attribute "attribute" has in random frames. 
	 * @return
	 */
	public Integer getStandardvalue() {
		return standardvalue;
	}

	@Override
	public Data process(Data input) {
		
		Double realvalue = (Double) input.get(attribute);
		
		if ((realvalue < standardvalue) && (Math.abs(realvalue-standardvalue) > t)) {
			input.put(predictionkey, "event");
		} else {
			input.put(predictionkey, "no_event");
		}
		
		return input;
	}

}
