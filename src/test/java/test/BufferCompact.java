/**
 * 
 */
package test;

import java.nio.CharBuffer;

/**
 * @author chris
 * 
 */
public class BufferCompact {
	public static void main(String[] args) throws Exception {
		CharBuffer buffer = CharBuffer.allocate(64);
		buffer.put("Let's write some Java code! ");

		System.out.println("Position: " + buffer.position());
		System.out.println("Limit   : " + buffer.limit());

		//
		// Read 10 chars from the buffer.
		//
		buffer.flip();
		for (int i = 0; i < 10; i++) {
			System.out.print(buffer.get());
		}
		System.out.println("");

		System.out.println("Position: " + buffer.position());
		System.out.println("Limit   : " + buffer.limit());

		//
		// clear the buffer using compact() method.
		//
		buffer.compact();
		System.out.println("Position: " + buffer.position());
		System.out.println("Limit   : " + buffer.limit());

		//
		// Write and read some more data.
		//
		buffer.put("Add some more data.");

		buffer.flip();
		while (buffer.hasRemaining()) {
			System.out.print(buffer.get());
		}
	}
}