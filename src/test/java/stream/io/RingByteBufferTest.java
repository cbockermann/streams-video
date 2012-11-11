/**
 * 
 */
package stream.io;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class RingByteBufferTest {

	static Logger log = LoggerFactory.getLogger(RingByteBufferTest.class);

	@Test
	public void testContainsAt() {

		byte[] buf = "abcdef".getBytes();
		byte[] sig = "fa".getBytes();

		Assert.assertTrue(RingByteBuffer.containsAt(buf, 4, sig) < 0);

		Assert.assertTrue(RingByteBuffer.containsAt(buf, 5, sig) == 5);

	}

	/**
	 * Test method for
	 * {@link stream.io.RingByteBuffer#read(java.io.InputStream)}.
	 */
	@Test
	public void testRead() {

		try {
			String test = "abcdefghijklmnopqrstuvwxyz";
			int size = 1024;

			RingByteBuffer ring = new RingByteBuffer(size);
			ring.read(RingByteBuffer.createStream(test));
			Assert.assertEquals(test.length(), ring.getBytesRead());
			Assert.assertEquals(size - test.length(), ring.getRemaining());

		} catch (Exception e) {
			log.error("Test failed: {}", e.getMessage());
			fail("Test failed!");
		}
	}

	@Test
	public void testReadTwice() {
		log.info("# testReadTwice() ");
		try {

			String test = "ABCD";
			RingByteBuffer ring = new RingByteBuffer(6);

			ring.read(stream(test));
			log.info("Buffer after first read: {}",
					RingByteBuffer.getHex(ring.getBuffer(), 6));

			log.info("Expecting 'A' ({}) at position 0",
					"0x" + Integer.toHexString((byte) 'A'));
			Assert.assertEquals((byte) 'A', ring.getByte(0));

			log.info("Reading new block...");
			ring.read(stream(test));
			log.info("Buffer after second read: {}",
					RingByteBuffer.getHex(ring.getBuffer(), 6));

			log.info("Expecting 'A' (0x{}) at position 4",
					Integer.toHexString((byte) 'A'));
			log.info("byte at '4': 0x{}", Integer.toHexString(ring.getByte(4)));
			Assert.assertEquals((byte) 'A', ring.getByte(4));

			log.info("Expecting 'D' (0x{}) at position 1",
					Integer.toHexString((byte) 'D'));
			log.info("byte at '1': 0x{}", Integer.toHexString(ring.getByte(1)));
			Assert.assertEquals((byte) 'D', ring.getByte(1));

		} catch (Exception e) {
			log.error("Test failed: {}", e.getMessage());
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
		}
	}

	/**
	 * Test method for {@link stream.io.RingByteBuffer#find(byte[], int)}.
	 */
	@Test
	public void testFind() {
		log.info("# testFind()");
		try {
			String test = "abcdefghijklmnopqrstuvwxyz";
			int size = 1024;

			RingByteBuffer ring = new RingByteBuffer(size);
			ring.read(RingByteBuffer.createStream(test));

			int idx = ring.find("op".getBytes(), 0);
			Assert.assertEquals(14, idx);

		} catch (Exception e) {
			log.error("Test failed: {}", e.getMessage());
			fail("Test failed!");
		}
	}

	/**
	 * Test method for {@link stream.io.RingByteBuffer#find(byte[], int)}.
	 */
	@Test
	public void testFind2() {

		try {
			String test = "abcdefghijklmn";
			int size = test.length();

			RingByteBuffer ring = new RingByteBuffer(size);
			ring.read(stream(test));

			int idx = ring.find("za".getBytes(), 0);
			log.info("index: {}", idx);

			long read = ring.read(stream("dfgasdfz"));
			log.info("Reading {} new bytes", read);
			idx = ring.find("za".getBytes(), 0);
			log.info("idx: {}", idx);

		} catch (Exception e) {
			log.error("Test failed: {}", e.getMessage());
			fail("Test failed!");
		}
	}

	public static InputStream stream(String str) {
		ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
		return in;
	}
}
