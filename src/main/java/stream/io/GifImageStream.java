/**
 * 
 */
package stream.io;

import java.io.InputStream;
import java.net.URL;

/**
 * @author chris
 * 
 */
public class GifImageStream extends AbstractImageStream {

	public GifImageStream(URL url) throws Exception {
		super(url, AbstractImageStream.GIF_SIGNATURE);
	}

	public GifImageStream(InputStream in) throws Exception {
		super(in, AbstractImageStream.GIF_SIGNATURE);
	}
}