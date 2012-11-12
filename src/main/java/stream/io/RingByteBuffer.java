/**
 * 
 */
package stream.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class RingByteBuffer {

	static Logger log = LoggerFactory.getLogger(RingByteBuffer.class);
	final byte[] buffer;
	final int bufferLength;
	int pos = 0;
	int limit = 0;
	long bytesRead = 0L;

	public RingByteBuffer(int size) {
		buffer = new byte[size];
		bufferLength = buffer.length;
	}

	public int getRemaining() {
		return bufferLength - (limit - pos);
	}

	public long getBytesRead() {
		return bytesRead;
	}

	public byte getByte(int i) {
		return buffer[(pos + i) % bufferLength];
	}

	public synchronized long read(InputStream in) throws Exception {
		log.info(
				"Reading into buffer, current position: {}, current limit: {}",
				pos, limit);

		bytesRead = 0;
		int data = -1;
		do {
			data = in.read();

			if (data >= 0) {
				buffer[limit % bufferLength] = (byte) data;
				log.debug("next read returned {}", buffer[limit % bufferLength]);
				bytesRead++;
				limit++;
				if (limit % buffer.length == pos % buffer.length) {
					pos++;
					log.debug("Advancing 'pos' to {}", pos);
				}
				log.debug("pos: {}, limit: {}", pos, limit);
			}
		} while (data >= 0);

		return bytesRead;
	}

	public synchronized int append(byte[] data) throws IOException {
		return -1;
	}

	public synchronized int find(final byte[] signature, final int offset) {

		for (int i = offset; i < limit - signature.length; i++) {
			int idx = containsAt(buffer, i, signature);
			if (idx >= 0) {
				// log.info("Found signature at {}!", idx);
				return idx;
			} else {
				// log.info("signature {} not found at start of {}",
				// getHex(signature, signature.length),
				// getHex(buffer, i, 32));
			}
		}

		return -1;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	protected int checkBytes(final int pos, final byte[] sig) {

		// log.info("Starting check at buffer[{}:..] = {}", pos,
		// getHex(buffer, pos, 32));

		for (int s = pos; s < limit; s++) {
			int correct = 0;
			for (int i = 0; i < sig.length && s + i < limit; i++) {
				if (buffer[i % bufferLength] == sig[i])
					correct++;
				else
					return -1;
			}

			if (correct == sig.length)
				return s;
		}

		int checked = 0;
		for (int s = pos; s < limit; s++) {
			checked = 0;
			for (int p = 0; p < sig.length; p++) {
				if (buffer[(s + p) % buffer.length] != sig[p]) {
					return -1;
				} else
					checked++;
			}
		}

		if (checked == 0) {
			log.info("signature not found.");
			return -1;
		}

		log.info("Found signature {} at position {}", new String(sig), pos);
		return pos;
	}

	public static int containsAt(byte[] buffer, int off, byte[] sig) {

		for (int i = 0; i < sig.length; i++) {
			if (buffer[(off + i) % buffer.length] != sig[i]) {
				return -1;
			}
		}

		return off;
	}

	public static void main(String[] args) throws Exception {

		try {
			byte[] sig = "ab".getBytes();

			RingByteBuffer buf = new RingByteBuffer(1024);
			log.info("Buffer position is {}, limit is {}", buf.pos, buf.limit);
			int start = 0;
			int idx = buf.find(sig, start);

			int cnt = 0;

			while (idx < 0 && cnt < 5) {
				long read = buf.read(createStream("bcbbacbcccadba"));
				if (read < 0) {
					log.error("end of stream!");
					break;
				} else {
					idx = buf.find(sig, start);
				}
				cnt++;
			}
			if (idx >= 0) {
				log.info("Found signature at {}", idx);
			} else {
				log.info("Signature not found!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getHex(byte[] bytes, int len) {
		return getHex(bytes, 0, len);
	}

	public static String getHex(byte[] bytes, int off, int len) {
		StringBuffer s = new StringBuffer("[");
		for (int i = off; i < bytes.length && i < len; i++) {
			s.append("0x" + Integer.toHexString((int) bytes[i]));
			if (i + 1 < bytes.length)
				s.append(", ");
		}
		s.append("]");
		return s.toString();
	}

	public static InputStream createStream(String str) {
		byte[] data = str.getBytes();
		log.info("Reading data '{}'", getHex(data, data.length));
		return new ByteArrayInputStream(data);
	}
}
