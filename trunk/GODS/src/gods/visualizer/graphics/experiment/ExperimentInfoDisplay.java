/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.visualizer.graphics.experiment;

import gods.cc.ControlCenterRemoteInterface;
import gods.experiment.Experiment;
import gods.experiment.events.RunExperiment;
import gods.visualizer.graphics.GUIparams;
import gods.visualizer.graphics.InternalDisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

/**
 * The <code>ExperimentInfoDisplay</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ExperimentInfoDisplay extends JPanel implements ActionListener {

	private static Logger log = Logger.getLogger(ExperimentInfoDisplay.class);

	/**
	 * Filename of the experiment stored earlier that is to be executed now
	 */
	private Experiment experiment = null;

	/**
	 * The <code>ExperimentInfoButton</code> class represents the buttons on
	 * ExperimentInfoDisplay
	 * 
	 * @author Ozair Kafray
	 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
	 */
	private enum ExperimentInfoButton {

		LOAD("load"), RUN("run");

		private String label;

		ExperimentInfoButton(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}
	}

	private InternalDisplay parent;

	/**
	 * 
	 */
	private JButton loadButton = new JButton(ExperimentInfoButton.LOAD.label());

	/**
	 * 
	 */
	private JButton runButton = new JButton(ExperimentInfoButton.RUN.label());

	/**
	 * 
	 */
	private static final long serialVersionUID = 5186821502865352439L;

	/**
	 * 
	 */
	private ControlCenterRemoteInterface control_center = null;

	/**
	 * 
	 */
	public ExperimentInfoDisplay(/* InternalDisplay parent, */
	ControlCenterRemoteInterface control_center) {

		// this.parent = parent;
		this.setBackground(Color.WHITE);
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(GUIparams.display_area_width / 4,
				30));

		this.control_center = control_center;

		addComponents();
	}

	public void addComponents() {

		this.loadButton.addActionListener(this);
		this.runButton.addActionListener(this);

		this.loadButton.setPreferredSize(new Dimension(GUIparams.button_width,
				GUIparams.button_height));
		this.runButton.setPreferredSize(new Dimension(GUIparams.button_width,
				GUIparams.button_height));

		this.add(this.loadButton, BorderLayout.WEST);
		this.add(this.runButton, BorderLayout.EAST);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand() == ExperimentInfoButton.LOAD.label()) {

			String godsHome = null;
			JFileChooser experimentChooser = null;

			if ((godsHome = System.getProperty("gods.home")) != null) {
				experimentChooser = new JFileChooser(godsHome);
			} else {
				experimentChooser = new JFileChooser();
			}

			experimentChooser.setFileFilter(new ExperimentFileFilter());
			if (experimentChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				String experimentFile = experimentChooser.getSelectedFile()
						.getAbsolutePath();
				log.debug("Chosen File is: " + experimentFile);
				experiment = Experiment.load(experimentFile);
			}
		}

		else if (e.getActionCommand() == ExperimentInfoButton.RUN.label()) {

			if (experiment == null) {
				JOptionPane.showMessageDialog(this,
						"Please load an experiment.", "Error",
						JOptionPane.WARNING_MESSAGE);

			} else {

				try {
					RunExperiment runExp = new RunExperiment(1);
					runExp.setExperiment(experiment);
					control_center.notifyEvent(runExp);

				} catch (RemoteException re) {
					log.error(re.getMessage());
				}

			}
		}
	}
}
