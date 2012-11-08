/**
 * 
 */
package stream.io;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.annotations.Parameter;

/**
 * <p>
 * This class implements a stream of byte chunks which are created by reading
 * until a byte signature within the underlying stream indicates the start of a
 * new byte chunk.
 * </p>
 * <p>
 * This can be used for reading for example a stream of GIF of JPEG images from
 * a stream and splitting this stream in to the byte chunks for each image.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 * 
 */
public abstract class ByteChunkStream extends AbstractDataStream {

	static Logger log = LoggerFactory.getLogger(ByteChunkStream.class);
	public final static byte[] GIF_SIGNATURE = new byte[] { 0x47, 0x49, 0x46,
			0x38 };
	public final static byte[] JPG_SIGNATURE = new byte[] { (byte) 0xff,
			(byte) 0xd8, (byte) 0xff, (byte) 0xfe, 0x00, 0x0e, 0x4c, 0x61 };

	int chunkSize = 1;
	URL url;
	BufferedInputStream input;
	byte[] buffer = new byte[2048 * 1024];
	int start = 0;
	long frameId;
	int limit = 0;
	long offset = 0L;
	byte[] signature;

	public ByteChunkStream(URL url, byte[] signature) throws Exception {
		this(openUrl(url), signature);
		this.url = url;
	}

	public ByteChunkStream(InputStream in, byte[] signature) throws Exception {
		this.input = new BufferedInputStream(in);
		this.signature = signature;
	}

	public final static InputStream openUrl(URL url) throws Exception {
		if (url.toString().toLowerCase().endsWith(".gz")) {
			return new GZIPInputStream(url.openStream());
		} else
			return url.openStream();
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
	 * 
	 * @param str
	 */
	@Parameter(description = "The byte-signature for splitting the binary stream into chunks. This expects a comma separated list of byte values in hex-form, e.g. '0xa0,0xd0'.")
	public void setSignature(String str) {

		if (str.indexOf(",") > 0) {
			String[] tok = str.split(",");
			byte[] sig = new byte[tok.length];
			for (int i = 0; i < tok.length; i++) {
				int val;
				if (tok[i].startsWith("0x"))
					val = Integer.parseInt(tok[i].substring(2));
				else
					val = Integer.parseInt(tok[i]);

				if (val < 0 || val > 255) {
					throw new RuntimeException("Invalid byte value '" + tok[i]
							+ "'!");
				} else {
					sig[i] = (byte) val;
				}
			}
		}
	}

	/**
	 * @see stream.io.AbstractDataStream#readItem(stream.data.Data)
	 */
	@Override
	public synchronized Data readItem(Data instance) throws Exception {

		log.info("Reading FRAME-" + frameId);
		int read = input.read(buffer, limit, buffer.length - limit);
		if (read > 0)
			limit += read;
		// log.info("Read {} bytes from input stream.", read);
		// log.info("   buffer.limit is {}", limit);

		start = 0;
		int idx = findBytes(buffer, start, signature);
		if (idx != 0) {
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
		log.info("## Frame offset is {}   decimal: {}",
				Long.toHexString(offset), offset);
		offset += end;
		instance.put("@chunk:id", frameId++);
		instance.put("@bytes", img);
		return instance;
	}

	public static int findBytes(byte[] buf, int from, byte[] sig) {

		for (int i = from; i + sig.length < buf.length; i++) {

			if (checkBytes(buf, i, sig))
				return i;
		}

		return -1;
	}

	public static String getHex(byte[] bytes) {
		StringBuffer s = new StringBuffer("[");
		for (int i = 0; i < bytes.length; i++) {
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

		log.info("Found signature {} at position {}", new String(sig), pos);
		return true;
	}
}