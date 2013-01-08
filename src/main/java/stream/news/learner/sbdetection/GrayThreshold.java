package stream.news.learner.sbdetection;

import stream.AbstractProcessor;
import stream.Data;

/**
 * This processor predicts shot boundaries based on the DiffImages of two successive frames. It can only
 * be applied, after the DiffImage has already been calculated.
 * If the average gray value of the DiffImages exceeds a given threshold t, the frame is labeled as a shot boundary. 
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
	 * Sets the threshold t to a new value.
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
	 * Tells the GrayThreshold processor, where to find the gray value of the pixels.
	 * @param graykey
	 */
	public void setGraykey(String graykey) {
		this.graykey = graykey;
	}
	
	/**
	 * Delivers the string, under which the processor expects the gray value of the input image to be found.
	 * @return the current key, the processor expects the gray value to be stored under.
	 */
	public String getGraykey() {
		return graykey;
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
