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
import gods.experiment.TimeStamp;
import gods.experiment.events.AbstractChurnExperimentEvent;
import gods.experiment.events.FailExperimentEvent;
import gods.experiment.events.JoinExperimentEvent;
import gods.experiment.events.LeaveExperimentEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * The <code>DummyEventGenerator</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class DummyEventGenerator extends AbstractChurnEventGenerator {

	/**
	 * A pseudo random number generator with the provided seed for generating
	 * the number of seconds at which next event should happen
	 */
	private Random randomSeconds;

	/**
	 * A pseudo random number generator with the provided seed for generating
	 * the number of microseconds at which next event should happen
	 */
	private Random randomMicroSeconds;

	/**
	 * A pseudo random number generator for generating different churn events
	 */
	private Random randomEvent;

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
	 * Count of the total nodes that have joined the ring
	 */
	private int joinedNodesCount = 0;

	/**
	 * @param netSize -
	 *            number of nodes in the modeled network
	 * @param seed
	 */
	public DummyEventGenerator(int noOfVnodes, int seed) {
		super(noOfVnodes, seed);

		randomSeconds = new Random(seed);
		randomMicroSeconds = new Random(seed);
		randomEvent = new Random(seed);
		randomVnode = new Random(seed);
		
		currentTime = new TimeStamp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gods.experiment.ChurnEventGenerator#getNextChurnEvent()
	 */
	@Override
	public AbstractChurnExperimentEvent getNextChurnEvent() {

		AbstractChurnExperimentEvent event = null;

		currentTime.add(new TimeStamp(randomSeconds.nextInt(10),
				randomMicroSeconds.nextInt(1000000)));
		System.out.println("Current Time = " + currentTime.toString());

		if (joinedNodes.size() < 1) {
			event = createJoinEvent();
		}

		else {

			int eventType = randomEvent.nextInt(3);
			// System.out.println("randomEvent = " + eventType);

			switch (eventType) {

			case 0:
				event = createJoinEvent();
				break;

			case 1:
				event = createLeaveEvent();
				break;

			case 2:
				event = createFailEvent();
				break;

			default:
				event = createJoinEvent();
				break;
			}
		}

		try {
			event.setTimeToHappen((TimeStamp)currentTime.clone());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return event;
	}

	private JoinExperimentEvent createJoinEvent() {
		JoinExperimentEvent joinEvent = new JoinExperimentEvent();

		int vnid = randomVnode.nextInt(getNoOfVnodes()) + 1;
		while (joinedNodes.contains(vnid)) {
			vnid = randomVnode.nextInt(getNoOfVnodes());
		}

		joinEvent.setVnid(vnid);
		joinedNodes.add(vnid);
		++joinedNodesCount;
		
		return joinEvent;
	}

	private LeaveExperimentEvent createLeaveEvent() {
		LeaveExperimentEvent leaveEvent = new LeaveExperimentEvent();

		int node = randomVnode.nextInt(joinedNodes.size());
		int vnid = joinedNodes.get(node);

		leaveEvent.setVnid(vnid);

		joinedNodes.remove(node);

		return leaveEvent;
	}

	private FailExperimentEvent createFailEvent() {
		FailExperimentEvent failEvent = new FailExperimentEvent();

		int node = randomVnode.nextInt(joinedNodes.size());
		int vnid = joinedNodes.get(node);

		failEvent.setVnid(vnid);

		joinedNodes.remove(node);

		return failEvent;
	}
	
	/** 
	 * {@link gods.experiment.ChurnEventGenerator#getJoinedNodesCount()}
	 */
	public int getJoinedNodesCount() {
		return joinedNodesCount;
	}
}
