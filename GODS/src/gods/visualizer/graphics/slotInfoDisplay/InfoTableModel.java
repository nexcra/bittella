/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */

// file: Start --------
// cre: 2006-12/FH  
// rev: 2007-02-18/FH  
// --------------------

package gods.visualizer.graphics.slotInfoDisplay;

import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

/**
 * The <code>InfoTableModel</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */

public class InfoTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9208881086238960330L;

	/**
	 * 
	 */
	public final String SELECTED = "selected";

	/**
	 * 
	 */
	public final String SLOT_ID = "slot id";

	/**
	 * 
	 */
	public final String NODE_ADR = "node address";

	/**
	 * 
	 */
	public final String COMMAND = "command";

	/**
	 * 
	 */
	public final String HOSTNAME = "host name";

	/**
	 * 
	 */
	public final String STATUS = "status";

	/**
	 * 
	 */
	private String[] column_names = { SELECTED, SLOT_ID, NODE_ADR, COMMAND,
			HOSTNAME, STATUS };

	/**
	 * 
	 */
	private HashMap<Integer, Object[]> column_values = new HashMap<Integer, Object[]>();

	/* --- Aux --- */

	public void setColumnValues(int column, Object[] column_values) {
		// System.out.println("setColumn: "+ column + column_values);
		// for (int i=0; i<column_values.length; i++){
		// System.out.println("i: "+ column_values[i]);
		// }

		this.column_values.put(column, column_values);
	}

	public Object getValueAtColumn(int row, String column_name) {
		int column_index = getColumnIndex(column_name);
		if (column_index == -1) {
			return null;
		}

		return getValueAt(row, column_index);

	}

	private int getColumnIndex(String column_name) {
		int index = -1;
		for (int i = 0; i < column_names.length; i++) {
			if (this.column_names[i].equals(column_name)) {
				index = i;
				break;
			}
		}
		return index;
	}

	/* --- TableModel --- */

	public int getColumnCount() {
		return column_names.length;
	}

	public int getRowCount() {
		return column_values.size();
	}

	@Override
	public String getColumnName(int col) {
		return column_names[col];
	}

	public Object getValueAt(int row, int col) {
		return column_values.get(row)[col];
	}

	@Override
	public Class<?> getColumnClass(int c) {
		if (c == 0) {
			return Boolean.class;
		}

		return String.class;

	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (col > 0) {
			return false;
		}

		return true;

	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		column_values.get(row)[col] = value;
		fireTableCellUpdated(row, col);
		// System.out.println(row + ":" + col + ": " + value + ";");
	}

}
