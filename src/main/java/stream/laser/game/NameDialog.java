/**
 * 
 */
package stream.laser.game;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author chris
 * 
 */
public class NameDialog extends JDialog {

	/** The unique class ID */
	private static final long serialVersionUID = 4389925220848341664L;

	final JTextField nameField = new JTextField(20);
	String name = null;
	JFrame parent;

	public NameDialog(JFrame parent) {
		super(parent);
		this.parent = parent;
		setModal(true);

		getContentPane().setLayout(new BorderLayout());

		JPanel content = new JPanel(new GridLayout(1, 1));
		content.add(new JLabel("Name:"));
		content.add(nameField);
		getContentPane().add(content, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout());
		final JButton ok = new JButton("Ok");
		ok.setEnabled(false);
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});

		buttons.add(ok);
		getContentPane().add(buttons, BorderLayout.SOUTH);

		nameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				ok.setEnabled(nameField.getText() != null
						&& !nameField.getText().trim().isEmpty());
			}
		});
		this.pack();
	}

	public String getName() {
		return name;
	}

	public void ok() {
		this.name = nameField.getText();
		setVisible(false);
	}

	public void center() {
		if (parent != null) {
			int w = parent.getWidth();
			int h = parent.getHeight();

			this.setLocation(w / 2, h / 2);
		}
	}

	public static void main(String args[]) {
		NameDialog d = new NameDialog(null);
		d.setVisible(true);
		System.out.println("Name was: " + d.getName());
	}
}
