/**
 * 
 */
package stream.laser;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import stream.AbstractProcessor;
import stream.Data;
import stream.ProcessContext;
import stream.expressions.ExpressionResolver;

/**
 * @author chris
 * 
 */
public class SendUDP extends AbstractProcessor {

	Integer port;
	String address;
	String message;
	DatagramSocket socket;
	InetAddress addr;

	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @see stream.AbstractProcessor#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
		socket = new DatagramSocket();

		if (port == null || port < 1024) {
			throw new Exception("Invalid port, needs to be > 1024!");
		}

		addr = InetAddress.getByName(address);
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		try {
			String msg = ExpressionResolver.expand(message, context, input)
					+ "\n";
			// System.out.println("Sending: '" + msg + "'");

			byte[] buf = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, addr,
					port);
			socket.send(packet);
			// System.out.println("UDP sent.");

		} catch (Exception e) {
			// e.printStackTrace();
		}
		return input;
	}
}
