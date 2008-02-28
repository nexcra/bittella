package de.tud.kom.p2psim.impl.analyzer;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.analyzer.Analyzer;
import de.tud.kom.p2psim.api.simengine.SimulationEvent;
import de.tud.kom.p2psim.api.simengine.SimulationEventHandler;
import de.tud.kom.p2psim.impl.common.DefaultMonitor;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 04.12.2007
 * 
 */

public class MemoryUsageAnalyzer implements SimulationEventHandler, Analyzer {

	private static Logger log = SimLogger.getLogger(MemoryUsageAnalyzer.class);

	/**
	 * Memory object representing the java virtual machine memory usage.
	 */
	/** One megabyte of memory units. */
	public static final double MEGABYTE = 1024 * 1024;

	/** Maximum used memory. */
	private double maxused;

	/** Average used memory. */
	private double avgused;

	/** Maximum free memory. */
	private double maxfree;

	/** Average free memory. */
	private double avgfree;

	/** Last memory status time. */
	private int count;

	/** Denotes if the memory analyzer is activated */
	private boolean isRunning;

	/** Output interval of periodic memory status events */
	private long outInterval;

	/**
	 * Create new memory object for collecting java virtual machine memory
	 * usage.
	 */
	public MemoryUsageAnalyzer() {
		Runtime run = Runtime.getRuntime();
		this.maxused = run.totalMemory() / MemoryUsageAnalyzer.MEGABYTE;
		this.avgused = this.maxused;
		this.maxfree = run.freeMemory() / MemoryUsageAnalyzer.MEGABYTE;
		this.avgfree = this.maxfree;
		this.count++;
		this.isRunning = false;
	}

	/**
	 * Collect new java virtual memory usage information and return current
	 * usage.
	 */
	public void collect() {
		System.gc();
		Runtime run = Runtime.getRuntime();
		double used = run.totalMemory() / MemoryUsageAnalyzer.MEGABYTE;
		double free = run.freeMemory() / MemoryUsageAnalyzer.MEGABYTE;
		this.maxused = Math.max(this.maxused, used);
		this.maxfree = Math.max(this.maxfree, free);
		this.avgused += used;
		this.avgfree += free;
		this.count++;
	}

	/** {@inheritDoc} */
	public String toString() {
		Runtime run = Runtime.getRuntime();
		double used = run.totalMemory() / MemoryUsageAnalyzer.MEGABYTE;
		double free = run.freeMemory() / MemoryUsageAnalyzer.MEGABYTE;
		return String.format("%1$.1f(%2$.1f)", new Object[] { new Double(used), new Double(free) });
	}

	public void eventOccurred(SimulationEvent se) {
		if (this.isRunning) {
			this.collect();
			log.info(toString());
			Simulator.scheduleEvent(null, Simulator.getCurrentTime() + this.outInterval, this, SimulationEvent.Type.STATUS);
		}
	}

	/**
	 * Sets the output interval of periodic memory status events 
	 * 
	 * @param time
	 *            the output interval of periodic memory status events 
	 *           
	 */
	public void setOutputInterval(long time) {
		this.outInterval = time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.analyzer.Analyzer#start()
	 */public void start() {
		this.isRunning = true;
		this.collect();
		log.info(toString());
		Simulator.scheduleEvent(null, Simulator.getCurrentTime() + this.outInterval, this, SimulationEvent.Type.STATUS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.analyzer.Analyzer#stop(java.io.Writer)
	 */
	public void stop(Writer output) {
		this.isRunning = false;
		this.collect();
		try {
			output.write("\n******** Memory Usage Stats ***********\n");
			output.write(this.toStatus());
			output.write("******* Memory Usage Stats End *********\n");

		} catch (IOException e) {
			throw new IllegalStateException("Problems in writing results" + e);
		}
	}

	private String toStatus() {
		return "\tstats memory used[m/a] free[m/a]\n\tstats memory "
				+ String.format("%1$.1f %2$.1f %3$.1f %4$.1f", new Object[] { new Double(this.maxused), new Double(this.avgused / this.count), new Double(this.maxfree),
						new Double(this.avgfree / this.count), }) + "\n";
	}
}
