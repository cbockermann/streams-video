package stream.news.helper;

import stream.AbstractProcessor;
import stream.Data;
import stream.annotations.Parameter;

/**
 * Compares a predicted label with the true label. If both labels match, no action is performed.
 * If the prediction deviates from the true label, the number of the frame gets stored. This processor
 * hence helps to get an overview of the data, that was not labeled correctly. This is helpful to improve
 * the classifiers.
 * 
 * @author Matthias
 *
 */
public class ErrorOutput extends AbstractProcessor {

	String label = "@label:shotboundary";
	String prediction = "@prediction:shotboundary";
	
	/**
	 * Sets the key under which the true label is stored.
	 * @param label String
	 */
	@Parameter(description="Sets the key under which the true label is stored.")
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Delivers the key under which the processor currently expects the true label to be stored.
	 * @return
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Sets the key under which the predicted label was stored by the classifier.
	 * @param label String
	 */
	@Parameter(description="Sets the key under which the predicted label was stored by the classifier.")
	public void setPrediction(String prediction) {
		this.prediction = prediction;
	}
	
	/**
	 * Delivers the key under which the processor currently expects the true label to be stored by the classifier.
	 * @return
	 */
	public String getPrediction() {
		return prediction;
	}
	
	@Override
	public Data process(Data input) {
		
		Boolean blabel = (Boolean) input.get(label);
		Boolean bprediction = (Boolean) input.get(prediction);
		Long frame = (Long) input.get("frame:id");
		
		if (blabel == false && bprediction == true) {
			//System.out.println("Prediction Error on frame " + frame + ". True label: " + sb + ", Prediction: " + prediction);
			System.out.print(frame +", ");
		}
		
		return input;
	}

}
