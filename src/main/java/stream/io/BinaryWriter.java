/**
 * 
 */
package stream.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;
import stream.annotations.Parameter;
import stream.expressions.ExpressionResolver;

/**
 * @author chris
 * 
 */
public class BinaryWriter extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(BinaryWriter.class);
	String url = null;
	String currentUrl;

	String key;
	OutputStream output;

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	@Parameter(description = "The URL to write to (file://...).", required = true)
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	@Parameter(description = "The key of the attribute containing the byte array to write.", required = true)
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		try {

			Serializable val = input.get(key);
			if (val == null) {
				return input;
			}

			byte[] data = null;

			if (!val.getClass().isArray()
					&& val.getClass().getComponentType() == byte.class) {
				log.error(
						"Value {} for key {} is not of required type 'byte[]'",
						val, key);
				return input;
			} else {
				data = (byte[]) val;
			}

			String newUrl = ExpressionResolver.expand(url, context, input);
			if (newUrl.equals(currentUrl)) {
				if (output == null) {
					output = createOutputStream(newUrl);
					currentUrl = newUrl;
				}
			} else {
				if (output != null) {
					output.close();
				}

				output = createOutputStream(newUrl);
				currentUrl = newUrl;
			}
			log.debug("Writing {} bytes to {}", data.length, currentUrl);
			output.write(data);

		} catch (Exception e) {
			log.error("Failed to write data: {}", e.getMessage());
		}
		return input;
	}

	private OutputStream createOutputStream(String theUrl) throws Exception {
		File file = new File(theUrl);
		if (theUrl.startsWith("file:")) {
			file = new File(theUrl.substring(5));
		}
		log.debug("Creating new outputstream to file '{}'", file);

		if (file.isDirectory())
			throw new Exception("Cannot write to directory '" + theUrl + "'!");

		if (!file.getParentFile().isDirectory()) {
			file.getParentFile().mkdirs();
		}

		return new FileOutputStream(file);
	}
}