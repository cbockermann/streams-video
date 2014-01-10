/**
 * 
 */
package stream.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.data.DataFactory;

/**
 * <p>
 * This class implements a simple stream of packets obtained from a transport
 * stream. Each packet has a size of 188 bytes. The data items emitted by this
 * stream contain the packet data (in <code>packet:data</code>) and the packet
 * identifier (in <code>packet:id</code>).
 * </p>
 * <p>
 * Additional attributes provided in each item are
 * <ul>
 * <li><code>packet:offset</code> - the number of bytes this packet starts from
 * the start of the stream</li>
 * <li><code>packet:sequence</code> - the sequence number of this packet from
 * the stream (first packet is '0')
 * </ul>
 * </p>
 * 
 * @author Christian Bockermann
 * 
 */
public class TransportStream extends AbstractStream {

	static Logger log = LoggerFactory.getLogger(TransportStream.class);
	InputStream stream;
	Long sequenceId = 0L;
	long offset = 0L;
	Thread prefetcher;
	final LinkedBlockingQueue<Data> queue = new LinkedBlockingQueue<Data>(10000);
	AtomicBoolean closed = new AtomicBoolean(false);

	final static byte PAYLOAD_BIT = 1 << 4;
	final static byte ADAPTION_BIT = 1 << 5;
	final static int SCRAMBLE_BIT = 1 << 7;

	final static int ADAPTION_DISCONTINUITY = 1 << 7;
	final static byte ADAPTION_RANDOM_ACCESS = 1 << 6;
	final static byte ADAPTION_ELEMENTARY_STREAM_PRIO = 1 << 5;
	final static byte ADAPTION_PCR = 1 << 4;
	final static byte ADAPTION_OPCR = 1 << 3;

	public TransportStream(SourceURL url) {
		super(url);
	}

	/**
	 * @see stream.io.AbstractStream#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();
		stream = this.getInputStream();

		prefetcher = new Thread() {
			public void run() {
				while (!closed.get()) {

					try {
						byte[] packet = new byte[188];

						int read = 0;
						while (!closed.get() && read < 188) {
							int bytes = stream.read(packet, read, packet.length
									- read);
							log.debug("Read {} bytes from stream...", bytes);
							if (bytes > 0) {
								read += bytes;
								offset += bytes;
							}
						}

						byte mask = 0xff >> 3;
						int pid = ((mask & packet[1]) << 8) + packet[2];

						// log.info("PID is: 0x{}", Integer.toHexString(pid));
						if (pid == 0) {
							log.debug("packet {} has PID 0, offset is {}",
									sequenceId, offset);
							log.debug("   total of {}k read", offset / 1024);
						}

						byte flags = packet[3];
						// log.info("Flags:              'SCAP____'");
						// log.info(
						// "Flags:              '{}'",
						// String.format("%8s",
						// Integer.toBinaryString(flags)));

						// log.info(
						// "scramble vector is  '{}'",
						// String.format("%8s",
						// Integer.toBinaryString(SCRAMBLE_BIT)));

						if ((flags & SCRAMBLE_BIT) > 0) {
							log.info("scrambling found!");
						}

						// log.info(
						// "adaption vector is  '{}'",
						// String.format("%8s",
						// Integer.toBinaryString(ADAPTION_BIT)));
						if ((flags & ADAPTION_BIT) > 0) {
							// log.info("Adaption bit set!");

							int adaptionSize = Byte.valueOf(packet[4])
									.intValue();
							// short adaptionSize = (short) packet[4];
							// log.info("Adaption header has {} bytes",
							// adaptionSize);
							if (adaptionSize > 0) {
								// byte flag = packet[5];
								//
								// if ((flag & ADAPTION_PCR) > 0) {
								// log.info("Found PCR !");
								// long pcr_base = ((((int) packet[6]) << 24)
								// + (packet[7] << 16)
								// + (packet[8] << 8) + packet[9]) << 1;
								//
								// long rest = (packet[10] << 8) + packet[11];
								// if ((rest & 0x8000) > 0) {
								// pcr_base++;
								// }
								// log.info("pcr_base = {}", pcr_base);
								// long pcr_ref_ext = (rest & 0x1FF);
								// log.info("pcr_ref_ext = {}", pcr_ref_ext);
								// }
								//
								// if ((flag & ADAPTION_OPCR) > 0) {
								// log.info("Found OPCR !");
								// }
							}
						}

						// if ((flags & PAYLOAD_BIT) > 0) {
						// log.debug("payload bit set!");
						// }

						// int count = (int) (flags & 15);
						// log.info("Counter is: {}", count);

						// if (pid == 0) {
						// log.info("Found Program Association Table (PAT)!");
						//
						// int table_id = packet[5];
						// log.info("table id is: {}",
						// Integer.toHexString(table_id));
						// }

						// log.info("count   vector is   '{}'", String.format(
						// "%8s", Integer.toBinaryString(15)));

						Data item = DataFactory.create();
						item.put("packet:data", packet);
						item.put("packet:pid", pid);
						item.put("packet:offset", offset - 188);
						item.put("packet:sequence", sequenceId++);
						while (queue.remainingCapacity() < 1) {
							log.info("Waiting for queue to drain a bit...");
							Thread.sleep(100);
						}
						queue.add(item);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				log.debug("Prefetcher thread finished.");
			}
		};
		prefetcher.setDaemon(true);
		prefetcher.start();
	}

	/**
	 * @see stream.io.AbstractStream#close()
	 */
	@Override
	public void close() throws Exception {
		log.info("Closing TransportStream...");
		super.close();
		closed.set(true);

		while (prefetcher.isAlive()) {
			log.info("Waiting for pre-fetcher thread to finish...");
			try {
				prefetcher.interrupt();
				prefetcher.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see stream.io.AbstractStream#readNext()
	 */
	@Override
	public Data readNext() throws Exception {

		try {
			Data item = queue.take();
			return item;
		} catch (Exception e) {
			throw e;
		}
	}

	public static void main(String[] args) throws Exception {
		TransportStream stream = new TransportStream(new SourceURL(
				"http://cb00.virtual/rtl.ts"));
		stream.setLimit(100L);
		stream.init();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		Data item = stream.read();
		int items = 0;
		long bytesWritten = 0L;

		while (item != null) {
			// log.info("item: {}", item);

			int pid = (Integer) item.get("packet:pid");
			if (pid == 0) {

				if (bytesWritten > 0) {
					log.info("Closing old block...");
					baos.close();

					File file = new File("/Volumes/RamDisk/video-block-"
							+ System.currentTimeMillis() + ".ts");
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(baos.toByteArray());
					fos.close();
					log.info("Write TS chunk of size {} to {}", bytesWritten,
							file);
					baos = new ByteArrayOutputStream();
					bytesWritten = 0;
				}
			}

			if (baos != null) {
				byte[] data = (byte[]) item.get("packet:data");
				baos.write(data);
				bytesWritten += data.length;
			}

			items++;
			item = stream.read();
		}

		if (baos != null) {
			baos.close();
		}

		log.info("{} items read.", items);
		stream.close();
	}
}