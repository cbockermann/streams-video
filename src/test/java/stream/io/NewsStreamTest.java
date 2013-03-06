package stream.io;

/**
 * 
 */

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class NewsStreamTest {

	static Logger log = LoggerFactory.getLogger(NewsStreamTest.class);

	@Test
	public void test() throws Exception {

		log.warn("Test excluded, news-data only available on Matthias notebook.");

		// System.setProperty("stream.io.ImageStream.buffer", "8388608" );
		// URL url = NewsStreamTest.class
		// .getResource("/news-stream.xml");
		// log.info("Starting MJpegImageStream-test from {}", url);
		// stream.run.main(url);
	}

	public static void main(String[] args) throws Exception {
		new NewsStreamTest().test();
	}
}
