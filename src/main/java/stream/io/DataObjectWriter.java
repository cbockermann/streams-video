/**
 * 
 */
package stream.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import stream.AbstractProcessor;
import stream.Data;
import stream.ProcessContext;

/**
 * @author chris
 * 
 */
public class DataObjectWriter extends AbstractProcessor {

	File file;
	ObjectOutputStream out;

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @see stream.AbstractProcessor#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
		if (file.getAbsolutePath().endsWith(".gz")) {
			out = new ObjectOutputStream(new GZIPOutputStream(
					new FileOutputStream(file, true)));
		} else
			out = new ObjectOutputStream(new FileOutputStream(file, true));
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		if (input != null) {
			try {
				out.writeObject(input);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return input;
	}

	/**
	 * @see stream.AbstractProcessor#finish()
	 */
	@Override
	public void finish() throws Exception {
		super.finish();
		out.flush();
		out.close();
	}
}