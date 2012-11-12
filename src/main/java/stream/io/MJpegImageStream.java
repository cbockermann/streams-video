/**
 * 
 */
package stream.io;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author chris
 * 
 */
public class MJpegImageStream extends ByteChunkStream {

	public MJpegImageStream(SourceURL url) throws Exception {
		super(openUrl(url), ByteChunkStream.JPG_SIGNATURE);
	}

	public MJpegImageStream(InputStream in) throws Exception {
		super(in, ByteChunkStream.JPG_SIGNATURE);
	}

	public final static InputStream openUrl(SourceURL url) throws Exception {
		if (url.toString().toLowerCase().endsWith(".gz")) {
			return new GZIPInputStream(url.openStream());
		} else
			return url.openStream();
	}
}