/**
 * 
 */
package stream.io;

import java.io.InputStream;

/**
 * @author chris
 * 
 */
public class GifImageStream extends ByteChunkStream {

	public GifImageStream(SourceURL url) throws Exception {
		super(url, ByteChunkStream.GIF_SIGNATURE);
	}

	public GifImageStream(InputStream in) throws Exception {
		super(in, ByteChunkStream.GIF_SIGNATURE);
	}
}