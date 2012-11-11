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
public class MJpegImageStreamTest {

	static Logger log = LoggerFactory.getLogger(MJpegImageStreamTest.class);

	@Test
	public void test() throws Exception {
		URL url = MJpegImageStreamTest.class
				.getResource("/test-mjpeg-stream.xml");
		log.info("Starting MJpegImageStream-test from {}", url);
		stream.run.main(url);
	}

	public static void main(String[] args) throws Exception {
		new MJpegImageStreamTest().test();
	}
}
