/**
 * 
 */
package stream.laser;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;
import stream.io.WavStream;

/**
 * @author chris
 * 
 */
public class LaserSound {

	static Logger log = LoggerFactory.getLogger(LaserSound.class);

	final String[] names = new String[] { "on0", "off0", "swing7" };
	final Map<String, Data> sounds = new HashMap<String, Data>();

	AudioFormat header;
	SourceDataLine audio;

	public LaserSound() {

		for (String name : names) {
			URL url = LaserSound.class.getResource("/laser/game/sounds/new-"
					+ name + ".wav");
			log.info("Sound file: {}", url);
			if (url != null) {
				Data sound = readSound(url);
				log.info("Sound '{}' is: {}", name, sound);
				if (sound != null) {
					sounds.put(name, sound);
				}
			}
		}

	}

	public void init() {
		try {

			Long sampleRate = 48000L;
			Integer channels = 1;
			Integer bytesPerSample = 1;

			header = new AudioFormat(sampleRate.floatValue(),
					8 * bytesPerSample, channels, true, false);

			log.info("Initializing audio output...");

			Info info = new Info(SourceDataLine.class, header);
			try {
				log.info("Acquiring audio-line from AudioSystem...");
				audio = (SourceDataLine) AudioSystem.getLine(info);
				log.info("Opening audio-line...");
				audio.open(header, 256 * 1024);
			} catch (LineUnavailableException e) {
				log.error("Unable to acquire audio-line: {}", e.getMessage());
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void play(String key) {
		Data item = sounds.get(key);
		if (item != null) {

			if (!audio.isRunning()) {
				log.debug("Starting audio-device...");
				audio.start();
			}
			double[] samples = (double[]) item.get("wav:samples");

			byte[] bytes = new byte[samples.length];
			for (int i = 0; i < samples.length; i++) {
				bytes[i] = (byte) (0.5 * samples[i]);
			}

			log.info("Writing {} bytes to audio-line...", bytes.length);
			audio.write(bytes, 0, bytes.length);
		} else {
			log.info("Sound '{}' not loaded!");
		}
	}

	public Set<String> getSoundNames() {
		return sounds.keySet();
	}

	public static Data readSound(URL url) {

		try {
			SourceURL source = new SourceURL(url);
			WavStream stream = new WavStream(source);
			stream.setBlockSize(48000 * 10);
			stream.init();
			Data sound = stream.read();

			double[] samples = (double[]) sound.get("wav:samples");

			stream.close();
			return sound;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			LaserSound sounds = new LaserSound();
			sounds.init();
			sounds.play("off0");

			sounds.play("on0");
			//
			// PlayWavData audio = new PlayWavData();
			// audio.init(new ProcessContextImpl());
			//
			// for (String name : sounds.getSoundNames()) {
			// log.info("Playing sound '{}'", name);
			// audio.process(sounds.sounds.get(name));
			//
			// Thread.sleep(1500);
			// }
			//
			// audio.finish();
			Thread.sleep(5000);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
