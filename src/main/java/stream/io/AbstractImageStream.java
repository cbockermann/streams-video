/**
 * 
 */
package stream.io;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;

/**
 * @author chris
 * 
 */
public abstract class AbstractImageStream extends AbstractDataStream {

	static Logger log = LoggerFactory.getLogger(AbstractImageStream.class);
	public final static byte[] GIF_SIGNATURE = new byte[] { 0x47, 0x49, 0x46,
			0x38 };
	public final static byte[] JPG_SIGNATURE = new byte[] { (byte) 0xff,
			(byte) 0xd8 }; // , (byte) 0xfe, 0x00, 0x0e, 0x4c, 0x61
							// };

	int chunkSize = 1;
	URL url;
	BufferedInputStream input;
	byte[] buffer = new byte[2048 * 1024 * 16];
	int start = 0;
	long frameId;
	int limit = 0;
	long offset = 0L;
	final byte[] signature;
	String key = "data";

	public AbstractImageStream(URL url, byte[] signature) throws Exception {
		this(url.openStream(), signature);
		this.url = url;
	}

	public AbstractImageStream(InputStream in, byte[] signature)
			throws Exception {
		this.input = new BufferedInputStream(in);
		this.signature = signature;
	}

	/**
	 * @see stream.io.AbstractDataStream#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();
		int bufSize = 2048 * 1024 * 16;
		try {
			bufSize = new Integer(System.getProperty(
					"stream.io.ImageStream.buffer", "" + (2048 * 1024 * 16)));

		} catch (Exception e) {
			e.printStackTrace();
			bufSize = 2048 * 1024 * 16;
		}
		log.info("Using buffer size of {}k", bufSize / 1024);
		buffer = new byte[bufSize];
	}

	/**
	 * @see stream.io.DataStream#close()
	 */
	@Override
	public void close() throws Exception {
		input.close();
	}

	/**
	 * @see stream.io.AbstractDataStream#readHeader()
	 */
	@Override
	public void readHeader() throws Exception {
	}

	/**
	 * @see stream.io.AbstractDataStream#readItem(stream.data.Data)
	 */
	@Override
	public synchronized Data readItem(Data instance) throws Exception {

		log.debug("Reading FRAME-" + frameId);
		// log.info("buffer.length = {}, limit = {}", buffer.length, limit);
		int read = input.read(buffer, limit, buffer.length - limit);
		if (read > 0)
			limit += read;
		// log.debug("Read {} bytes from input stream: {}", read,
		// this.getHex(buffer, 64));
		// log.debug("Signature is: {}", getHex(signature, 64));
		// log.debug("   buffer.limit is {}", limit);

		start = 0;
		int idx = findBytes(buffer, start, signature);
		while (idx != 0) {
			log.error(
					"Error! Expecting stream to start with image signature! Found index {}",
					idx);
			byte[] begin = new byte[32];
			for (int i = 0; i < begin.length && i < buffer.length; i++) {
				begin[i] = buffer[i];
			}
			log.error("   byte prefix was: {}", begin);
			return null;
		}

		int end = -1;
		do {
			end = findBytes(buffer, idx + signature.length, signature);
			if (end < 0) {
				while (limit < buffer.length) {
					int b = input.read();
					if (b > 0) {
						buffer[limit++] = (byte) b;
					} else
						break;
				}
				end = findBytes(buffer, idx + signature.length, signature);
			}
		} while (end < 0 && read > 0);

		//
		// copy the detected image to the image buffer and remove the bytes
		// from the temporary buffer
		//
		byte[] img = new byte[end];
		for (int k = 0; k < img.length; k++) {
			img[k] = buffer[k];
			buffer[k] = buffer[k + end];
		}
		for (int i = end + 1; i < limit - end; i++) {
			buffer[i] = buffer[i + end];
		}
		limit -= end;
		start = 0;
		// log.debug("## Frame offset is {}   decimal: {}",
		// Long.toHexString(offset), offset);
		offset += end;
		instance.put("frame:id", frameId++);
		instance.put(key, img);
		return instance;
	}

	public static int findBytes(byte[] buf, int from, byte[] sig) {

		for (int i = from; i + sig.length < buf.length; i++) {

			if (checkBytes(buf, i, sig))
				return i;
		}

		return -1;
	}

	public static String getHex(byte[] bytes, int len) {
		StringBuffer s = new StringBuffer("[");
		for (int i = 0; i < bytes.length && i < len; i++) {
			s.append(Integer.toHexString((int) bytes[i]));
			if (i + 1 < bytes.length)
				s.append(", ");
		}
		s.append("]");
		return s.toString();
	}

	protected static byte[] getFirst(byte[] buffer, int len) {
		return getFirst(buffer, 0, len);
	}

	protected static byte[] getFirst(byte[] buffer, int off, int len) {
		byte[] begin = new byte[len];
		for (int i = off; i < begin.length && i < buffer.length; i++) {
			begin[i] = buffer[i];
		}
		return begin;
	}

	protected static boolean checkBytes(byte[] buf, int pos, byte[] sig) {

		if (pos + sig.length < buf.length) {

			for (int p = 0; p < sig.length; p++) {

				if (buf[pos + p] != sig[p]) {
					return false;
				}
			}
		}

		log.debug("Found signature {} at position {}", new String(sig), pos);
		return true;
	}
}