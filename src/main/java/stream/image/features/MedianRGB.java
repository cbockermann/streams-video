package stream.image.features;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * This processor computes the median color value for all three RGB channels
 * 
 * @author Matthias
 */
public class MedianRGB extends AbstractImageProcessor {

	@Override
	public Data process(Data item, ImageRGB img) {
		
		//int skipped = 0;
		
		int[] rvalues = new int[256];
		int[] gvalues = new int[256];
		int[] bvalues = new int[256];
		
		for (int i = 0; i < img.pixels.length; i++) {
			int argb = img.pixels[i];

/*			int alpha = (argb >> 24) & 0xff;
			if (alpha == 0) {
				skipped++;
				continue;
			}*/
			
			int red = (argb >> 16) & 0xFF;
			int green = (argb >> 8) & 0xFF;
			int blue = argb & 0xFF;
			
			rvalues[red]++;
			gvalues[green]++;
			bvalues[blue]++;
		}
		
		int median = img.getWidth()*img.getHeight() / 2;
		
		int r = 0;
		int g = 0;
		int b = 0;
		
		int i = 0;
		while (i < median) {
			i += rvalues[r];
			r++;
		}
		
		i = 0;
		while (i < median) {
			i += gvalues[g];
			g++;
		}
		
		i = 0;
		while (i < median) {
			i += bvalues[b];
			b++;
		}
		
		item.put("frame:red:median", r);
		item.put("frame:green:median", g);
		item.put("frame:blue:median", b);
		
		return item;
	}

}
