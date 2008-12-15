/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
// file: MenuBar.java ---
// cre: 2006-09/FH
// rev: 2006-12-18/FH
//-----------------------

package gods.visualizer.graphics;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.JMenuBar;

/**
 * The <code>MenuBar</code> class
 * 
 * @author Fredrik Holmgren
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class MenuBar extends JMenuBar implements ActionListener, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7655611399132193989L;

	/**
	 * 
	 */
	private MainDisplay parent;

	/**
	 * @param parent
	 */
	public MenuBar(MainDisplay parent) {
		this.parent = parent;

		super.setMaximumSize(new Dimension(GUIparams.displaybar_width / 2,
				GUIparams.displaybar_height));

		super.setBorder(BorderFactory.createEmptyBorder(2, // top
				2, // left
				2, // bottom
				2) // right
				);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 *      Implements ActionListener
	 */
	public void actionPerformed(ActionEvent e) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 *      Implements ItemListener
	 */
	public void itemStateChanged(ItemEvent e) {
	}

}
