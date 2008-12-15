/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
// file: InternalDisplay ---
// cre: 2006-09-22/FH
// rev: 2006-09-22/FH
//----------------------

package gods.visualizer.graphics;

import javax.swing.JInternalFrame;

/**
 * The <code>InternalDisplay</code> class
 * 
 * @author Fredrik Holmgren
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class InternalDisplay extends JInternalFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 3009578520922814722L;

	/**
	 * @param title
	 * @param x
	 * @param y
	 * @param xlen
	 * @param ylen
	 */
	public InternalDisplay(String title, int x, int y, int xlen, int ylen) {
		super(title, true, // resizable
				true, // closable
				true, // maximizable
				true); // iconifiable

		setSize(xlen, ylen);
		setLocation(x, y);
	}

}
