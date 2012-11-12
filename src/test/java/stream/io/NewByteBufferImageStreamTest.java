/**
 * 
 */
package stream.io;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

/**
 * @author chris
 * 
 */
public class NewByteBufferImageStreamTest {

	@Test
	public void test() {
		try {

			URL url = NewByteBufferImageStreamTest.class
					.getResource("/test-new-bytebuffer.xml");
			stream.run.main(url);

		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
		}
	}

}
