/**
 * 
 */
package stream.image;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.data.DataFactory;
import stream.image.features.AverageRGB;
import stream.runtime.ProcessContextImpl;

/**
 * @author chris
 * 
 */
public class AverageRGBTest {

	static Logger log = LoggerFactory.getLogger(AverageRGBTest.class);

	@Test
	public void test() {

		try {

			AverageRGB argb = new AverageRGB();
			argb.init(new ProcessContextImpl());

			Data item = DataFactory.create();
			ImageRGB image = new ImageRGB(300, 300);
			// double size = 300 * 300;
			int transparent = 0;

			for (int i = 0; i < image.pixels.length; i++) {

				if (i % 3 == 0)
					image.pixels[i] = 0xffff0000;

				if (i % 3 == 1)
					image.pixels[i] = 0xff00ff00;

				if (i % 3 == 2)
					image.pixels[i] = 0xff0000ff;

				if (i % 2 == 0 || i % 3 == 0) { // make every 2nd (red) pixel
												// transparent
					image.pixels[i] = 0x00ffffff & image.pixels[i];
					transparent++;
				}
			}
			log.info("marked {} of {} pixels as transparent.", transparent,
					image.width * image.height);

			item.put("frame:data", image);
			// DisplayImage si = new DisplayImage();
			// si.init(new ProcessContextImpl());
			// si.process(item);

			argb.setImage("frame:data");
			argb.process(item);

			// all red pixels should be transparent and thus should be discarded
			// for
			// computing the average red value
			//
			Double avgRed = (Double) item.get("frame:red:average");
			Assert.assertEquals(85.0, avgRed);

			Double avgGreen = (Double) item.get("frame:green:average");
			Assert.assertEquals(85.0, avgGreen);

			log.info("item: {}", item);

			// Thread.sleep(10000000);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error: " + e.getMessage());
		}
	}
}
