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
import gods.churn.events.KillApplicationEvent;
import gods.churn.events.LaunchApplicationEvent;
import gods.churn.events.StopApplicationEvent;
import gods.visualizer.graphics.GUIparams;
import gods.visualizer.graphics.slotInfoDisplay.InfoTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The <code>CommandPanelTopHalf</code> class
 *
 * @author Fredrik Holmgren
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class LaunchInputPanelTopHalf extends JPanel implements ActionListener {

	private static final long serialVersionUID = -6365516384434559850L;
	private JTextField text_field;
	private InfoTableModel table_model;
	private ControlCenterRemoteInterface control_center;
	private int event_priority = 9;
	private CommandPanel parent;

	/**
	 * Constructor
	 * 
	 * @param table_model
	 * @param control_center
	 */
	public LaunchInputPanelTopHalf(CommandPanel parent, InfoTableModel table_model, ControlCenterRemoteInterface control_center) {
		this.parent= parent;
		
		this.table_model = table_model;
		this.control_center = control_center;

		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(
				GUIparams.display_area_width / 2, 30));
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel cmd_label = new JLabel(" Command: ");
		this.add(cmd_label, BorderLayout.WEST);

		this.text_field = new JTextField(10);
		this.text_field.addActionListener(this);
		this.add(this.text_field, BorderLayout.CENTER);

		ButtonPanel button_panel = new ButtonPanel(this);
		this.add(button_panel, BorderLayout.EAST);

	}

	/* --- Commands --- */

	public void actionPerformed(ActionEvent evt) {
		launch();
	}

	//
	public void launch() {
		String text = this.text_field.getText();
		this.text_field.setText(null);

		ArrayList<String> slotIds = getCheckedRows();

		if (slotIds.size() > 0) {
			LaunchApplicationEvent event = new LaunchApplicationEvent(
					this.event_priority);
			int[] id_tab = convertToIntTab(slotIds);
			event.setSlotIds(id_tab);
			
			// collect additional args...
			
			String arg2= parent.getComboBoxValue();
			String arg3= parent.getTextFieldValue();

			System.out.println("arg2: " + arg2);
			System.out.println("arg3: " + arg3);
			
			event.setAppLaunchCommand(text);
			
			try{
				this.control_center.notifyEvent(event);
			}catch(RemoteException re){
				re.printStackTrace();
			}

		}
	}

	//
	public void stop() {
		this.text_field.setText(null);
		ArrayList<String> slotIds = getCheckedRows();
		if (slotIds.size() > 0) {
			StopApplicationEvent event = new StopApplicationEvent(
					this.event_priority);
			event.setSlotIds(convertToIntTab(slotIds));
			
			try{
				this.control_center.notifyEvent(event);
			}catch(RemoteException re){
				re.printStackTrace();
			}
		}
	}

	//
	public void kill() {
		this.text_field.setText(null);
		ArrayList<String> slotIds = getCheckedRows();
		if (slotIds.size() > 0) {
			KillApplicationEvent event = new KillApplicationEvent(
					this.event_priority);
			event.setSlotIds(convertToIntTab(slotIds));
			
			try{
				this.control_center.notifyEvent(event);
			}catch(RemoteException re){
				re.printStackTrace();
			}
		}
	}

	// --- AUX

	private ArrayList<String> getCheckedRows() {
		ArrayList<String> slotIds = new ArrayList<String>();
		for (int i = 0; i < this.table_model.getRowCount(); i++) {
			if ((Boolean) this.table_model.getValueAt(i, 0)) {
				slotIds.add(((String) this.table_model.getValueAtColumn(i,
						this.table_model.SLOT_ID)));
			}
		}
		return slotIds;
	}

	private int[] convertToIntTab(ArrayList<String> slotIds) {
		int[] id_tab = new int[slotIds.size()];
		int index = 0;
		for (String string_value : slotIds) {
			id_tab[index] = Integer.valueOf(string_value);
			index++;
		}
		return id_tab;
	}
}
