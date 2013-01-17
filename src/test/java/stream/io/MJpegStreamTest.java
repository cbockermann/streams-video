/**
 * 
 */
package stream.io;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.util.ByteSize;

/**
 * @author chris
 * 
 */
public class MJpegStreamTest {

	static Logger log = LoggerFactory.getLogger(MJpegStreamTest.class);

	@Test
	public void test() {
	}

	public static void main(String[] args) {
		try {
			SourceURL url = new SourceURL(
					"http://kirmes.cs.uni-dortmund.de/video/20120911-micro.raw");
			url = new SourceURL("file:/Volumes/RamDisk/20120911-micro.raw");
			// url = new SourceURL(
			// "http://kirmes.cs.uni-dortmund.de/video/20120911-small-1000f.raw");
			MJpegImageStream stream = new MJpegImageStream(url);
			stream.setReadBufferSize(new ByteSize("16"));
			stream.setBufferSize(new ByteSize("16M"));
			stream.setLimit(1000L);
			stream.init();
			int frames = 0;
			Data item = stream.read();
			while (item != null) {
				// if (frames % 100 == 0) {
				// System.out.println(frames + " frames read. ");
				// }
				if (item.containsKey("error:data")) {
					dump((byte[]) item.get("error:data"), "frame-" + frames
							+ ".jpg");
				}
				//
				// if (frames > 998) {
				// dump((byte[]) item.get("data"), "frame-" + frames + ".jpg");
				// }
				log.info("frame:size_raw is: {}", item.get("frame:size_raw"));

				frames++;
				item = stream.read();
			}

			// System.out.println(frames + " frames read in total.");
			stream.info();

			Assert.assertTrue(frames == 23461);

		} catch (Exception e) {
			e.printStackTrace();
			fail("Error: " + e.getMessage());
		}

	}

	public static void dump(byte[] data, String f) throws IOException {
		File file = new File("/Volumes/RamDisk/frame-errors/" + f);
		file.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(data);
		fos.close();
	}

}
