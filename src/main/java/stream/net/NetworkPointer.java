/**
 * 
 */
package stream.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class NetworkPointer extends Thread {

	static Logger log = LoggerFactory.getLogger(NetworkPointer.class);
	DatagramSocket socket;
	final List<PointerListener> listener = new ArrayList<PointerListener>();

	public NetworkPointer(int port) throws Exception {
		// socket = new DatagramSocket();
		socket = new DatagramSocket(
				new InetSocketAddress("192.168.128.4", port));
		log.info("socket bound to {}", socket.getLocalSocketAddress());
		log.info("NetworkPointer listening on UDP port {}", port);
	}

	public void addListener(PointerListener l) {
		listener.add(l);
	}

	public void run() {
		Pattern pattern = Pattern.compile("^\\((\\d+),(\\d+)\\)$");

		while (socket.isBound()) {
			try {
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);

				// log.info("socket is bound to {}", socket.getLocalAddress());
				// log.info("Waiting for packet to arrive..");
				socket.receive(packet);
				String input = new String(packet.getData());
				// log.info("received packet: '{}'", input);
				input = input.trim();
				Matcher m = pattern.matcher(input);
				if (m.find()) {
					String x = m.group(1);
					String y = m.group(2);
					// log.info("Received point-2d, x:{}, y:{}", x, y);

					try {
						int px = new Integer(x);
						int py = new Integer(y);
						for (PointerListener l : listener) {
							l.pointingAt(px, py);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {

		try {

			NetworkPointer pointer = new NetworkPointer(9100);

			// Calibration cal = new Calibration(1024, 768, null);
			// pointer.addListener(cal);
			pointer.addListener(new PointerListener() {
				@Override
				public void pointingAt(int x, int y) {
					log.info("Pointer pointing at {},{}", x, y);
				}
			});

			pointer.run();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
