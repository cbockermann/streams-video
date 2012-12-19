package stream.news.learner.sbdetection;

import stream.AbstractProcessor;
import stream.Data;

/**
 * This operator predicts shot boundaries based on the DiffImages of two successive frames.
 * If the average gray value of the images exceeds a given threshold t, the frame is labeled as a shot boundary. 
 * 
 * @author Matthias
 *
 */
public class GrayThreshold extends AbstractProcessor {

	/*
	 * The threshold. If t is exceeded, a shot boundary in predicted.
	 */
	Integer t = 50;
	String graykey = "frame:red:avg";
	
	/**
	 * Sets the threshold t
	 * @param t Threshold
	 */
	public void setT(Integer t) {
		this.t = t;
	}
	
	/**
	 * 
	 * @return Threshold
	 */
	public Integer getT() {
		return t;
	}
	
	
	@Override
	public Data process(Data input) {
		
		//System.out.println("Learner received item");
		
		Double gray = (Double) input.get(graykey);
		if (gray == null) return input; 
		
		Boolean prediction = false;
		if (gray >= t) {
			prediction = true;
		}
		
		input.put("@prediction:shotboundary", prediction);
		
		//For debugging only
		//Boolean sb = (Boolean) input.get("ShotBoundary");
		//Integer frame = (Integer) input.get("frame:id");
		//if (prediction != sb) {
		//	System.out.println("Prediction error at frame: " + frame + " Prediction: " + prediction + ". Real label: " + sb +".");
		//}
		
		return input;
	}

}
