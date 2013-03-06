/**
 * 
 */
package stream.laser.game;

import stream.Data;
import stream.Processor;

/**
 * @author chris
 * 
 */
public class LevelScore implements Processor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		try {
			Integer level = new Integer(input.get("level") + "");
			Double score = new Double(input.get("@result2") + "");

			input.put("@punkte", 0);

			if (level == 0) {
				input.put("@punkte", punkteLevel1(score));
			}

			if (level == 1) {
				input.put("@punkte", punkteLevel2(score));
			}

			if (level == 2) {
				input.put("@punkte", punkteLevel3(score));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return input;
	}

	public Double punkteLevel1(Double score) {

		if (score > 60.0)
			return 3.0;

		if (score > 40)
			return 2.0d;

		if (score > 20)
			return 1.0d;

		return 0.0;
	}

	public Double punkteLevel2(Double score) {

		if (score > 85.0)
			return 3.0;

		if (score > 70.0)
			return 2.0d;

		if (score > 60.0)
			return 1.0d;

		return 0.0d;
	}

	public Double punkteLevel3(Double score) {

		if (score > 70.0)
			return 3.0d;

		if (score > 55)
			return 2.0;

		if (score > 40)
			return 1.0;

		return 0.0d;
	}
}