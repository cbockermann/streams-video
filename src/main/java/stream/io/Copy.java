/**
 * 
 */
package stream.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * @author chris
 * 
 */
public class Copy {

	public final static byte[] JPEG_SOI = new byte[] { (byte) 0xff, (byte) 0xd8 };
	public final static byte[] JPEG_EOI = new byte[] { (byte) 0xff, (byte) 0xd9 };

	final static ArrayList<byte[]> chunks = new ArrayList<byte[]>();

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		InputStream in;
		OutputStream out = null;

		if ("-".equals(args[0]))
			in = System.in;
		else
			in = new FileInputStream(new File(args[0]));

		if (args.length > 1) {
			File file = new File(args[1]);
			System.err.println("Copying standard output to " + file);
			out = new FileOutputStream(file);
		}
		boolean verbose = System.getProperty("verbose") != null;

		byte[] buf = new byte[1024 * 1024 * 16];
		int total = 0;
		int read = 0;
		int count = 0;
		// int start = -2;
		// int end = -1;
		Long frames = 0L;

		long begin = System.currentTimeMillis();

		ByteBuffer current = ByteBuffer.allocateDirect(buf.length * 4);

		do {

			while (in.available() == 0) {
				try {
					Thread.sleep(5);
				} catch (Exception e) {
				}
			}

			// System.err.println(System.in.available()
			// + " bytes available for reading...");
			read = in.read(buf);
			if (verbose)
				System.out.println(read + " bytes read.");

			int idx = indexOf(buf, JPEG_SOI, 0);
			int offset = 0;
			if (idx >= 0) {
				System.err.println("Found JPG SOI at " + (idx));
				offset = idx;
				byte[] img = new byte[current.position() + idx];
				current.flip();
				current.get(img);

				frames++;
				chunks.add(img);
				if (chunks.size() > 10) {
					System.err.println("Dropping frame...");
					chunks.remove(0);
				}

				current.clear();
			} else {
				if (current.remaining() < buf.length) {
					System.err
							.println("No start found, buffer full, clearing buffer and starting anew");
					current.clear();
				}
			}

			current.put(buf, offset, read - offset);

			//
			// if (start < end) {
			// int idx = indexOf(buf, JPEG_SOI, 0);
			// if (idx >= 0) {
			// start = total + idx;
			// if (verbose)
			// System.err.println("Found JPG SOI at " + (start));
			// }
			// } else {
			// int idx = indexOf(buf, JPEG_EOI, 0);
			// if (idx >= 0) {
			// end = total + idx;
			// if (verbose)
			// System.err.println("Found JPG EOI at " + (end));
			// frames++;
			// }
			// }

			if (read > 0) {
				if (out != null)
					out.write(buf, 0, read);

				total += read;

				count++;
				if (verbose || frames % 10 == 0) {
					Long time = System.currentTimeMillis() - begin;
					Double seconds = time.doubleValue() / 1000.0d;

					System.err.println(count + " reads, " + total
							+ " bytes read so far (" + (total / seconds)
							+ " bytes/second)");

					System.err.println(frames + " frames read ("
							+ (frames.doubleValue() / seconds) + " fps)");
				}
			}
		} while (read >= 0);
		System.err.println(total + " bytes written, last read was: " + read);
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