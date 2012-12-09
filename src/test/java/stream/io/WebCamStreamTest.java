package stream.io;

/**
 * 
 */

import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class WebCamStreamTest {

	static Logger log = LoggerFactory.getLogger(WebCamStreamTest.class);

	@Test
	public void test() throws Exception {
		URL url = WebCamStreamTest.class.getResource("/webcam-stream.xml");
		log.info("Starting MJpegImageStream-test from {}", url);
		stream.run.main(url);
	}

	public static void main(String[] args) throws Exception {
		new WebCamStreamTest().test();
	}
}
