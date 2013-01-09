/**
 * 
 */
package stream.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;

/**
 * @author chris
 * 
 */
public class PlayWavData extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(PlayWavData.class);

	final static int AUDIO_BUFFER = 128 * 1024; // 128kb audio buffer

	AudioFormat header;
	SourceDataLine audio;
	Double volume = 0.5;
	Integer blocksProcessed = 0;
	Long samplesWritten = 0L;

	/**
	 * @return the volume
	 */
	public Double getVolume() {
		return volume;
	}

	/**
	 * @param volume
	 *            the volume to set
	 */
	public void setVolume(Double volume) {

		if (volume > 1.0) {
			this.volume = 1.0;
			return;
		}

		if (volume < 0.0) {
			this.volume = 0.0;
			return;
		}

		this.volume = volume;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if (header == null) {

			if (input.containsKey("wav:samplerate")) {

				Long sampleRate = new Long(input.get("wav:samplerate") + "");
				Integer channels = new Integer(input.get("wav:channels") + "");
				Integer bytesPerSample = new Integer(
						input.get("wav:bytesPerSample") + "");

				header = new AudioFormat(sampleRate.floatValue(),
						8 * bytesPerSample, channels, true, true);

				log.info("Initializing audio output...");

				Info info = new Info(SourceDataLine.class, header);
				try {
					log.info("Acquiring audio-line from AudioSystem...");
					audio = (SourceDataLine) AudioSystem.getLine(info);
					log.info("Opening audio-line...");
					audio.open(header, AUDIO_BUFFER);
				} catch (LineUnavailableException e) {
					log.error("Unable to acquire audio-line: {}",
							e.getMessage());
					e.printStackTrace();
				}

				log.debug("Starting audio-device...");
				audio.start();
			}
		}

		if (header != null) {
			blocksProcessed++;
			double[] samples = (double[]) input.get("wav:samples");

			byte[] bytes = new byte[samples.length];
			for (int i = 0; i < samples.length; i++) {
				bytes[i] = (byte) (volume * samples[i]);
			}

			// log.info("Writing {} bytes to audio-line...", bytes.length);
			audio.write(bytes, 0, bytes.length);
			samplesWritten += bytes.length;

			Long frames = audio.getLongFramePosition();
			Float sampleRate = header.getSampleRate();
			Double time = (frames.doubleValue() / sampleRate.doubleValue());
			if (blocksProcessed % 15 == 0) {
				log.info("Audio position is: {}", time);
			}

		} else {
			log.error("No audio-header information received, yet.");
		}

		return input;
	}

	/**
	 * @see stream.AbstractProcessor#finish()
	 */
	@Override
	public void finish() throws Exception {
		super.finish();

		if (audio != null) {
			if (audio.isRunning())
				audio.stop();
			audio.close();
		}
	}
}