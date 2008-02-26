package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.util.ArrayList;
import java.util.Collections;

import umontreal.iro.lecuyer.probdist.LognormalDist;
import de.tud.kom.p2psim.impl.simengine.Simulator;

public class LinkProperty {

	double minRtt;

	double averageRtt;

	double delayVariation; // interquartile range

	double packetLoss;

	LognormalDist jitterDistribution;

	public LinkProperty(double minRtt, double averageRtt, double delayVariation, double packetLoss) {
		this.minRtt = minRtt;
		this.averageRtt = averageRtt;
		this.delayVariation = delayVariation;
		this.packetLoss = packetLoss;

		if (delayVariation > 0) {
			JitterParameter jp = getJitterParameterDownhillSimplex(averageRtt - minRtt, delayVariation);
			jitterDistribution = new LognormalDist(jp.m, jp.s);
		}
	}

	public double getMinRtt() {
		return minRtt;
	}

	public double getAverageRtt() {
		return averageRtt;
	}

	public double getDelayVariation() {
		return delayVariation;
	}

	public double getPacketLoss() {
		return packetLoss;
	}

	public double nextJitter() {
		if (jitterDistribution != null) {
			return jitterDistribution.inverseF(Simulator.getRandom().nextDouble());
		} else
			return 0.0;
	}

	public String toString() {
		return "minRtt: " + minRtt + " averageRtt: " + averageRtt + " delayVariation: " + delayVariation + " packetLoss: " + packetLoss;
	}

	/**
	 * 
	 * @param ev
	 *            expected Value
	 * @param iqr
	 *            interquartile range
	 * @return
	 */
	private JitterParameter getJitterParameterDownhillSimplex(double ev, double iqr) {

		ArrayList<JitterParameter> solutions = new ArrayList<JitterParameter>();
		solutions.add(new JitterParameter(0.1, 0.1, ev, iqr));
		solutions.add(new JitterParameter(0.1, 5.0, ev, iqr));
		solutions.add(new JitterParameter(5.0, 0.1, ev, iqr));
		Collections.sort(solutions);

		// while (solutions.get(2).getError() - solutions.get(0).getError() >
		// 0.001) {
		for (int c = 0; c < 100; c++) {
			JitterParameter newSolution = getNewParameter1(solutions, ev, iqr);
			if (newSolution != null && newSolution.getError() < solutions.get(0).getError()) {
				JitterParameter newSolution2 = getNewParameter2(solutions, ev, iqr);
				if (newSolution2 != null && newSolution2.getError() < newSolution.getError()) {
					solutions.remove(2);
					solutions.add(newSolution2);
				} else {
					solutions.remove(2);
					solutions.add(newSolution);
				}
			} else if (newSolution != null && newSolution.getError() < solutions.get(2).getError()) {
				solutions.remove(2);
				solutions.add(newSolution);
			} else {
				solutions.get(1).m = solutions.get(1).m + 0.5 * (solutions.get(0).m - solutions.get(1).m);
				solutions.get(2).m = solutions.get(2).m + 0.5 * (solutions.get(0).m - solutions.get(2).m);
				solutions.get(1).s = solutions.get(1).s + 0.5 * (solutions.get(0).s - solutions.get(1).s);
				solutions.get(2).s = solutions.get(2).s + 0.5 * (solutions.get(0).s - solutions.get(2).s);
			}
			Collections.sort(solutions);
		}
		return solutions.get(0);
	}

	private JitterParameter getNewParameter1(ArrayList<JitterParameter> solutions, double ew, double iqr) {
		double middleM = (solutions.get(0).m + solutions.get(1).m + solutions.get(2).m) / 3.0;
		double middleS = (solutions.get(0).s + solutions.get(1).s + solutions.get(2).s) / 3.0;
		double newM = middleM + (solutions.get(0).m - solutions.get(2).m);
		double newS = middleS + (solutions.get(0).s - solutions.get(2).s);
		if (newS > 0)
			return new JitterParameter(newM, newS, ew, iqr);
		else
			return null;
	}

	private JitterParameter getNewParameter2(ArrayList<JitterParameter> solutions, double ew, double iqr) {
		double middleM = (solutions.get(0).m + solutions.get(1).m + solutions.get(2).m) / 3.0;
		double middleS = (solutions.get(0).s + solutions.get(1).s + solutions.get(2).s) / 3.0;
		double newM = middleM + 2 * (solutions.get(0).m - solutions.get(2).m);
		double newS = middleS + 2 * (solutions.get(0).s - solutions.get(2).s);
		if (newS > 0)
			return new JitterParameter(newM, newS, ew, iqr);
		else
			return null;
	}

	private class JitterParameter implements Comparable<JitterParameter> {

		double m;

		double s;

		double ew;

		double iqr;

		public JitterParameter(double m, double s, double ew, double iqr) {
			this.m = m;
			this.s = s;
			this.ew = ew;
			this.iqr = iqr;
		}

		public double getError() {
			LognormalDist jitterDistribution = new LognormalDist(m, s);
			double error1 = Math.pow((iqr - (jitterDistribution.inverseF(0.75) - jitterDistribution.inverseF(0.25))) / iqr, 2);
			double error2 = Math.pow((ew - Math.exp(m + (Math.pow(s, 2) / 2.0))) / ew, 2);
			return error1 + error2;
		}

		public int compareTo(JitterParameter p) {
			double error1 = this.getError();
			double error2 = p.getError();
			if (error1 < error2)
				return -1;
			else if (error1 > error2)
				return 1;
			else
				return 0;
		}

		public double getAverageJitter() {
			return Math.exp(m + (Math.pow(s, 2) / 2.0));
		}

		public double getIQR() {
			LognormalDist jitterDistribution = new LognormalDist(m, s);
			return jitterDistribution.inverseF(0.75) - jitterDistribution.inverseF(0.25);
		}

		public String toString() {
			LognormalDist jitterDistribution = new LognormalDist(m, s);
			double iqr1 = jitterDistribution.inverseF(0.75) - jitterDistribution.inverseF(0.25);
			double ew1 = Math.exp(m + (Math.pow(s, 2) / 2.0));
			return "m: " + m + " s: " + s + " Error: " + getError() + " iqr: " + iqr1 + " ew: " + ew1;
		}
	}

}
