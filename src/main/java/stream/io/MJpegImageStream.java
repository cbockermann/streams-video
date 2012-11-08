/**
 * 
 */
package stream.io;

import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * @author chris
 * 
 */
public class MJpegImageStream extends AbstractImageStream {

	public MJpegImageStream(URL url) throws Exception {
		super(openUrl(url), AbstractImageStream.JPG_SIGNATURE);
	}

	public MJpegImageStream(InputStream in) throws Exception {
		super(in, AbstractImageStream.JPG_SIGNATURE);
	}

	public final static InputStream openUrl(URL url) throws Exception {
		if (url.toString().toLowerCase().endsWith(".gz")) {
			return new GZIPInputStream(url.openStream());
		} else
			return url.openStream();
	}
}