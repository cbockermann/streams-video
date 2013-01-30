package stream.laser.game;

import stream.AbstractProcessor;
import stream.Data;

public class LaserMazeResult extends AbstractProcessor{

	@Override
	public Data process(Data data) {
		Double onPath = null;
		Double error = null;
		Double time=null;
		
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

		try {
			time = new Double(data.get("time") + "");
		} catch (Exception e) {
			time = null;
		}
		
		if(time!=null && error !=null && onPath !=null){
			Double result = ((onPath/error)*time)/100;
			data.put("@result", result);
		}
		return data;
		
	}

	

}
