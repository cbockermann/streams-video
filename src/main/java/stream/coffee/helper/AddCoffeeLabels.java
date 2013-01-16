package stream.coffee.helper;

import java.util.HashMap;
import java.util.Map;

import stream.AbstractProcessor;
import stream.Data;
import stream.ProcessContext;
import stream.io.CsvStream;
import stream.io.SourceURL;

/**
 * This processor labels coffee data by reading the true labels from a given .csv-file.
 * @author Matthias
 *
 */
public class AddCoffeeLabels extends AbstractProcessor {
	
	String file = "file:///C:/Users/Matthias/Documents/SchulteSVN/Diplomarbeit/data/kapseln/kapseln.csv";

	/**
	 * Sets the file the labels are stored in.
	 * @param file
	 */
	public void setFile(String file) {
		this.file = file;
	}
	
	/**
	 * Delivers the file the labels are actually read from.
	 * @return
	 */
	public String getFile() {
		return file;
	}
	
	private Map<Long, String> labels = new HashMap<Long, String>();
	
	@Override
	public void init(ProcessContext ctx) throws Exception {
		
		CsvStream stream = new CsvStream(new SourceURL(file));
		
		stream.init();

		Data item = stream.read();

		while (item != null) {
			String frame = (String) item.get("frame");
			String label = (String) item.get("label");

			for (int i=0; i < 6; i++) {
				labels.put(Long.parseLong(frame)+i, label);
			}

			item = stream.read();
		}

		stream.close();

		super.init(ctx);
		
		System.out.println("Initalization of AddCoffeeLabels completed. " + labels.size() + " elements found.");
	}
	
	@Override
	public Data process(Data input) {
		Long frame = (Long) input.get("frame:id");
		
		if (labels.containsKey(frame)) {
			input.put("@label:event", labels.get(frame));
		} else {
			input.put("@label:event", "no_event");
		}
		
		return input;
	}

}
