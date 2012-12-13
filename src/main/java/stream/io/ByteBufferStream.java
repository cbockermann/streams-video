/**
 * 
 */
package stream.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * <p>
 * A new version of the ByteChunkStream. It will search for starting signatures
 * of byte chunks and emit those chunks.
 * </p>
 * 
 * @author Christian Bockermann
 * 
 */
public class ByteBufferStream {

	public final static byte[] JPEG_SOI = new byte[] { (byte) 0xff, (byte) 0xd8 };
	public final static byte[] JPEG_EOI = new byte[] { (byte) 0xff, (byte) 0xd9 };

	final static ArrayList<byte[]> chunks = new ArrayList<byte[]>();

	final InputStream in;
	final int readBufferSize;
	final byte[] buf;
	final ByteBuffer current;
	final byte[] startSignature;

	final boolean continuous;
	boolean verbose = false;
	Long frames = 0L;
	long begin = 0L;
	long total = 0L;

	public ByteBufferStream(InputStream in, int readBufferSize,
			int maxResultBuffer, byte[] startSig, boolean continuous) {
		this.in = in;
		this.readBufferSize = readBufferSize;
		this.buf = new byte[readBufferSize];
		this.startSignature = startSig;

		this.current = ByteBuffer.allocateDirect(maxResultBuffer);
		if (readBufferSize > maxResultBuffer)
			throw new RuntimeException(
					"Result buffer cannot be smaller than read-buffer!");
		this.continuous = continuous;
	}

	public byte[] readNextChunk() throws IOException {

		if (begin == 0L)
			begin = System.currentTimeMillis();
		int read = 0;
		do {

			while (in.available() == 0) {
				try {
					Thread.sleep(5);
				} catch (Exception e) {
				}
			}

			read = in.read(buf);
			if (read > 0) {
				total += read;
			}

			// if (verbose)
			// System.out.println(read + " bytes read.");

			int idx = indexOf(buf, startSignature, 0);
			int offset = 0;
			if (idx >= 0) {
				// System.err.println("Found starting byte-signature at "
				// + (total + idx));
				offset = idx;
				if (offset > 0)
					current.put(buf, 0, offset);
				// System.err.println("Offset is: " + offset);
				byte[] img = new byte[current.position()];
				current.flip();
				current.get(img);
				// current.compact();

				frames++;

				if (verbose || frames % 50 == 0) {
					Long time = System.currentTimeMillis() - begin;
					Double seconds = time.doubleValue() / 1000.0d;

					System.err.println(frames + " frames read ("
							+ (frames.doubleValue() / seconds) + " fps, "
							+ (total / seconds) + " bytes/second)");
				}

				current.clear();
				current.put(buf, offset, read - offset);
				return img;
			} else {
				if (current.remaining() < buf.length) {
					System.err
							.println("No start found, buffer full, clearing buffer and starting anew");
					current.clear();
				}
				current.put(buf, 0, read);
			}

		} while (read >= 0);
		System.err.println(total + " bytes written, last read was: " + read);

		System.out
				.println("Returning remaining buffer content as last chunk...");
		byte[] img = new byte[current.limit()];
		current.flip();
		current.get(img);
		return img;

	}

	/**
	 * @param pos
	 * @param sig
	 * @return
	 */
	protected static boolean isSignatureAt(byte[] buffer, int pos, byte[] sig) {

		for (int i = 0; i < sig.length; i++) {
			byte b = buffer[pos + i];
			if (b != sig[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 
	 * @param sig
	 * @param from
	 * @return
	 */
	protected static int indexOf(byte[] buffer, byte[] sig, int from) {
		int pos = from;

		while (pos + sig.length < buffer.length
				&& !isSignatureAt(buffer, pos, sig)) {
			pos++;
		}

		if (pos + sig.length >= buffer.length) {
			return -1;
		}

		return pos;
	}
}