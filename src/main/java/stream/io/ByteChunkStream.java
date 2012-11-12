/**
 * 
 */
package stream.io;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.annotations.Parameter;

/**
 * <p>
 * This class implements a fast byte-oriented stream of byte chunks. The chunks
 * are found by checking for a start-signature (i.e. byte array). The stream
 * returns a sequence of data items, each holding a chunk of bytes.
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
			(byte) 0xd8 };

	SourceURL url;
	ByteBuffer buffer;

	/* The byte-channel which is being read from using NIO */
	final ReadableByteChannel channel;

	Long frameId = 0L;
	final byte[] signature;

	/* The name of the attribute into which the byte arrays (chunks) are put */
	String key = "data";

	// The 'look-ahead' buffer
	int bufferSize = 2 * 16 * 1024;

	// the timestamp of the first read (for debugging/timing-measurement)
	Long firstRead = 0L;

	// the number of bytes read so fare
	Long bytesRead = 0L;

	public ByteChunkStream(SourceURL url, byte[] signature) throws Exception {
		this(url.openStream(), signature);
		this.url = url;
	}

	public ByteChunkStream(InputStream in, byte[] signature) throws Exception {
		// this.input = new BufferedInputStream(in);
		channel = Channels.newChannel(in);
		this.signature = signature;
	}

	/**
	 * @see stream.io.AbstractDataStream#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();
		int bufSize = 1024 * 16;
		try {
			bufSize = new Integer(System.getProperty(
					"stream.io.ImageStream.buffer", "" + (2 * 1024 * 16)));

		} catch (Exception e) {
			e.printStackTrace();
			bufSize = 1024 * 16;
		}
		log.info("Using buffer size of {}k", bufSize / 1024);
		buffer = ByteBuffer.allocateDirect(bufSize);
		if (buffer.isDirect()) {
			log.info("ByteBuffer is using direct memory.");
		} else {
			log.info("ByteBuffer is non-direct memory.");
		}
	}

	/**
	 * @see stream.io.DataStream#close()
	 */
	@Override
	public void close() throws Exception {
		buffer.clear();
		channel.close();
	}

	/**
	 * @see stream.io.AbstractDataStream#readHeader()
	 */
	@Override
	public void readHeader() throws Exception {
	}

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize
	 *            the bufferSize to set
	 */
	@Parameter(description = "The internal buffer size of this stream.")
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * 
	 * @param sig
	 * @return
	 */
	private int indexOf(byte[] sig) {
		return indexOf(sig, 0);
	}

	/**
	 * 
	 * @param sig
	 * @param from
	 * @return
	 */
	private int indexOf(byte[] sig, int from) {
		int pos = from;
		while (pos + sig.length < buffer.limit() && !isSignatureAt(pos, sig)) {
			pos++;
		}

		if (pos + sig.length >= buffer.limit()) {
			return -1;
		}

		return pos;
	}

	/**
	 * 
	 * 
	 * @param pos
	 * @param sig
	 * @return
	 */
	private boolean isSignatureAt(int pos, byte[] sig) {

		for (int i = 0; i < sig.length; i++) {
			if (buffer.get(pos + i) != sig[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @see stream.io.AbstractDataStream#readItem(stream.data.Data)
	 */
	@Override
	public synchronized Data readItem(Data instance) throws Exception {
		// log.debug("Reading FRAME-" + frameId);

		int read = channel.read(buffer);
		while (read == 0) {
			Thread.yield();
			read = channel.read(buffer);
			if (read < 0) {
				return null;
			}
		}

		if (read < 0) {
			return null;
		}

		if (read > 0)
			bytesRead += read;

		int start = indexOf(signature);
		while (start < 0) {
			//
			// skip to the end of the buffer and clear it
			//
			buffer.position(buffer.limit() - signature.length);
			buffer.compact();

			// read new data into the buffer
			//
			read = channel.read(buffer);

			while (read == 0) {
				Thread.yield();
				read = channel.read(buffer);
			}

			if (read < 0) {
				return null;
			}

			bytesRead += read;
			start = indexOf(signature);
		}

		buffer.mark();

		int end = indexOf(signature, start + signature.length);
		while (end < 0 && buffer.capacity() > 0) {
			read = channel.read(buffer);

			while (read == 0) {
				Thread.yield();
				read = channel.read(buffer);
			}

			if (read < 0) {
				return null;
			}

			end = indexOf(signature);
		}

		if (end < 0) {
			return null;
		}

		buffer.position(start);

		byte[] output = new byte[end - start];
		buffer.get(output, 0, (end - start));

		buffer.compact();
		instance.put("frame:id", frameId++);
		instance.put(key, output);

		if (firstRead == 0L) {
			firstRead = System.currentTimeMillis();
		} else {
			if (log.isDebugEnabled()) {
				if (frameId % 100 == 0) {
					Long seconds = (System.currentTimeMillis() - firstRead);
					log.debug("Reading rate after {} frames is {} fps",
							frameId, (1000 * (frameId.doubleValue() / seconds
									.doubleValue())));
					log.debug("{} bytes read", bytesRead);
				}
			}
		}

		return instance;
	}
}