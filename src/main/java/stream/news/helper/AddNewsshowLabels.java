package stream.news.helper;

import java.util.HashMap;
import java.util.Map;

import stream.AbstractProcessor;
import stream.Data;
import stream.ProcessContext;
import stream.io.CsvStream;
import stream.io.SourceURL;

public class AddNewsshowLabels extends AbstractProcessor {
	
	String file = "file:///C:/Users/Mattis/workspace/schulte/Diplomarbeit/data/news20120911/transitions.csv";
	
	private Map<Integer,String> labels = new HashMap<Integer, String>();
	private String lastlabel = "intro";
	
	@Override
	public void init(ProcessContext ctx) throws Exception {
		
		CsvStream stream = new CsvStream(new SourceURL(file));
		stream.init();
		
		Data item = stream.read();
		
		while (item != null) {
			String frame = (String) item.get("frame");
			String label = (String) item.get("label");
			
			labels.put(Integer.parseInt(frame), label);
			
			item = stream.read();
		}
		
		stream.close();
		
		super.init(ctx);
	}

	@Override
	public Data process(Data input) {
		
		String filename = (String) input.get("FileName");
		Integer frame = Integer.parseInt(filename.replace(".jpg", ""));
		
		if (labels.containsKey(frame)) {
			input.put("ShotBoundary", true);
			lastlabel = labels.get(frame);
		} else {
			input.put("ShotBoundary", false);
		}
		
		input.remove("Filename");
		input.put("Frame", frame);
		
		input.put("ShotType", lastlabel);
		
		return input;
	}

}
