/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment.generators;

import gods.experiment.AbstractChurnEventGenerator;
import gods.experiment.AbstractExperimentEvent;
import gods.experiment.TimeStamp;
import gods.experiment.events.JoinExperimentEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * The <code>JoinEventGenerator</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class JoinEventGenerator extends AbstractChurnEventGenerator {

	/**
	 * Time interval between node joins
	 */
	private static final TimeStamp timeInterval = new TimeStamp(3, 0); 
	
	/**
	 * No of machines hosting the virtual nodes 
	 */
	private static final int noOfHosts = 4;
	
	/**
	 * Previously generated Vnid
	 */
	private int lastVnid = 0;
	
	/**
	 * A pseudo random number generator for VNids
	 */
	private Random randomVnode;
	
	/**
	 * Holds the timestamp for last generated event
	 */
	private TimeStamp currentTime;
	
	/**
	 * Holds the values of VNids on which an application has joined
	 */
	private List<Integer> joinedNodes = new LinkedList<Integer>();
	
	/**
	 * @param noOfVnodes
	 * @param seed
	 */
	public JoinEventGenerator(int noOfVnodes, int seed) {
		super(noOfVnodes, seed);
		
		randomVnode = new Random(seed);
		
		currentTime = new TimeStamp();
	}

	/* (non-Javadoc)
	 * @see gods.experiment.AbstractChurnEventGenerator#getNextChurnEvent()
	 */
	@Override
	public AbstractExperimentEvent getNextChurnEvent() {
		
		JoinExperimentEvent joinEvent = new JoinExperimentEvent();  
		
		currentTime.add(timeInterval);
		
		int vnid = getNextVnid();
		
		joinedNodes.add(vnid);
		joinEvent.setVnid(vnid);
		
		try {
			joinEvent.setTimeToHappen((TimeStamp)currentTime.clone());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		return joinEvent;
	}

	/* (non-Javadoc)
	 * @see gods.experiment.ChurnEventGenerator#getJoinedNodesCount()
	 */
	public int getJoinedNodesCount() {
		
		return joinedNodes.size();
	}
	
	private int getNextVnid(){
		
		int vnid = 0;
		
		if(lastVnid == 0){
			lastVnid = 1;
			vnid = 1;
		}
		else{
			
			int slotsPerMachine = getNoOfVnodes() / noOfHosts;
			vnid = lastVnid + slotsPerMachine;
			
			if(vnid > getNoOfVnodes()){
				vnid = vnid % getNoOfVnodes() + 1;
			}
			lastVnid = vnid;
		}
		return vnid;
	}

}
