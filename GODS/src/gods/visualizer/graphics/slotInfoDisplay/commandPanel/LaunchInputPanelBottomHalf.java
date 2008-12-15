/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.visualizer.graphics.slotInfoDisplay.commandPanel;

import gods.cc.ControlCenterRemoteInterface;
import gods.churn.ArgumentGeneratorFactory;
import gods.visualizer.graphics.GUIparams;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The <code>CommandPanelBottomHalf</code> class
 *
 * @author Fredrik Holmgren
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */

public class LaunchInputPanelBottomHalf extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3394113533250569119L;
	private JTextField text_field;
	private JComboBox combo_box;
	private ControlCenterRemoteInterface control_center;
	
	public LaunchInputPanelBottomHalf(ControlCenterRemoteInterface control_center) {

		this.control_center = control_center;

		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(
				GUIparams.display_area_width / 2, 30));
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		ArgumentGeneratorFactory.initialize("/var/www/gods/config/arggens.config/gods.churn.arggens.xml");
		String[] patterns = ArgumentGeneratorFactory.getAllDisplayNames();
		//String[] patterns = {"a... a", "b... b", "c... c", "d... d", "e... e"};
		 
		System.err.println("GIGIBEEEEEEEEEEEECALI: " + patterns.length);
		
		combo_box = new JComboBox(patterns);
		combo_box.setEditable(false);
		
		this.add(combo_box, BorderLayout.WEST);

		this.text_field = new JTextField(20);
		this.add(this.text_field, BorderLayout.EAST);

	}
	
	public String getComboBoxValue(){
		return (String) this.combo_box.getSelectedItem();
	}
	
	public String getTextFieldValue(){
		return this.text_field.getText();
	}
	
}
