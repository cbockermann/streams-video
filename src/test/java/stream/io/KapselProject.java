/**
 * 
 */
package stream.io;

import java.net.URL;

/**
 * @author chris
 * 
 */
public class KapselProject {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			URL url = KapselProject.class
					.getResource("/kapseln.xml");
			stream.run.main(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
