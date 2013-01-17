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
public class CoffeeTest {

	static Logger log = LoggerFactory.getLogger(CoffeeTest.class);

	@Test
	public void testDummy() {

	}

	public void test() throws Exception {
		URL url = CoffeeTest.class.getResource("/border-detection-stream.xml");
		log.info("Starting MJpegImageStream-test from {}", url);
		stream.run.main(url);
	}

	public static void main(String[] args) throws Exception {
		new CoffeeTest().test();
	}
}
