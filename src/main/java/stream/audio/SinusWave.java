/**
 * 
 */
package stream.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.runtime.ProcessContextImpl;

/**
 * @author chris
 * 
 */
public class SinusWave extends AbstractStream {

	static Logger log = LoggerFactory.getLogger(SinusWave.class);
	Float amplitude = 127.0f;
	Double frequency = 261.63d;
	Integer sampleRate = 48000;
	int blockSize = sampleRate;

	double[] block;
	int blocksRead = 0;
	int off = 0;
	String prefix = "wav:";
	String key = "samples";

	/**
	 * @return the amplitude
	 */
	public Float getAmplitude() {
		return amplitude;
	}

	/**
	 * @param amplitude
	 *            the amplitude to set
	 */
	@Parameter(description = "The amplitude of the sine wave signal. Default is 127.", required = false)
	public void setAmplitude(Float amplitude) {
		this.amplitude = amplitude;
	}

	/**
	 * @return the frequency
	 */
	public Double getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency
	 *            the frequency to set
	 */
	@Parameter(description = "The frequency of the sine wave. Default value is 261.63.", required = false)
	public void setFrequency(Double frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return the sampleRate
	 */
	public Integer getSampleRate() {
		return sampleRate;
	}

	/**
	 * @param sampleRate
	 *            the sampleRate to set
	 */
	@Parameter(description = "The sampling rate, default is 48.000 Hz.", required = false)
	public void setSampleRate(Integer sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * @return the blockSize
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * @param blockSize
	 *            the blockSize to set
	 */
	@Parameter(description = "Number of samples collected in each item. Default is 48000.", required = false)
	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	/**
	 * @see stream.io.AbstractStream#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();

		block = new double[blockSize];

		// length of a single sinus period, ie interval = 1 * Math.PI
		Double interval = sampleRate.doubleValue() / frequency;

		log.info("Sinus interval is {}", interval);
	}

	/**
	 * @see stream.io.AbstractStream#readNext()
	 */
	@Override
	public Data readNext() throws Exception {

		Data item = DataFactory.create();

		if (blocksRead == 0) {
			item.put(prefix + "samplerate", sampleRate);
			item.put(prefix + "channels", 1);
			item.put(prefix + "bytesPerSample", 1);
		}

		double freq = frequency;
		// if (blocksRead > 0) {
		// frequency = Math.pow(2, 1 / 12.0d) * frequency;
		// log.info("Frequency is: {}", frequency);
		// }

		double sampleInterval = sampleRate.doubleValue() / freq;

		double[] bytes = new double[blockSize];

		for (int i = 0; i < block.length; i++) {
			double angle = (2.0 * Math.PI * (off + i)) / sampleInterval;
			bytes[i % block.length] = (Math.sin(angle) * amplitude);
		}

		off += bytes.length;
		item.put(prefix + key, bytes);
		blocksRead++;
		return item;
	}

	public static void main(String[] args) {

		try {
			SinusWave sine = new SinusWave();
			sine.init();
			int count = 0;

			PlayWavData sound = new PlayWavData();
			sound.init(new ProcessContextImpl());

			Data item = sine.read();
			while (item != null && count++ < 100) {

				log.info("Sample: {}", item);
				sound.process(item);
				item = sine.read();
			}

			sine.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
