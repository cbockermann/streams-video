package stream.io;

// Wav file IO class
// A.Greensted
// http://www.labbookpages.co.uk

// File format is based on the information from
// http://www.sonicspot.com/guide/wavefiles.html
// http://www.blitter.com/~russtopia/MIDI/~jglatt/tech/wave.htm

// Version 1.0

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.annotations.Parameter;
import stream.audio.PlayWavData;
import stream.data.DataFactory;
import stream.runtime.ProcessContextImpl;

public class WavStream extends AbstractStream {

	static Logger log = LoggerFactory.getLogger(WavStream.class);

	private final static int BUFFER_SIZE = 4096;

	private final static int FMT_CHUNK_ID = 0x20746D66;
	private final static int DATA_CHUNK_ID = 0x61746164;
	private final static int RIFF_CHUNK_ID = 0x46464952;
	private final static int RIFF_TYPE_ID = 0x45564157;

	private int bytesPerSample; // Number of bytes required to store a single
								// sample

	// Wav Header
	private int numChannels; // 2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
	private Long sampleRate; // 4 bytes unsigned, 0x00000001 (1) to 0xFFFFFFFF
								// (4,294,967,295)
								// Although a java int is 4 bytes, it is signed,
								// so need to use a long
	private int blockAlign; // 2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
	private int validBits; // 2 bytes unsigned, 0x0002 (2) to 0xFFFF (65,535)

	// Buffering
	private byte[] buffer; // Local buffer used for IO
	private int bufferPointer; // Points to the current position in local buffer
	private int bytesRead; // Bytes read after last read into local buffer
	// private Long frameCounter; // Current number of frames read or written

	// Cannot instantiate WavFile directly, must either use newWavFile() or
	// openWavFile()

	final SourceURL source;
	InputStream in;
	protected int blockSize = 48000;
	protected Long blocksRead = 0L;
	final Data header = DataFactory.create();
	boolean eos = false;

	public WavStream(SourceURL source) {
		super(source);
		this.source = source;
		buffer = new byte[BUFFER_SIZE];
	}

	/**
	 * @return the bytesPerSample
	 */
	public int getBytesPerSample() {
		return bytesPerSample;
	}

	/**
	 * @return the sampleRate
	 */
	public Long getSampleRate() {
		return sampleRate;
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
	@Parameter(description = "The number of samples (double values) read from the stream for each data item.")
	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public void init() throws Exception {

		this.in = source.openStream();

		// Read the first 12 bytes of the file
		int bytesRead = in.read(buffer, 0, 12);
		if (bytesRead != 12)
			throw new WavFileException("Not enough wav file bytes for header");

		// Extract parts from the header
		long riffChunkID = getLE(buffer, 0, 4);
		long chunkSize = getLE(buffer, 4, 4);
		long riffTypeID = getLE(buffer, 8, 4);

		// Check the header bytes contains the correct signature
		if (riffChunkID != RIFF_CHUNK_ID)
			throw new WavFileException(
					"Invalid Wav Header data, incorrect riff chunk ID");
		if (riffTypeID != RIFF_TYPE_ID)
			throw new WavFileException(
					"Invalid Wav Header data, incorrect riff type ID");

		boolean foundFormat = false;
		boolean foundData = false;

		// Search for the Format and Data Chunks
		while (true) {
			// Read the first 8 bytes of the chunk (ID and chunk size)
			bytesRead = in.read(buffer, 0, 8);
			if (bytesRead == -1)
				throw new WavFileException(
						"Reached end of file without finding format chunk");
			if (bytesRead != 8)
				throw new WavFileException("Could not read chunk header");

			// Extract the chunk ID and Size
			long chunkID = getLE(buffer, 0, 4);
			chunkSize = getLE(buffer, 4, 4);

			// Word align the chunk size
			// chunkSize specifies the number of bytes holding data. However,
			// the data should be word aligned (2 bytes) so we need to calculate
			// the actual number of bytes in the chunk
			long numChunkBytes = (chunkSize % 2 == 1) ? chunkSize + 1
					: chunkSize;

			if (chunkID == FMT_CHUNK_ID) {
				// Flag that the format chunk has been found
				foundFormat = true;

				// Read in the header info
				bytesRead = in.read(buffer, 0, 16);

				// Check this is uncompressed data
				int compressionCode = (int) getLE(buffer, 0, 2);
				if (compressionCode != 1)
					throw new WavFileException("Compression Code "
							+ compressionCode + " not supported");

				// Extract the format information
				numChannels = (int) getLE(buffer, 2, 2);
				sampleRate = getLE(buffer, 4, 4);
				blockAlign = (int) getLE(buffer, 12, 2);
				validBits = (int) getLE(buffer, 14, 2);

				if (numChannels == 0)
					throw new WavFileException(
							"Number of channels specified in header is equal to zero");
				if (blockAlign == 0)
					throw new WavFileException(
							"Block Align specified in header is equal to zero");
				if (validBits < 2)
					throw new WavFileException(
							"Valid Bits specified in header is less than 2");
				if (validBits > 64)
					throw new WavFileException(
							"Valid Bits specified in header is greater than 64, this is greater than a long can hold");

				// Calculate the number of bytes required to hold 1 sample
				bytesPerSample = (validBits + 7) / 8;
				if (bytesPerSample * numChannels != blockAlign)
					throw new WavFileException(
							"Block Align does not agree with bytes required for validBits and number of channels");

				header.put("wav:samplerate", sampleRate);
				header.put("wav:channels", numChannels);
				header.put("wav:bytesPerSample", bytesPerSample);
				// Account for number of format bytes and then skip over
				// any extra format bytes
				numChunkBytes -= 16;
				if (numChunkBytes > 0)
					in.skip(numChunkBytes);
			} else if (chunkID == DATA_CHUNK_ID) {
				// Check if we've found the format chunk,
				// If not, throw an exception as we need the format information
				// before we can read the data chunk
				if (foundFormat == false)
					throw new WavFileException(
							"Data chunk found before Format chunk");

				// Check that the chunkSize (wav data length) is a multiple of
				// the
				// block align (bytes per frame)
				if (chunkSize % blockAlign != 0)
					throw new WavFileException(
							"Data Chunk size is not multiple of Block Align");

				// Calculate the number of frames
				// numFrames = chunkSize / blockAlign;

				// Flag that we've found the wave data chunk
				foundData = true;

				break;
			} else {
				// If an unknown chunk ID is found, just skip over the chunk
				// data
				in.skip(numChunkBytes);
			}
		}

		// Throw an exception if no data chunk has been found
		if (foundData == false)
			throw new WavFileException("Did not find a data chunk");

		bufferPointer = 0;
		bytesRead = 0;
		// frameCounter = 0L;

		log.info("Sample rate is: {}", this.sampleRate);
		log.info("  chunk size is: {}", chunkSize);
		this.blockSize = (new Long(chunkSize)).intValue();
		log.info("  each sample is for {} seconds",
				1.0d / sampleRate.doubleValue());
		log.info("  stream block size is: {} ({} seconds for each block)",
				this.blockSize, blockSize / sampleRate.doubleValue());

	}

	// Get and Put little endian data from local buffer
	// ------------------------------------------------
	private static long getLE(byte[] buffer, int pos, int numBytes) {
		numBytes--;
		pos += numBytes;

		long val = buffer[pos] & 0xFF;
		for (int b = 0; b < numBytes; b++)
			val = (val << 8) + (buffer[--pos] & 0xFF);

		return val;
	}

	public double readSample() throws IOException, WavFileException {
		double val = 0;

		for (int b = 0; b < bytesPerSample; b++) {
			if (bufferPointer == bytesRead) {
				int read = in.read(buffer, 0, BUFFER_SIZE);
				if (read < 0) {
					eos = true;
					return Double.NaN;
				}
				bytesRead = read;
				bufferPointer = 0;
			}

			int v = buffer[bufferPointer];
			if (b < bytesPerSample - 1 || bytesPerSample == 1)
				v &= 0xFF;
			val += v << (b * 8);

			bufferPointer++;
		}

		return val;
	}

	public Data readNext() throws Exception {

		if (eos)
			return null;

		Data item = DataFactory.create();

		if (blocksRead == 0)
			item.putAll(header);

		double min = 255.0d;
		double max = 0.0d;
		double avg = 0.0d;
		double[] block = new double[blockSize];
		int last = 0;
		int read = 0;
		for (int i = 0; i < block.length; i++) {
			double sample = readSample();
			if (sample == Double.NaN) {
				eos = true;
				log.info("EOS! Last successful block was {}", i);
				break;
			} else {
				block[i] = sample;
				last++;
				read++;
			}
			min = Math.min(min, block[i]);
			max = Math.max(max, block[i]);
			avg += block[i];
		}

		log.info("{} samples successfully read", read);

		if (last < block.length) {
			log.info("Shrinking block to {} samples", last);
			double[] nb = new double[last];
			for (int i = 0; i < last; i++) {
				nb[i] = block[i];
			}
			block = nb;
		}

		avg = avg / block.length;

		double var = 0.0d;

		for (int i = 0; i < block.length; i++) {
			var += ((avg - block[i]) * (avg - block[i]));
		}
		var = Math.sqrt(var) / block.length;

		blocksRead++;

		double rate = 1.0d / sampleRate.doubleValue();

		item.put("wav:position", blocksRead.doubleValue() * blockSize * rate);
		item.put("wav:samples", block);
		item.put("wav:blocklength", block.length);
		item.put("wav:max", max);
		item.put("wav:min", min);
		item.put("wav:variance", var);
		item.put("wav:avg", avg);

		return item;
	}

	public void close() throws Exception {
		// Close the input stream and set to null
		if (in != null) {
			in.close();
			in = null;
		}
	}

	public static void main(String[] args) {

		try {
			int count = 0;
			int limit = 100000;

			SourceURL url = new SourceURL("file:/Users/chris/tagesschau.wav.gz");
			url = new SourceURL("classpath:/tagesschau.wav.gz");
			WavStream stream = new WavStream(url);
			stream.setBlockSize(1200);
			stream.init();

			PlayWavData sound = new PlayWavData();
			sound.setVolume(0.25);
			sound.init(new ProcessContextImpl());
			Data item = stream.read();
			while (item != null && count < limit) {
				log.debug("sample: {}", item);

				sound.process(item);
				item = stream.read();
				count++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
