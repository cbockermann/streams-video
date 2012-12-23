/**
 * 
 */
package stream.io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import stream.Data;
import stream.data.DataFactory;
import stream.image.ImageRGB;
import stream.util.ByteSize;

/**
 * @author chris
 * 
 */
public class MJpegImageStream extends AbstractStream {

	public final static int DEFAULT_READ_BUFFER = 1 * 1024;
	public final static int DEFAULT_FRAME_BUFFER = 16 * 1024 * 1024;

	protected JpegStream stream;
	protected ByteSize readBufferSize = new ByteSize("1k");
	protected ByteSize bufferSize = new ByteSize("16mb");
	protected boolean continuous = false;
	int ok = 0;
	int errors = 0;
	long frame = 0;

	public MJpegImageStream(SourceURL url) throws Exception {
		super(url);
	}

	public MJpegImageStream(InputStream in) throws Exception {
		super(in);
	}

	/**
	 * @return the readBufferSize
	 */
	public ByteSize getReadBufferSize() {
		return readBufferSize;
	}

	/**
	 * @param readBufferSize
	 *            the readBufferSize to set
	 */
	public void setReadBufferSize(ByteSize readBufferSize) {
		this.readBufferSize = readBufferSize;
	}

	/**
	 * @return the bufferSize
	 */
	public ByteSize getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize
	 *            the bufferSize to set
	 */
	public void setBufferSize(ByteSize bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * @return the continuous
	 */
	public boolean isContinuous() {
		return continuous;
	}

	/**
	 * @param continuous
	 *            the continuous to set
	 */
	public void setContinuous(boolean continuous) {
		this.continuous = continuous;
	}

	/**
	 * @see stream.io.AbstractStream#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();

		stream = new JpegStream(getInputStream(), readBufferSize.getBytes(),
				bufferSize.getBytes(), isContinuous());
	}

	/**
	 * @see stream.io.AbstractStream#readNext()
	 */
	@Override
	public Data readNext() throws Exception {

		byte[] data = stream.readNextChunk();
		if (data == null)
			return null;

		Data item = DataFactory.create();
		// item.put("frame:data", data);
		item.put("frame:size_raw", data.length);

		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
			ImageRGB rgb = new ImageRGB(img);
			item.put("data", rgb);
			item.put("frame:width", rgb.width);
			item.put("frame:height", rgb.height);
			ok++;
			log.debug("Successfully parsed JPEG image...");
		} catch (Exception e) {
			log.error("Failed to read from image: {}", e.getMessage());
			errors++;
			if (log.isDebugEnabled())
				e.printStackTrace();
			item.put("error:data", data);
		}

		return item;
	}

	/**
	 * @see stream.io.AbstractStream#close()
	 */
	@Override
	public void close() throws Exception {
		super.close();
	}

	public void info() {
		log.info("{} frames were read without problems.", ok);
		log.info("{} frames could not be read", errors);
		log.info("{} frames read in total.", ok + errors);
	}

	public static void main(String[] args) throws Exception {

		SourceURL url = new SourceURL("file:/Volumes/RamDisk/laser.mjpeg");
		MJpegImageStream stream = new MJpegImageStream(url);
		stream.setReadBufferSize(new ByteSize("1k"));
		stream.setBufferSize(new ByteSize("8mb"));
		stream.init();

		int frames = 0;
		Data item = null;
		do {
			item = stream.read();
			if (item != null)
				frames++;
		} while (item != null);

		log.info("{} frames read.", frames);
	}
}