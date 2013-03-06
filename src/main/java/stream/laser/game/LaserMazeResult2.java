package stream.laser.game;

import stream.AbstractProcessor;
import stream.Data;

public class LaserMazeResult2 extends AbstractProcessor {

	Double base = 100.0d;
	Double timePenalty = 1.0;
	Double errorPenalty = 1.5;

	@Override
	public Data process(Data data) {
		Double onPath = null;
		Double error = null;
		Double time = null;

		try {
			onPath = new Double(data.get("onpath") + "");
		} catch (Exception e) {
			onPath = null;
		}

		try {
			error = new Double(data.get("error") + "");
		} catch (Exception e) {
			error = null;
		}

		Double seconds = 0.0;
		try {
			time = new Double(data.get("time") + "");
			seconds = time / 1000.0d;
		} catch (Exception e) {
			time = null;
		}

		if (time != null && error != null && onPath != null) {
			Double result = Math.max(0, base - (seconds * this.timePenalty)
					- error * errorPenalty); // ((onPath
			// /
			// error)
			// * time) / 100;
			data.put("@result2", result);
		}
		return data;

	}
}
