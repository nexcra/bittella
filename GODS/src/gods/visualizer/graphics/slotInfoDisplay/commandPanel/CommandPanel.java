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

import gods.cc.ControlCenterRemoteInterface;
import gods.visualizer.graphics.slotInfoDisplay.InfoTableModel;

import java.awt.BorderLayout;

import javax.swing.JPanel;


/**
 * The <code>CommandPanel</code> class
 * 
 * @author Fredrik Holmgren
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */

public class CommandPanel extends JPanel {

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 2607785406248984163L;
	private LaunchInputPanelTopHalf top_panel;
	private LaunchInputPanelBottomHalf bottom_panel;
	
	public String getComboBoxValue(){
		return bottom_panel.getComboBoxValue();
	}
	public String getTextFieldValue() {
		return bottom_panel.getTextFieldValue();
	}
	
	/* --- CONSTRUCTOR --- */
	
	public CommandPanel(InfoTableModel table_model, ControlCenterRemoteInterface control_center) {
		this.setLayout(new BorderLayout());
		
		top_panel= new LaunchInputPanelTopHalf(this, table_model, control_center);
		bottom_panel= new LaunchInputPanelBottomHalf(control_center);
		
		this.add(top_panel, BorderLayout.NORTH);
		this.add(bottom_panel, BorderLayout.SOUTH);
		
	}

	// ---
	
}
