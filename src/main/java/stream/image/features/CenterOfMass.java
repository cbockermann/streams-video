package stream.image.features;

import stream.Data;
import stream.image.AbstractImageProcessor;
import stream.image.ImageRGB;

/**
 * This processor calculates the Center of mass of one ColorChannel of the
 * Image. You can either ask for the absolute x- and y-Coordinates of the Center
 * of Mass or the normalized Center of Mass (= absolute Center of Mass / the
 * size of the image).
 * 
 * @author Matthias
 * 
 */
public class CenterOfMass extends AbstractImageProcessor {

	String colorchannel = "red";
	Boolean normalized = true;
	
	/**
	 * Tells the CenterOfMass processor, on which color to base the Center of Mass computation on.
	 * @param colorchannel the color. Possible values are "red", "green", and "blue"
	 */
	public void setColorchannel(String colorchannel) {
		this.colorchannel = colorchannel;
	}
	
	/**
	 * Delivers, on which color the Center of Mass computation is based on.
	 * @return the actual color. Possible values are "red", "green", and "blue"
	 */
	public String getColorchannel() {
		return colorchannel;
	}
	
	/**
	 * Tells the CenterOfMass processor, if we are looking for the absolute or the normalized x- and
	 * y- coordinates of the Center of Mass.
	 * @param normalized
	 */
	public void setNormalized(Boolean normalized) {
		this.normalized = normalized;
	}
	
	/**
	 * Delivers, if the CenterOfMass processor is actually looking for the absolute or the normalized x- and
	 * y- coordinates of the Center of Mass.
	 * @return
	 */
	public Boolean getNormalized() {
		return normalized;
	}

	@Override
	public Data process(Data item, ImageRGB img) {

		// Calculation of the x-CenterOfMass

		int sumofallpixels = 0;
		int[] rowsums = new int[img.getWidth()];

		for (int i = 0; i < img.getWidth(); i++) {

			rowsums[i] = 0;

			for (int j = 0; j < img.getHeight(); j++) {

				if (colorchannel.equalsIgnoreCase("red")) {
					rowsums[i] += img.getRED(i, j);
					sumofallpixels += img.getRED(i, j);
				} else
				if (colorchannel.equalsIgnoreCase("green")) {
					rowsums[i] += img.getGREEN(i, j);
					sumofallpixels += img.getGREEN(i, j);
				} else
				if (colorchannel.equalsIgnoreCase("blue")) {
					rowsums[i] += img.getBLUE(i, j);
					sumofallpixels += img.getBLUE(i, j);
				} else
				if (colorchannel.equalsIgnoreCase("gray")) {
					int red = img.getRED(i, j);
					int green = img.getGREEN(i, j);
					int blue = img.getBLUE(i, j);
					int gray = Math.round((1/3)*red+(1/3)*green+(1/3)+blue);
					rowsums[i] += gray;
					sumofallpixels += gray;
				}
				if (colorchannel.equalsIgnoreCase("black_and_white")) {
					int red = img.getRED(i, j);
					int green = img.getGREEN(i, j);
					int blue = img.getBLUE(i, j);
					int gray = Math.round((1/3)*red+(1/3)*green+(1/3)+blue);
					if (gray > 125) {
						rowsums[i] += gray;
						sumofallpixels += gray;
					}
				}
			}
		}

		int valuetosearchfor = sumofallpixels / 2;

		int sum = 0;
		int x = 0;

		while (sum < valuetosearchfor) {
			sum += rowsums[x];
			x++;
		}

		if (normalized == true) {
			double normalizedxcenterofmass = (double) x / img.getWidth();
			item.put("frame:"+colorchannel + ":CenterOfMass:normalizedX",
					normalizedxcenterofmass);
		} else {
			item.put("frame:"+colorchannel + ":CenterOfMass:X", x);
		}

		// Calculation of the y-CenterOfMass

		int[] columnsum = new int[img.getHeight()];

		for (int j = 0; j < img.getHeight(); j++) {

			columnsum[j] = 0;

			for (int i = 0; i < img.getWidth(); i++) {

				if (colorchannel.equalsIgnoreCase("red")) {
					columnsum[j] += img.getRED(i, j);
				} else
				if (colorchannel.equalsIgnoreCase("green")) {
					columnsum[j] += img.getGREEN(i, j);
				} else
				if (colorchannel.equalsIgnoreCase("blue")) {
					columnsum[j] += img.getBLUE(i, j);
				} else
				if (colorchannel.equalsIgnoreCase("gray")) {
					int red = img.getRED(i, j);
					int green = img.getGREEN(i, j);
					int blue = img.getBLUE(i, j);
					int gray = Math.round((1/3)*red+(1/3)*green+(1/3)+blue);
					columnsum[j] += gray;
				} else 
				if (colorchannel.equalsIgnoreCase("black_and_white")) {
					int red = img.getRED(i, j);
					int green = img.getGREEN(i, j);
					int blue = img.getBLUE(i, j);
					int gray = Math.round((1/3)*red+(1/3)*green+(1/3)+blue);
					if (gray > 125) {
						columnsum[j] += gray;
					}
				}

			}
		}

		sum = 0;
		int y = 0;

		while (sum < valuetosearchfor) {
			sum += columnsum[y];
			y++;
		}

		if (normalized == true) {
			double normalizedycenterofmass = (double) y / img.getHeight();
			item.put("frame:"+colorchannel + ":CenterOfMass:normalizedY",
					normalizedycenterofmass);
		} else {
			item.put("frame:"+colorchannel + ":CenterOfMass:Y", y);
		}

		return item;
	}

}
