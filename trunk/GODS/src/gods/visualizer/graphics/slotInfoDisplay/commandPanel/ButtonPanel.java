/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
// file: --------
// cre: 2006-12/FH  
// rev: 2007-02-15/FH  
// --------------------
package gods.visualizer.graphics.slotInfoDisplay.commandPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import gods.visualizer.graphics.GUIparams;

/**
 * The <code>ButtonPanel</code> class
 * 
 * @author Fredrik Holmgren
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */

public class ButtonPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 6276429789647734631L;

	private final String LAUNCH = "launch";

	private final String STOP = "stop";

	private final String KILL = "kill";

	private LaunchInputPanelTopHalf parent;

	private JButton launch_button = new JButton(LAUNCH);

	private JButton stop_button = new JButton(STOP);

	private JButton kill_button = new JButton(KILL);

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ButtonPanel(LaunchInputPanelTopHalf parent) {

		this.parent = parent;

		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(GUIparams.display_area_width / 4,
				30));

		this.launch_button.addActionListener(this);
		this.stop_button.addActionListener(this);
		this.kill_button.addActionListener(this);

		this.launch_button.setPreferredSize(new Dimension(
				GUIparams.button_width, GUIparams.button_height));

		this.stop_button.setPreferredSize(new Dimension(GUIparams.button_width,
				GUIparams.button_height));

		this.kill_button.setPreferredSize(new Dimension(GUIparams.button_width,
				GUIparams.button_height));

		this.add(this.launch_button, BorderLayout.WEST);
		this.add(this.stop_button, BorderLayout.CENTER);
		this.add(this.kill_button, BorderLayout.EAST);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {

		String s = event.getActionCommand();
		// System.out.println("button pressed: --- " + s + ";");

		if (s == LAUNCH) {
			parent.launch();
		} else if (s == STOP) {
			parent.stop();
		} else if (s == KILL) {
			parent.kill();
		}

	}

}
