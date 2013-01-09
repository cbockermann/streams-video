/**
 * 
 */
package stream.image;

import stream.AbstractProcessor;
import stream.Data;

/**
 * @author chris
 * 
 */
public class ShowColor extends AbstractProcessor {

	String key = "color";
	ImageRGB img = new ImageRGB(100, 100);

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		Double r = (Double) input.get("frame:red:avg");
		Double g = (Double) input.get("frame:green:avg");
		Double b = (Double) input.get("frame:blue:avg");

		if (r == null || g == null || b == null) {
			input.put(key, img);
			return input;
		}

		int c = color(r.intValue(), g.intValue(), b.intValue());

		for (int i = 0; i < img.pixels.length; i++) {
			img.pixels[i] = c;
		}

		input.put(key, img);
		return input;
	}

	public int color(int r, int g, int b) {
		int c = (0xff << 24) | (r << 16) | (g << 8) | b;
		return c;
	}

	public int color(int a, int r, int g, int b) {
		int c = (a << 24) | (r << 16) | (g << 8) | b;
		return c;
	}
}
