/**
 * 
 */
package stream.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A new version of the ByteChunkStream. It will search for starting signatures
 * of byte chunks and emit those chunks.
 * </p>
 * 
 * @author Christian Bockermann
 * 
 */
public class JpegStream {

    static Logger log = LoggerFactory.getLogger(JpegStream.class);
    public final static byte[] JPEG_SOI = new byte[] { (byte) 0xff, (byte) 0xd8 };
    public final static byte[] JPEG_EOI = new byte[] { (byte) 0xff, (byte) 0xd9 };

    final static ArrayList<byte[]> chunks = new ArrayList<byte[]>();

    final InputStream in;
    final int readBufferSize;
    final byte[] buf;
    final ByteBuffer current;

    final boolean continuous;
    boolean verbose = false;
    Long frames = 0L;
    long begin = 0L;
    long total = 0L;
    boolean closed = false;
    int start = -1;
    int end = -1;
    boolean endOfStream = false;

    public JpegStream(InputStream in, int readBufferSize, int maxResultBuffer, boolean continuous) {
        this.in = in;
        this.readBufferSize = readBufferSize;
        this.buf = new byte[readBufferSize];

        current = ByteBuffer.allocateDirect(maxResultBuffer);
        current.clear();
        if (readBufferSize > maxResultBuffer)
            throw new RuntimeException("Result buffer cannot be smaller than read-buffer!");
        this.continuous = continuous;
        log.debug("Stream created.");
    }

    public long getBytesRead() {
        return total;
    }

    public byte[] readNextChunk() throws IOException {

        if (endOfStream)
            return null;

        if (begin == 0L)
            begin = System.currentTimeMillis();
        int read = 0;
        do {
            int off = 0;
            while (start < 0 && read >= 0) {
                // log.info("Searching for JPEG_SOI at 0");
                read = readBytes();

                start = indexOf(JPEG_SOI, off);
                if (start < 0 && read == 0) {
                    // buffer is full
                    current.clear();
                    current.flip();
                }

            }

            // log.info("Found JPEG_SOI at {}", start);
            if (start < 0)
                return null;

            // log.info("Found SOI at {}", start);
            off = start;
            while (end < 0 && read >= 0) {
                // log.info("Searching for JPEG_EOI starting from {}", start
                // + JPEG_EOI.length);
                read = readBytes();
                end = indexOf(JPEG_EOI, off);
            }

            if (end < 0) {
                endOfStream = true;
                current.flip();
                byte[] img = new byte[current.limit()];
                log.debug("hit end-of-file/stream, dumping remaining {} bytes", current.limit());
                current.get(img, 0, img.length);
                return img;
            }
            // log.info("Found EOI at {}", end);

            byte[] image = new byte[end - start + JPEG_EOI.length];
            current.flip();
            current.position(start);
            current.get(image, 0, image.length);
            // int copied = current.position();
            current.compact();
            // log.info("frame is {} bytes in size, {} bytes read so far...",
            // image.length, total);
            // current.flip();

            start = -1;
            end = -1;

            frames++;
            if (log.isDebugEnabled() && frames % 100 == 0) {
                Long time = System.currentTimeMillis() - begin;
                log.debug("Read {} frames, frame rate is {} fps", frames, (100.0d / (time.doubleValue() / 1000.0d)));
                begin = System.currentTimeMillis();
            }

            // if (frames > 23000)
            // log.info("{} frames read", frames);
            return image;
        } while (read >= 0);
    }

    public void close() {
        closed = true;
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

        while (pos + sig.length < buffer.length && !isSignatureAt(buffer, pos, sig)) {
            pos++;
        }

        if (pos + sig.length >= buffer.length) {
            return -1;
        }

        return pos;
    }

    protected int indexOf(byte[] sig, int from) {
        int pos = from;
        while (pos + sig.length < current.position()) {
            if (isSignatureAt(sig, pos)) {
                return pos;
            }
            pos++;
        }

        return -1;
    }

    protected boolean isSignatureAt(byte[] sig, int pos) {
        for (int i = 0; i < sig.length; i++) {
            byte b = current.get(pos + i);
            if (b != sig[i]) {
                return false;
            }
        }
        return true;
    }

    protected int readBytes() throws IOException {

        int free = current.limit() - current.position();

        if (free == 0) {
            log.debug("Buffer full!");
            return 0;
        }

        int until = Math.min(free, buf.length);
        // log.info("Reading buf[0..{}]", until);
        int read = in.read(buf, 0, until);
        // log.info("{} bytes read from input-stream...", read);
        if (read < 0)
            return -1;

        current.put(buf, 0, read);
        total += read;
        return read;
    }
}