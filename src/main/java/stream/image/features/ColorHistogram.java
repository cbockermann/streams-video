package stream.image.features;

import stream.Data;
import stream.annotations.Parameter;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

public class ColorHistogram extends AbstractImageProcessor {
	
	Integer bins = 9;
	String colorchannel = "red";
	
	@Parameter(description="Sets the number of bins the color channel is discretized into.")
	public void setBins(Integer bins) {
		this.bins = bins;
	}
	
	@Parameter(description="Sets the color channel the histogram is computed for.")
	public void setColorchannel(String colorchannel) {
		this.colorchannel = colorchannel;
	}

	@Override
	public Data process(Data item, ImageRGB img) {
		// TODO Auto-generated method stub
		return item;
	}

}
