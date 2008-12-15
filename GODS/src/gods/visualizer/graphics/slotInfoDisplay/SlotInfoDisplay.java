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
// rev: 2007-02-18/FH  
// --------------------

package gods.visualizer.graphics.slotInfoDisplay;

// ---

import gods.cc.ControlCenterRemoteInterface;
import gods.topology.common.SlotInformation;
import gods.topology.events.SlotInformationChanged;
import gods.visualizer.graphics.slotInfoDisplay.commandPanel.CommandPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

// ---

public class SlotInfoDisplay extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3930880744780037965L;
	private InfoTableModel table_model;
	private int row_counter= 0;
	private HashMap<Integer, Integer> id_tab= new HashMap<Integer, Integer>();
	
	/* ---   ---*/
	
	public void SlotInfoChange(SlotInformationChanged event){
		
//		System.out.println("SlotInfoDisplay: info change");
		
		SlotInformation[] slot_info_tab= event.getChangedSlots();
		
		for (SlotInformation slot_info : slot_info_tab) {
		
			Object[] new_row= {new Boolean(false),
					           Integer.toString(slot_info.getSlotId()),
					           slot_info.getVirtualNodeAddress(),
					           slot_info.getCommand(),
					           slot_info.getHostName(),
					           slot_info.getSlotStatus()};
			
			if(id_tab.containsKey(slot_info.getSlotId())){
				this.table_model.setColumnValues(this.id_tab.get(slot_info.getSlotId()), new_row);
			} 
			else {
			this.id_tab.put(slot_info.getSlotId(), this.row_counter);
			this.table_model.setColumnValues(this.row_counter++, new_row);
			}
		}
		this.table_model.fireTableStructureChanged();
	}
	
	/* --- Constructor --- */
	
	public SlotInfoDisplay(ControlCenterRemoteInterface control_center){
		
		this.setBackground(Color.WHITE);
		addComponents(control_center);
	}
	
	/* --- */
	
	public void addComponents(ControlCenterRemoteInterface control_center){
		
		this.table_model= new InfoTableModel();
		
		JTable table= new JTable(table_model);
		
		JScrollPane scroll_pane= new JScrollPane(table);

		this.setLayout(new BorderLayout());
		
		CommandPanel cmd_panel= new CommandPanel(this.table_model, control_center);   
		
		this.add(scroll_pane, BorderLayout.CENTER);
		this.add(cmd_panel, BorderLayout.SOUTH);
		
	}
	
	
}
