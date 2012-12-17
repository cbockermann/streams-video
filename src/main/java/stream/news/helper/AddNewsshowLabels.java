package stream.news.helper;

import java.util.HashMap;
import java.util.Map;

import stream.AbstractProcessor;
import stream.Data;
import stream.ProcessContext;
import stream.io.CsvStream;
import stream.io.SourceURL;

public class AddNewsshowLabels extends AbstractProcessor {

	String file = "file:///C:/Users/Matthias/Documents/SchulteSVN/Diplomarbeit/data/news20120911/transitions.csv";

	private Map<Long, String> labels = new HashMap<Long, String>();
	private String lastlabel = "intro";

	@Override
	public void init(ProcessContext ctx) throws Exception {

		//file = "file:/Volumes/RamDisk/transitions.csv";

//		CsvStream stream = new CsvStream(new SourceURL(file));
		CsvStream stream = new CsvStream(new SourceURL("classpath:/transitions.csv"));
		stream.init();

		Data item = stream.read();

		while (item != null) {
			String frame = (String) item.get("frame");
			String transition = (String) item.get("transition");
			String label = (String) item.get("label");

			
			if (frame != null && transition != null) {
					labels.put(Long.parseLong(frame)-2, label);
			}

			item = stream.read();
		}

		stream.close();

		super.init(ctx);
		
		System.out.println("Initalization of AddNewsshowLabels completed. labels contains " + labels.size() + " elements.");
	}

	@Override
	public Data process(Data input) {
		
		Long frame = (Long) input.get("frame:id");
		
		if (labels.containsKey(frame)) {
			input.put("@label:shotboundary", true);
			if (labels.get(frame) != null) {
				lastlabel = labels.get(frame);
			}
		} else {
			input.put("@label:shotboundary", false);
		}

		input.put("ShotType", lastlabel);

		return input;
	}

}
