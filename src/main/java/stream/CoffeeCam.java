/**
 * 
 */
package stream;

import java.net.URL;

import javax.swing.JOptionPane;

/**
 * @author chris
 * 
 */
public class CoffeeCam {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			URL url = CoffeeCam.class.getResource("/coffee-cam.xml");
			stream.run.main(url);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error!",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
