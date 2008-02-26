package de.tud.kom.p2psim.impl.analyzer;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.analyzer.Analyzer.ChurnAnalyzer;
import de.tud.kom.p2psim.api.common.Host;
import de.tud.kom.p2psim.impl.simengine.Simulator;

public class MedianSessionLengthAnalyzer implements ChurnAnalyzer {

	private boolean isRunning;

	private Map<Host, Long> onlineHosts;

	private List<Long> onlineTimes;

	public MedianSessionLengthAnalyzer() {
		this.isRunning = false;
		this.onlineHosts = new HashMap<Host, Long>();
		this.onlineTimes = new LinkedList<Long>();
	}

	public void offlineEvent(Host host) {
		if (this.onlineHosts.containsKey(host)) {
			long sessionLength = Simulator.getCurrentTime() - this.onlineHosts.get(host);
			this.onlineTimes.add(sessionLength);
			this.onlineHosts.remove(host);
		}

	}

	public void onlineEvent(Host host) {
		if (this.isRunning) {
			this.onlineHosts.put(host, Simulator.getCurrentTime());
		}
	}

	public void start() {
		this.isRunning = true;

	}

	public void stop(Writer output) {
		this.isRunning = false;
		Collections.sort(onlineTimes);
		int medianPos = (int) Math.round((this.onlineTimes.size() + 1) / 2d);
		long result = this.onlineTimes.get(medianPos - 1);
		long sum = 0;
		for (long time : onlineTimes) {
			sum+=time;
		}
		long mean = sum/onlineTimes.size();
		try {
			output.write("\n******** Median Session Length Stats ***********\n");
			output.write("Median Session length in minutes: " + result / Simulator.MINUTE_UNIT + "\n");
			output.write("Mean Session length in minutes: " + mean / Simulator.MINUTE_UNIT + "\n");
			output.write("******* Median Session Length Stats *********\n");

		} catch (IOException e) {
			throw new IllegalStateException("Problems in writing results" + e);
		}

	}

}
