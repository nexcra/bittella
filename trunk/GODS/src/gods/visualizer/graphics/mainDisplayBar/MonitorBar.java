/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */

// file: MonitorBar ---
// cre: 2005-01-21/FH
// rev: 2006-10-13/FH
// --------------------

package gods.visualizer.graphics.mainDisplayBar;

import java.awt.*;
import javax.swing.*;
import gods.visualizer.graphics.GUIparams;

/**
 * The <code>MonitorBar</code> class
 * 
 * @author Fredrik Holmgren
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class MonitorBar extends JPanel implements DisplayBarContent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3036767427003219380L;

	/**
	 * Constructor
	 */
	public MonitorBar() {

		// --- CONTAINER --

		this.setLayout(new BorderLayout());

		this.setPreferredSize(new Dimension(GUIparams.displaybar_width,
				GUIparams.displaybar_height + 15));

		// CREATE COMPONENTS---

		JLabel sics_label = new JLabel(new ImageIcon(this.getClass()
				.getResource("images/sics_logo_s2.png")));

		// --- ADD TO CONTAINER ---

		JPanel button_panel = new JPanel();

		JPanel mid_panel = new JPanel();

		JPanel label_panel = new JPanel();
		label_panel.add(sics_label);

		// ---

		this.add(button_panel, BorderLayout.WEST);
		this.add(mid_panel, BorderLayout.CENTER);
		this.add(label_panel, BorderLayout.EAST);

	}

}
