/**
 * 
 */
package stream.io;

import java.io.InputStream;

import stream.Data;
import stream.data.DataFactory;

/**
 * @author chris
 *
 */
public class RawJpegStream extends AbstractStream {

    SourceURL url;
    JpegStream stream;

    public RawJpegStream(SourceURL url) {
        super(url);
        this.url = url;
    }

    /**
     * @see stream.io.AbstractStream#init()
     */
    @Override
    public void init() throws Exception {
        super.init();

        stream = new JpegStream(url.openStream(), 1024 * 1024, 16 * 1024 * 1024, true);
    }

    /**
     * @see stream.io.AbstractStream#readNext()
     */
    @Override
    public Data readNext() throws Exception {

        byte[] chunk = stream.readNextChunk();
        if (chunk == null) {
            return null;
        }

        Data item = DataFactory.create();
        item.put("raw:data", chunk);
        return item;
    }

    public static void main(String[] args) throws Exception {

        SourceURL src = new SourceURL("tcp://192.168.30.231:9000");
        if (args.length > 0) {
            src = new SourceURL(args[0]);
        }

        InputStream in = src.openStream();
        JpegStream stream = new JpegStream(in, 10124 * 1024, 16 * 1024 * 1024, true);

        // stream.init();
        byte[] item = stream.readNextChunk();
        while (item != null) {
            System.out.println(item);
            item = stream.readNextChunk();
        }
    }

}
