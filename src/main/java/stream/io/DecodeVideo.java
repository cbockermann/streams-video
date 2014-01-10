/**
 * 
 */
package stream.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.flow.Emitter;

/**
 * @author chris
 * 
 */
public class DecodeVideo extends Emitter {

	static Logger log = LoggerFactory.getLogger(DecodeVideo.class);
	String key = "packet:data";

	ByteArrayOutputStream chunks = new ByteArrayOutputStream();
	int chunksWritten = 0;
	Long frameId = 0L;

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data processMatchingData(Data input) {

		Serializable value = input.get(key);
		if (value != null && value.getClass().isArray()
				&& value.getClass().getComponentType() == byte.class) {
			// log.info("found byte array for decoding: {}", value);
			byte[] data = (byte[]) value;

			int pid = (Integer) input.get("packet:pid");
			// log.info("pid is {}", pid);
			if (pid == 0 && chunksWritten > 0) {
				log.info("{} chunks in memory, flushing and decoding...",
						chunksWritten);
				try {
					File video = File.createTempFile("video", ".ts");
					// video.deleteOnExit();
					FileOutputStream fos = new FileOutputStream(video);
					fos.write(chunks.toByteArray());
					fos.close();
					log.info("Wrote video chunk to {}", video);

					chunks = new ByteArrayOutputStream();
					chunksWritten = 0;

					File tempData = File.createTempFile("decoded-frames",
							".mjpeg");
					// tempData.deleteOnExit();

					String ffmpeg = Ffmpeg.getBinary();

					String cmd = ffmpeg + " -v quiet -y -i "
							+ video.getAbsolutePath()
							+ " -vcodec mjpeg -s 384x216 "
							+ tempData.getAbsolutePath();
					log.info("Calling  '{}'", cmd);

					Process p = Runtime.getRuntime().exec(cmd);

					dump(p.getErrorStream());

					int ret = p.waitFor();
					log.info("Process exited with code {}.", ret);

					File wavData = File.createTempFile("wav-samples", ".wav");
					cmd = ffmpeg + " -v quiet -y -i " + video.getAbsolutePath()
							+ " -acodec pcm_u8 " + wavData.getAbsolutePath();
					log.info("Calling  '{}'", cmd);

					p = Runtime.getRuntime().exec(cmd);
					dump(p.getErrorStream());

					MJpegImageStream frames = new MJpegImageStream(
							new FileInputStream(tempData));
					frames.init();

					WavStream samples = new WavStream(new SourceURL("file:"
							+ wavData.getAbsolutePath()));
					samples.setBlockSize(48000 / 25);
					samples.init();

					Data frame = frames.read();
					while (frame != null) {
						frame.put("frame:id", frameId++);
						frame.put("frame:time", frameId.doubleValue()
								* (1 / 25.0));

						Data audio = samples.read();
						if (audio != null) {
							frame.putAll(audio);
						}
						emit(frame);
						frame = frames.read();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					// log.info("Remembering chunk data...");
					chunks.write(data);
					chunksWritten++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return input;
	}

	public void setOutput(Sink[] sink) {
		setSinks(sink);
	}

	public Sink[] getOutput() {
		return this.sinks;
	}

	public void dump(InputStream in) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = reader.readLine();
		while (line != null) {
			System.out.println(line);
			line = reader.readLine();
		}
		reader.close();
	}
}
