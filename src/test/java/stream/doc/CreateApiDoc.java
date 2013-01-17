/**
 * 
 */
package stream.doc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateApiDoc {

	static Logger log = LoggerFactory.getLogger(CreateApiDoc.class);
	static String[] packages = new String[] { "stream.video", "stream.image",
			"stream.io" };

	public static String[] readPackages() {
		try {

			String prop = stream.util.URLUtilities
					.readContent(CreateApiDoc.class
							.getResource("/doc.properties"));
			log.debug("Read properties:\n{}", prop);

			List<String> list = new ArrayList<String>();
			String[] str = prop.split("\\n");
			for (String s : str) {
				if (!s.trim().isEmpty()) {
					list.add(s.trim());
				}
			}
			return list.toArray(new String[list.size()]);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new String[0];
	}

	@Test
	public void test() {
		try {
			File output = new File("/tmp/streams-video-api");

			String[] packages = readPackages();

			log.info(
					"Output directory for generated API documentation is: '{}'",
					output);
			DocGenerator doc = new DocGenerator(output);
			doc.generateDocs(packages);

			log.debug("Creating API for packages: {}", packages);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
