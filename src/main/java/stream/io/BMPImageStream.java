/**
 * 
 */
package stream.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.data.DataFactory;
import stream.image.DisplayImage;
import stream.image.ImageRGB;
import stream.runtime.ProcessContextImpl;

/**
 * @author chris
 * 
 */
public class BMPImageStream extends AbstractStream {

	static Logger log = LoggerFactory.getLogger(BMPImageStream.class);
	SourceURL url;

	public BMPImageStream(SourceURL source) {
		super(source);
	}

	/**
	 * @see stream.io.AbstractStream#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();
	}

	/**
	 * @see stream.io.AbstractStream#readNext()
	 */
	@Override
	public Data readNext() throws Exception {

		try {
			// log.info("Starting at offset {}", dataStream.bytesRead());
			String bfType = "";
			ByteBuffer head = ByteBuffer.allocate(14 + 40);
			ReadableByteChannel bc = Channels.newChannel(getInputStream());

			while (head.hasRemaining()) {
				int ret = bc.read(head);
				if (ret < 0)
					return null;
			}
			head.flip();
			head.order(ByteOrder.LITTLE_ENDIAN);

			// log.info("Header: {} bytes read", head.limit());

			bfType = new String(new char[] { (char) head.get(),
					(char) head.get() });
			// log.info("bfType: {}, remaining: {}", bfType, head.remaining());

			// int bfSize = head.get() << 24 | head.get() << 16 | head.get() <<
			// 8
			// | head.get();
			int bfSize = head.getInt();
			// log.info("bfSize: {}, remaining: {}", bfSize, head.remaining());

			// int bfReserved = header[6] << 24 | header[7] << 16 | header[8] <<
			// 8
			// | header[9];
			int bfReserved = head.getInt();
			// log.info("bfReserved: {}, remaining: {}", bfReserved,
			// head.remaining());
			int bfOffsetBits = head.getInt();

			//
			// --------
			//

			int biSize = head.getInt();
			int biWidth = head.getInt();
			int biHeight = head.getInt();
			int biPlanes = head.getShort();
			int biBitCount = head.getShort();
			int biCompression = head.getInt();
			int biSizeImage = head.getInt();
			int biXPelsPerMeter = head.getInt();
			int biYPelsPerMeter = head.getInt();
			int biClrUsed = head.getInt();
			int biClrImportant = head.getInt();

			if (log.isDebugEnabled()) {
				log.debug("bfType: {}", bfType);
				log.debug("bfSize: {}", bfSize);
				log.debug("bfReserved: {}", bfReserved);
				log.debug("bfOffsetBits: {}", bfOffsetBits);
				log.debug("biSize: {}", biSize);
				log.debug("biWidth: {}", biWidth);
				log.debug("biHeight: {}", biHeight);
				log.debug("biPlanes: {}", biPlanes);
				log.debug("biBitCount: {}", biBitCount);
				log.debug("biCompression: {}", biCompression);
				log.debug("biSizeImage: {}", biSizeImage);
				log.debug("biXPelsPerMeter: {}", biXPelsPerMeter);
				log.debug("biYPelsPerMeter: {}", biYPelsPerMeter);
				log.debug("biClrUsed: {}", biClrUsed);
				log.debug("biClrImportant: {}", biClrImportant);
			}
			log.info("Skipping {} bytes for BitmapInfoHeader...", biSize);

			ByteBuffer buf = ByteBuffer.allocate(biSizeImage);
			// log.debug("{} bytes remaining for image-data", buf.remaining());
			// log.info("Inserting head into buffer...");
			// buf.put(head);
			// log.debug("{} bytes remaining for image-data", buf.remaining());

			int read = bc.read(buf);
			while (buf.remaining() > 0) {
				read = bc.read(buf);
				if (read < 0)
					return null;
			}

			// log.debug("{} bytes of image-data read...", read);
			// log.debug("This results in {} pixels", buf.position() / 3);
			buf.flip();

			ImageRGB image = new ImageRGB(biWidth, biHeight);
			int len = image.pixels.length;
			for (int p = 1; p < len; p++) {
				int b = buf.get();
				int g = ((int) buf.get() << 8);
				int r = ((int) buf.get() << 16);
				image.pixels[len - p] = (0xff << 24) | r | g | b;
				// image.pixels[p] = (r << 16) | (g << 8) | b;
			}

			Data item = DataFactory.create();
			// item.put("image:data", buf.array());
			item.put("frame:image", image);
			item.put("frame:image:width", image.width);
			item.put("frame:image:height", image.height);
			return item;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		BMPImageStream stream = new BMPImageStream(new SourceURL(
				"file:/Volumes/RamDisk/rtl.bmp"));
		stream.init();

		DisplayImage display = new DisplayImage();
		display.setKey("frame:image");
		display.init(new ProcessContextImpl());
		Long start = System.currentTimeMillis();
		int count = 0;
		Data item = stream.read();
		while (item != null && count < 1) {
			count++;
			display.process(item);
			item = stream.read();
			// System.out.println("image item: " + item);
		}
		Long duration = System.currentTimeMillis() - start;
		log.info("Rate is {}/sec", count / (duration.doubleValue() / 1000.0d));
	}
}
