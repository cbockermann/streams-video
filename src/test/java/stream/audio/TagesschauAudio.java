/**
 * 
 */
package stream.audio;

import java.net.URL;

/**
 * @author chris
 * 
 */
public class TagesschauAudio {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			URL url = TagesschauAudio.class
					.getResource("/tagesschau-audio.xml");
			stream.run.main(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
