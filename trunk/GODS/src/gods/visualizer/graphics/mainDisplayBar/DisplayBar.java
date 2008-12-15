/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
// file: displayBar.java
// cre: 2005-01-21/FH
// rev: 2006-10-12/FH
// ---------------------

package gods.visualizer.graphics.mainDisplayBar;

// ---

import java.awt.*;

import javax.swing.*;

import gods.visualizer.graphics.GUIparams;
import gods.visualizer.graphics.MainDisplay;

/**
 * The <code>DisplayBar</code> class
 * 
 * @author Fredrik Holmgren
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class DisplayBar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7325836015168300874L;

	/**
	 * 
	 */
	private MainDisplay parent;

	/**
	 * 
	 */
	private DisplayBarContent display_bar_content;

	private enum Content {
		MONITOR_BAR, REPLAY_BAR
	}

	private Content content;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public DisplayBar(MainDisplay parent) {

		this.parent = parent;

		// ---

		super.setLayout(new FlowLayout());
		super.setMaximumSize(new Dimension(GUIparams.displaybar_width,
				GUIparams.displaybar_height));

		// ---

		this.display_bar_content = new MonitorBar();
		this.add((JPanel) this.display_bar_content);
		this.content = Content.MONITOR_BAR;

	}

}
