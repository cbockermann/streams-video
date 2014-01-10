/**
 * 
 */
package stream.io;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class VideoDecoderTest {

	static Logger log = LoggerFactory.getLogger(VideoDecoderTest.class);

	@Test
	public void test() {
		try {
			URL url = VideoDecoderTest.class.getResource("/video-decoder.xml");
			stream.run.main(url);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		try {
			URL url = VideoDecoderTest.class.getResource("/video-decoder.xml");
			stream.run.main(url);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
		}
	}
}