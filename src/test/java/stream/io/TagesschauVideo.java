/**
 * 
 */
package stream.io;

import java.net.URL;

import org.junit.Test;

/**
 * @author chris
 * 
 */
public class TagesschauVideo {

	@Test
	public void test() {
		try {
			URL url = TagesschauVideo.class.getResource("/tagesschau.xml");
			stream.run.main(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
