/**
 * 
 */
package stream.net;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.MJpegImageStream;
import stream.io.SourceURL;

/**
 * @author chris
 * @deprecated Use the new DataTap concept, which is part of streams-core in
 *             version 0.9.7-SNAPSHOT
 */
public class StreamServer {

	static Logger log = LoggerFactory.getLogger(StreamServer.class);
	ServerSocket server;
	final List<ClientHandler> clients = new ArrayList<ClientHandler>();
	final Thread inputDispatcher;
	int delay = 0;

	public StreamServer(int port, final InputStream input) throws Exception {
		server = new ServerSocket(port);

		delay = new Integer(System.getProperty("delay", "0"));

		inputDispatcher = new Thread() {
			Logger log = LoggerFactory.getLogger("InputDispatcherThread");

			public void run() {
				try {
					MJpegImageStream stream = new MJpegImageStream(input);
					int frame = 0;
					Data item = stream.readNext();
					while (item != null) {

						try {
							synchronized (clients) {
								if (clients.isEmpty()) {
									log.info("Dropping frame {}", frame);
								} else {
									for (ClientHandler handler : clients) {
										handler.add((byte[]) item.get("data"));
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						frame++;

						if (delay > 0)
							Thread.sleep(delay);

						item = stream.readNext();
					}

					stream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	public void run() {

		if (!inputDispatcher.isAlive()) {
			log.info("Starting input-dispatcher...");
			inputDispatcher.start();
		}

		try {
			while (true) {
				Socket socket = server.accept();
				log.info("new client connection: {}", socket);
				synchronized (clients) {
					ClientHandler handler = new ClientHandler(socket);
					handler.start();
					clients.add(handler);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class ClientHandler extends Thread {

		static Logger log = LoggerFactory.getLogger(ClientHandler.class);
		Socket socket;
		LinkedBlockingQueue<byte[]> chunks = new LinkedBlockingQueue<byte[]>();

		public ClientHandler(Socket sock) {
			this.socket = sock;
		}

		public void run() {

			while (socket.isConnected()) {

				try {
					byte[] chunk = chunks.take();
					if (chunk != null)
						socket.getOutputStream().write(chunk);
				} catch (SocketException se) {
					log.error("Socket error: {}", se.getMessage());
					log.info("Disconnecting client...");
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void add(byte[] chunk) {
			chunks.add(chunk);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			if (args.length == 0) {
				log.info("You need to specify the source-URL to stream from!");
				return;
			}

			SourceURL url = new SourceURL(args[0]);
			log.info("Reading MJpegStream from {}", url);
			int port = new Integer(System.getProperty("port", "9999"));
			log.info("Starting server on port {}", port);
			StreamServer server = new StreamServer(port, url.openStream());
			server.run();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
