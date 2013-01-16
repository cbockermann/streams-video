/**
 * 
 */
package stream.net;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chris
 * 
 */
public class InfiniteLoop {

	static AtomicBoolean stop = new AtomicBoolean(false);

	static class KeyCheck extends Thread {

		public void run() {

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(System.in));

				while (!stop.get()) {

					String line = reader.readLine();
					if (line == null)
						continue;

					if ("stop".startsWith(line.trim())) {
						stop.set(true);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		KeyCheck keyCheck = new KeyCheck();
		keyCheck.setDaemon(true);
		keyCheck.start();

		Console console = System.console();
		System.out.println("console: " + console);
		// Reader reader = System.console().reader();

		for (int i = 0; !stop.get(); i++) {
			System.out.println("Iteration " + i);
			Thread.sleep(500);
		}
	}
}
