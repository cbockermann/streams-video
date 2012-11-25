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
public class FileStreamTest {

	static Logger log = LoggerFactory.getLogger(FileStreamTest.class);

	@Test
	public void test() throws Exception {
		URL url = FileStreamTest.class.getResource("/file-stream.xml");
		log.info("Starting MJpegImageStream-test from {}", url);
		stream.run.main(url);
	}

	public static void main(String[] args) throws Exception {
		new FileStreamTest().test();
	}
}
