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
public class NewsshowLabeler {

	static Logger log = LoggerFactory.getLogger(NewsshowLabeler.class);

	@Test
	public void test() throws Exception {
		URL url = NewsshowLabeler.class
				.getResource("/newsshowlabeler.xml");
		log.info("Starting MJpegImageStream-test from {}", url);
		stream.run.main(url);
	}

	public static void main(String[] args) throws Exception {
		new NewsshowLabeler().test();
	}
}
