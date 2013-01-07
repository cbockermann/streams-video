/**
 * 
 */
package stream.audio;

import java.net.URL;

/**
 * @author chris
 * 
 */
public class TagesschauVideo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			URL url = TagesschauVideo.class
					.getResource("/tagesschau-video.xml");
			stream.run.main(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
