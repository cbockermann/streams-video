/**
 * 
 */
package stream;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.io.LineStream;
import stream.io.SourceURL;

/**
 * @author chris
 * 
 */
public class StaticKrimp {

	static Logger log = LoggerFactory.getLogger(StaticKrimp.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		List<Data> transactions = new ArrayList<Data>();

		LineStream stream = new LineStream(new SourceURL(
				"http://download.jwall.org/streams/transactions.dat.gz"));
		stream.setLimit(100L);
		stream.init();

		SplitItems split = new SplitItems();

		Data item = stream.read();
		while (item != null) {

			item = split.process(item);
			item.remove("@stream");

			transactions.add(item);
			item = stream.read();
		}

		// 'transactions' enthaelt jetzt alle Transaktionen...
		// Mache "Krimp"...
		for (Data itemset : transactions) {
			log.info("itemset: {}", itemset.keySet());
		}
	}
}
