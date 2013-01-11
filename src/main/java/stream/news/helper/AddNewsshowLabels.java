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
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public String getFile() {
		return file;
	}

	private Map<Long, String> labels = new HashMap<Long, String>();
	private String lastlabel = "intro";

	@Override
	public void init(ProcessContext ctx) throws Exception {
		
		Integer counterCuts = 0;
		Integer counterGT = 0;

		CsvStream stream = new CsvStream(new SourceURL(file));
		
		stream.init();
		
		labels.put(1L, "Intro");

		Data item = stream.read();

		while (item != null) {
			String frame = (String) item.get("frame");
			String transition = (String) item.get("transition");
			String label = (String) item.get("label");

			
			if (frame != null && transition.equals("C")) {
					labels.put(Long.parseLong(frame), label);
					counterCuts++;
			} else {
				if (frame != null && transition.equals("GT")) {
					labels.put(Long.parseLong(frame), label);
					counterGT++;
				}
			}

			item = stream.read();
		}

		stream.close();

		super.init(ctx);
		
		System.out.println("Initalization of AddNewsshowLabels completed. " + labels.size() + " elements found (" + counterCuts + " Cuts and " + counterGT + " Gradual Transitions)");
	}

	@Override
	public Data process(Data input) {
		
		Long frame = (Long) input.get("frame:id");
		
		if (labels.containsKey(frame)) {
			input.put("@label:shotboundary", true);
			if (labels.get(frame) != null) {
				lastlabel = labels.get(frame);
			} else {
				lastlabel="not definined";
			}
		} else {
			input.put("@label:shotboundary", false);
		}

		if (lastlabel.equalsIgnoreCase("AS")) {
			input.put("@label:anchorshot", true);
		} else {
			input.put("@label:anchorshot", false);
		}
		
		System.out.println(lastlabel + "  " + lastlabel.equalsIgnoreCase("AS"));
		input.put("@label:shottype", lastlabel);
		
		
		return input;
	}

}
