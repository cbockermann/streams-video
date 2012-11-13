/**
 * 
 */
package stream.net;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
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

	public static String[] logSearchPath = new String[] { "" };

	final ServerSocket server;
	final List<ClientHandler> clients = new ArrayList<ClientHandler>();
	final Thread inputDispatcher;
	int delay = 0;
	int clientBuffer = 100;

	public StreamServer(int port, final InputStream input) throws Exception {
		server = new ServerSocket(port);

		delay = new Integer(System.getProperty("delay", "0"));

		inputDispatcher = new Thread() {
			Logger log = LoggerFactory.getLogger("InputDispatcherThread");

			public void run() {
				try {
					MJpegImageStream stream = new MJpegImageStream(input);
					stream.init();

					int frame = 0;
					Data item = stream.readNext();
					while (item != null) {

						try {
							synchronized (clients) {
								if (clients.isEmpty()) {
									log.info("Dropping frame {}", frame);
								} else {
									log.info("Sending frame {}", frame);
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
				final Socket socket = server.accept();
				log.info("new client connection: {}", socket);
				synchronized (clients) {
					final ClientHandler handler = new ClientHandler(socket,
							clientBuffer, this);
					handler.start();
					synchronized (clients) {
						clients.add(handler);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeClient(ClientHandler client) {
		synchronized (clients) {
			clients.remove(client);
		}
	}

	public final static class ClientHandler extends Thread {

		final static Logger log = LoggerFactory.getLogger(ClientHandler.class);
		final Socket socket;
		final LinkedBlockingQueue<byte[]> chunks = new LinkedBlockingQueue<byte[]>();
		final int clientBuffer;
		final StreamServer server;

		public ClientHandler(Socket sock, int clientBuffer, StreamServer server) {
			this.socket = sock;
			this.clientBuffer = clientBuffer;
			this.server = server;
		}

		public void run() {

			while (socket.isConnected()) {

				try {
					byte[] chunk = null;
					chunk = chunks.take();
					if (chunk != null)
						socket.getOutputStream().write(chunk);
				} catch (SocketException se) {
					log.error("Socket error: {}", se.getMessage());
					log.info("Disconnecting client...");
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			log.info("Client handler for {} exiting...", socket);
			chunks.clear();
		}

		public void add(byte[] chunk) {
			if (chunks.size() > clientBuffer) {
				log.debug("Client buffer of size {} exceeded, dropping chunk",
						clientBuffer);
				chunks.remove();
				chunks.add(chunk);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			setupLogging();
			List<String> params = stream.run.handleArguments(args);

			if (params.isEmpty()) {
				log.info("You need to specify the source-URL to stream from!");
				return;
			}

			SourceURL url = new SourceURL(params.get(params.size() - 1));
			log.info("Reading MJpegStream from {}", url);
			int port = new Integer(System.getProperty("port", "9100"));
			log.info("Starting server on port {}", port);
			StreamServer server = new StreamServer(port, url.openStream());
			server.run();

		} catch (Exception e) {
			log.error("Error while running StreamServer: {}", e.getMessage());
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
	}

	public static void setupLogging() {

		List<String> searchPaths = new ArrayList<String>();
		if (System.getenv("STREAMS_HOME") != null)
			searchPaths.add(System.getenv("STREAMS_HOME") + File.separator
					+ "conf");

		for (String path : logSearchPath)
			searchPaths.add(path);

		for (String path : searchPaths) {
			String p = path;
			if (!p.isEmpty())
				p = path + File.separator + "log4j.properties";
			else
				p = "log4j.properties";

			File logProp = new File(p);
			if (logProp.canRead()) {
				System.err.println("Using log settings from "
						+ logProp.getAbsolutePath());
				try {
					Class<?> configurator = Class
							.forName("org.apache.log4j.PropertyConfigurator");
					Method configure = configurator.getMethod("configure",
							String.class);
					configure.invoke(null, logProp.getAbsolutePath());
					break;
				} catch (Exception e) {
					System.err
							.println("Failed to setup logging with log4j.properties: "
									+ e.getMessage());
				}
			}
		}
	}
}
