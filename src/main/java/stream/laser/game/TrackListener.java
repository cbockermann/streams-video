/**
 * 
 */
package stream.laser.game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.LinkedBlockingQueue;

import net.minidev.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;
import stream.ProcessContext;

/**
 * @author chris
 * 
 */
public class TrackListener extends AbstractProcessor implements Runnable {

	static Logger log = LoggerFactory.getLogger(TrackListener.class);
	File file;
	PrintStream out;
	boolean running = false;
	LinkedBlockingQueue<Data> items = new LinkedBlockingQueue<Data>();
	PrintStream scores;

	public TrackListener() throws IOException {
		file = new File("/tmp/laser-game.dat");
		out = new PrintStream(new FileOutputStream(file, true));
	}

	/**
	 * @see stream.AbstractProcessor#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);

		File scoreFile = new File(System.getProperty("user.home")
				+ File.separator + "laser-scores.json");
		scores = new PrintStream(new FileOutputStream(scoreFile, true));

		Thread t = new Thread(this);
		t.start();
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		try {
			if (input == null) {
				return input;
			}

			items.add(input);
			return input;
		} catch (Exception e) {
			e.printStackTrace();
			return input;
		}
	}

	/**
	 * @see stream.AbstractProcessor#finish()
	 */
	@Override
	public void finish() throws Exception {
		super.finish();
		running = false;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		running = true;
		while (running) {

			while (!items.isEmpty()) {
				try {
					Data item = items.take();
					// log.debug("Writing item: {}", item);

					if (out == null
							|| "start".equalsIgnoreCase(item.get("track:point")
									+ "")) {
						if (out != null) {
							out.flush();
							out.close();
						}

						File file = new File(System.getProperty("user.home")
								+ File.separator + "trace-" + item.get("user")
								+ "-" + System.currentTimeMillis() + ".json");
						log.info("Creating new trace-file {}", file);
						out = new PrintStream(new FileOutputStream(file));
					}

					String json = JSONObject.toJSONString(item);
					if (out != null) {
						out.println(json);
					} else
						log.error("No output stream given to write traces to...");

					if (item.containsKey("time"))
						scores.println(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		try {
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
