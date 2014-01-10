/**
 * 
 */
package stream.io;

import java.io.File;

/**
 * @author chris
 * 
 */
public class Ffmpeg {

	File ffmpegBinary;
	final static String[] paths = new String[] { "/usr/bin", "/usr/local/bin" };
	static String binary = null;

	public static String getBinary() throws Exception {

		if (binary == null) {
			for (String path : paths) {
				File f = new File(path + File.separator + "ffmpeg");
				if (f.canExecute()) {
					binary = f.getCanonicalPath();
				}
			}
		}

		if (binary == null) {
			throw new Exception("Failed to locate 'ffmpeg' binary!");
		}

		return binary;
	}
}
