/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
// file: MainDisplay ---
// cre: 2006-09/FH
// rev: 2006-12-18/FH
//----------------------

package gods.visualizer.graphics;

import gods.cc.ControlCenterRemoteInterface;
import gods.topology.events.SlotInformationChanged;
import gods.visualizer.Visualizer;
import gods.visualizer.graphics.experiment.ExperimentInfoDisplay;
import gods.visualizer.graphics.mainDisplayBar.DisplayBar;
import gods.visualizer.graphics.slotInfoDisplay.SlotInfoDisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * The <code>MainDisplay</code> class
 * 
 * @author Fredrik Holmgren
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class MainDisplay {

	/**
	 * 
	 */
	private JDesktopPane desktop;

	/**
	 * 
	 */
	private InternalDisplay slots_display_frame;
	
	/**
	 * 
	 */
	private InternalDisplay experiment_display_frame;

	/**
	 * 
	 */
	private SlotInfoDisplay slot_info_area;
	
	/**
	 * 
	 */
	private ExperimentInfoDisplay experiment_info_area;

	/**
	 * 
	 */
	private DisplayBar display_bar;

	/**
	 * 
	 */
	private JFrame frame;

	/**
	 * 
	 */
	private Visualizer vengine;

	/**
	 * @return
	 */
	public Visualizer getEngine() {
		return this.vengine;
	}

	/**
	 * @return
	 */
	public SlotInfoDisplay getSlotInfoArea() {
		return this.slot_info_area;
	}

	/**
	 * @param event
	 */
	public void SlotInfoChange(SlotInformationChanged event) {
		this.slot_info_area.SlotInfoChange(event);
	}

	/**
	 * 
	 */
	private void initLookAndFeel() {
		try {
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
			// JFrame.setDefaultLookAndFeelDecorated(true);
		} catch (Exception e) {
			JFrame.setDefaultLookAndFeelDecorated(true);
		}
	}

	/**
	 * @param cont
	 * @param control_center
	 */
	public void createComps(Container cont, ControlCenterRemoteInterface control_center) {

		cont.setLayout(new BorderLayout());

		// --- Desktop ---

		desktop = new JDesktopPane();
		desktop.setPreferredSize(new Dimension(GUIparams.display_area_width,
				GUIparams.display_area_height));
		desktop.setBackground(Color.WHITE);

		// --- Setup display ---

		slots_display_frame = new InternalDisplay(
				GUItext.LABEL_SLOTS_INFO_DISPLAY,
				GUIparams.display_area_width / 2, 0,
				GUIparams.display_area_width / 2, GUIparams.display_area_height);
		slots_display_frame.setVisible(true);
		
		slot_info_area = new SlotInfoDisplay(control_center);
		slot_info_area.setVisible(true);
		slots_display_frame.add(slot_info_area);
		
		experiment_display_frame = new InternalDisplay(
				GUItext.LABEL_EXPERIMENT_INFO_DISPLAY,
				0, 0,
				240, 60);
		experiment_display_frame.setVisible(true);
		
		experiment_info_area = new ExperimentInfoDisplay(control_center);
		experiment_info_area.setVisible(true);
		experiment_display_frame.add(experiment_info_area);
		
		
		desktop.add(slots_display_frame);
		desktop.add(experiment_display_frame);

		// --- Display bar ---

		display_bar = new DisplayBar(this);

		// --- Menu ---

		MenuBar menu_bar = new MenuBar(this);

		// --- Add components ---

		cont.add(menu_bar, BorderLayout.PAGE_START);
		cont.add(desktop, BorderLayout.CENTER);
		cont.add(display_bar, BorderLayout.PAGE_END);

	}

	/**
	 * Constructor
	 * 
	 * @param vengine
	 * @param control_center
	 */
	public MainDisplay(Visualizer vengine, ControlCenterRemoteInterface control_center) {

		this.vengine = vengine;

		// Window decorations

		initLookAndFeel();

		// Create and set up the window

		frame = new JFrame(GUItext.LABEL_MAIN_DISPLAY);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Contents

		createComps(frame.getContentPane(), control_center);

		// Display window

		frame.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
				this.getClass().getResource("images/sicsLogo2xSmall.jpg")));
		frame.pack();
		frame.setVisible(true);
	}

}
