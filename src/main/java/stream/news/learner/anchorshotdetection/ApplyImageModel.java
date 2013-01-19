package stream.news.learner.anchorshotdetection;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * This processor classifies a news video into anchorshots and news report shots, by matching each shot against
 * an image, representing an anchorshot model. Pixels, that are black in the model, get ignored, all other pixels
 * get compared pixelwise. If the difference between the current image and the model does not exceed a given threshold
 * t, the shot is predicted to be an anchorshot.
 * 
 * @author Matthias
 *
 */
public class ApplyImageModel extends AbstractImageProcessor {

	/*
	 * The threshold. If t is exceeded, a shot boundary in predicted.
	 */
	Integer t = 50;
	ImageRGB modelImage = new ImageRGB(0, 0);
	String model = "C:/Users/Matthias/Documents/SchulteSVN/Diplomarbeit/data/anchorshots/2-scaled.jpg";
	String predictionkey = "@prediction:anchorshot";
	
	/**
	 * Tells the processor, where to find the model to be matched.
	 * @param model String holding the location of the model image
	 */
	@Parameter(description="Tells the processor, where to find the model to be matched.")
	public void setModel(String model) {
		this.model = model;
	}
	
	/**
	 * Delivers the location, where the processor currents looks for the model image.
	 * @return String holding the location of the model image
	 */
	public String getModel() {
		return model;
	}
	
	/**
	 * Sets the threshold t to a new value.
	 * @param t Threshold
	 */
	@Parameter(description="Sets the threshold t to a new value.")
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
	 * Sets the key under which the classifier shall store the predicted label.
	 * @param predictionkey
	 */
	@Parameter(description="Sets the key under which the classifier shall store the predicted label.")
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
	public void init(ProcessContext ctx) throws Exception {
		
		InputStream is = new BufferedInputStream( new FileInputStream(model) );
		BufferedImage temp = ImageIO.read(is);
		modelImage = new ImageRGB(temp);
		
		super.init(ctx);
	}
	
	@Override
	public Data process(Data item, ImageRGB img) {
		
		int pixels = 0;
		
		//int normalpixel = 0;
		//int blackpixel = 0;
		
		
		double similar = 0.0;

		for (int i = 0; i < modelImage.getWidth(); i++) {
			for (int j = 0; j < modelImage.getHeight(); j++) {

				int rgbmodel = modelImage.getRGB(i, j);
				
				int rold = (rgbmodel >> 16) & 0xFF;
				int gold = (rgbmodel >> 8) & 0xFF;
				int bold = rgbmodel & 0xFF;
				
				if (rold < 250 && gold <250 && bold < 250) {
				
					//normalpixel++;
					
					int rgboriginal = img.getRGB(i, j);

					int rnew = (rgboriginal >> 16) & 0xFF;
					int gnew = (rgboriginal >> 8) & 0xFF;
					int bnew = rgboriginal & 0xFF;
				
					int rdiff = Math.abs(rold - rnew);
					int gdiff = Math.abs(gold - gnew);
					int bdiff = Math.abs(bold - bnew);
				
					pixels++;
					if ((rdiff < 55) && (gdiff < 55) && (bdiff < 55)) {
						similar++;
					}
				} else {
					//blackpixel++;
				}
			}
		}

		//System.out.println("Blackpixels " + blackpixel + " ,others: " + normalpixel);
		
		double similarityratio = (similar/pixels);
		item.put("frame:similaritytoanchorshotmodel", similarityratio);
		
		if (similarityratio > 0.5) {
			item.put(predictionkey, true);
		} else {
			item.put(predictionkey, false);
		}

		return item;
	}

}
