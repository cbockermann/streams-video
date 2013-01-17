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
public class LaserPointerTest {

	static Logger log = LoggerFactory.getLogger(LaserPointerTest.class);

	@Test
	public void testDummy() {
	}

	public void test() throws Exception {
		URL url = LaserPointerTest.class.getResource("/laser-pointer.xml");
		log.info("Starting LaserPointerTest from {}", url);
		stream.run.main(url);
	}

	public static void main(String[] args) throws Exception {
		new LaserPointerTest().test();
	}
}
