package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.io.Serializable;
import java.util.ArrayList;

public class RttDistribution implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2306382710981289429L;

	// private ArrayList<Double> rttArray = new ArrayList<Double>();

	private double min = Double.NaN;

	// private double mean = Double.NaN;
	// private double sdDeviation = Double.NaN;
	// private boolean finalized = false;

	public void finalize() {
		// if (rttArray != null)
		// rttArray.clear();
		// rttArray = null;
	}

	public void clear() {
		// rttArray.clear();
		min = 0.0;
		// mean = Double.NaN;
		// sdDeviation = Double.NaN;

	}

	public double getMinRTT() {
		return min;
	}

	public double getMeanRTT() {
		// return mean;
		return 0;
	}

	public double getSdDeviationRTT() {
		// return sdDeviation;
		return 0;
	}

	public ArrayList<Double> getMeasuredVaues() {
		// return rttArray;
		return new ArrayList<Double>();
	}

	/*
	 * private void calculateMinRTT() { try { min = Collections.min(rttArray); }
	 * catch (NoSuchElementException e) {
	 *  } }
	 * 
	 * private void calculateMeanRTT() { mean = 0.0; for (Double rtt : rttArray)
	 * mean += rtt; mean /= (double)rttArray.size(); }
	 * 
	 * 
	 * private void calculateSdDeviationRTT() { sdDeviation = 0.0; for (Double
	 * rtt : rttArray) sdDeviation += Math.pow( rtt - getMeanRTT() , 2);
	 * sdDeviation /= (double)rttArray.size(); sdDeviation =
	 * Math.sqrt(sdDeviation);
	 *  }
	 */

	public boolean addRtt(double rtt) {
		/*
		 * if (rttArray == null) return false; rttArray.add(rtt);
		 * calculateMinRTT(); calculateMeanRTT(); calculateSdDeviationRTT();
		 */
		if (min > rtt || Double.compare(Double.NaN, min) == 0)
			min = rtt;

		return true;
	}

	public boolean mergeRtt(RttDistribution rtt) {
		/*
		 * if (rttArray.size() == 0) { this.min = Math.min(this.min,
		 * rtt.getMinRTT()); this.mean = ( this.mean + rtt.getMeanRTT() ) / 2.0;
		 * this.sdDeviation = ( this.sdDeviation + rtt.getSdDeviationRTT() ) /
		 * 2.0; return false; } rttArray.addAll(rtt.getMeasuredVaues());
		 * calculateMinRTT(); calculateMeanRTT(); calculateSdDeviationRTT();
		 */

		if (this.min > 0.0 && rtt.getMinRTT() > 0.0)
			min = Math.min(min, rtt.getMinRTT());
		else if (rtt.getMinRTT() > 0.0)
			min = rtt.getMinRTT();

		return true;
	}

}