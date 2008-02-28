package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.math.stat.StatUtils;

import de.tud.kom.p2psim.impl.simengine.Simulator;

public class GnpSpace implements Comparable<GnpSpace>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6941552219570807190L;

	private GnpHostMap mapRef;

	private int noOfDimensions;

	private Hashtable<Long, GnpPosition> coordinateIndex;

	private GnpPosition[] monitorIndex2;

	private ArrayList<Double>[] distanceToRtt;

	public GnpSpace(int noOfDimensions, GnpHostMap mapRef) {
		super();
		coordinateIndex = new Hashtable<Long, GnpPosition>(1000000);
		this.mapRef = mapRef;
		this.noOfDimensions = noOfDimensions;

		// this.monitorIndex = new Hashtable<Long,Coordinate>();
		this.monitorIndex2 = new GnpPosition[mapRef.getNoOfMonitors()];

		Hashtable<Long, GnpHost> monitors = mapRef.getMonitorIndex();
		Set<Long> ips = monitors.keySet();
		int c = 0;
		for (Long ip : ips) {
			monitorIndex2[c] = new GnpPosition(noOfDimensions, monitors.get(ip), this);
			c++;
		}
	}

	public static GnpSpace getGnpWithDownhillSimplex(int noOfDimensions, int param1, double param2, GnpHostMap mapRef) {

		double alpha = 1.0;
		double beta = 0.5;
		double gamma = 2;
		double maxDiversity = 0.5;

		long start = System.currentTimeMillis();

		// dhs: Parameter N
		int dhs_N = mapRef.getNoOfMonitors();

		// dhs: n + 1 beginning Solutions
		ArrayList<GnpSpace> solutions = new ArrayList<GnpSpace>(dhs_N + 1);
		for (int c = 0; c < dhs_N + 1; c++)
			solutions.add(new GnpSpace(noOfDimensions, mapRef));

		// dhs: best and worst Solutions
		GnpSpace bestSolution = Collections.min(solutions);
		GnpSpace worstSolution = Collections.max(solutions);
		double bestError = bestSolution.getObjectiveValueMonitor();
		double worstError = worstSolution.getObjectiveValueMonitor();

		for (int z = 0; z < param1; z++) { // Z ist ein wichtiger parameter,
											// wichtiger als kleine differenz
											// zwischen best und worst

			int count = 0;
			for (GnpSpace gnp : solutions) {
				if (gnp != bestSolution) {
					gnp.diversifyMonitor(count, maxDiversity);
					count++;
				}
			}

			bestSolution = Collections.min(solutions);
			worstSolution = Collections.max(solutions);
			bestError = bestSolution.getObjectiveValueMonitor();
			worstError = worstSolution.getObjectiveValueMonitor();

			// dhs: Abbruchkriterium
			while (worstError - bestError > param2) {
				// for (int xy=0; xy<100; xy++) {

				GnpSpace center = GnpSpace.getCenterSolution(solutions);
				GnpSpace newSolution1 = GnpSpace.getMovedSolution(worstSolution, center, 1 + alpha);
				double newError1 = newSolution1.getObjectiveValueMonitor();

				// wenn neuer Punkt ist immerhin Besser als bisher schlechteste
				// Lšsung, dann ersetze Schelchteste Lšsung
				if (newError1 <= bestError) {
					int IndexOfWorstSolution = solutions.indexOf(worstSolution);
					GnpSpace newSolution2 = GnpSpace.getMovedSolution(worstSolution, center, 1 + alpha + gamma);
					double newError2 = newSolution2.getObjectiveValueMonitor();
					if (newError2 <= newError1) {
						solutions.set(IndexOfWorstSolution, newSolution2);
						bestError = newError2;
					} else {
						solutions.set(IndexOfWorstSolution, newSolution1);
						bestError = newError1;
					}
					bestSolution = solutions.get(IndexOfWorstSolution);

				} else if (newError1 < worstError) {
					int IndexOfWorstSolution = solutions.indexOf(worstSolution);
					solutions.set(IndexOfWorstSolution, newSolution1);
				} else {
					for (int c = 0; c < solutions.size(); c++) {
						if (solutions.get(c) != bestSolution)
							solutions.set(c, GnpSpace.getMovedSolution(solutions.get(c), bestSolution, beta));
					}
					bestSolution = Collections.min(solutions);
					bestError = bestSolution.getObjectiveValueMonitor();
				}

				worstSolution = Collections.max(solutions);
				worstError = worstSolution.getObjectiveValueMonitor();
			}
		}

		// Set the Coordinate Reference to the Peer
		for (int c = 0; c < bestSolution.getNumberOfMonitors(); c++) {
			bestSolution.getMonitor(c).getPeerRef().setCoordinateReference(bestSolution.getMonitor(c));
		}

		long stop = System.currentTimeMillis();
		System.out.println("Calculation Time GNP Monitors in seconds: " + ((stop - start) / 1000.0));

		return bestSolution;
	}

	public void insertCoordinates(int param1, double param2) {

		NumberFormat f = NumberFormat.getNumberInstance();
		f.setMaximumFractionDigits(1);
		f.setMinimumFractionDigits(1);

		coordinateIndex.clear();

		Hashtable<Long, GnpHost> peers = this.getMapRef().getPeerIndex();
		Set<Long> ips = peers.keySet();
		int c = 0;
		int d = 0;
		long start = System.currentTimeMillis();
		long current = start;
		for (Long ip : ips) {

			if (peers.get(ip).getPeerType() == GnpHost.MONITOR)
				continue;

			GnpPosition coord = this.insertCoordinateDownhillSimplex(peers.get(ip), param1, param2);
			coordinateIndex.put(ip, coord);
			c++;
			if (c % 1000 == 0) {
				d++;
				current = System.currentTimeMillis();
				double timePerPeer = (current - start) / (double) c;

				System.out.println("Minutes to End:" + Math.floor((timePerPeer * (peers.size() - c)) / (1000 * 60)));
			}

		}
	}

	public GnpPosition insertCoordinateDownhillSimplex(GnpHost peer, int param1, double param2) {

		double alpha = 1.0;
		double beta = 0.5;
		double gamma = 2;
		double maxDiversity = 0.5;

		ArrayList<GnpPosition> solutions = new ArrayList<GnpPosition>(noOfDimensions + 1);

		for (int c = -1; c < noOfDimensions; c++) {
			GnpPosition coord = new GnpPosition(noOfDimensions, peer, this);
			// coord.setCoordinatesToOne();
			// coord.setCoordinateToZero(c);
			solutions.add(coord);
		}

		GnpPosition bestSolution = Collections.min(solutions);
		GnpPosition worstSolution = Collections.max(solutions);
		double bestError = bestSolution.getError();
		double worstError = worstSolution.getError();

		double newError = 0.0;

		for (int z = 0; z < param1; z++) {

			for (GnpPosition coord : solutions) {
				if (coord != bestSolution) {
					coord.diversify(this.getDimension(), maxDiversity);
				}
			}
			bestSolution = Collections.min(solutions);
			worstSolution = Collections.max(solutions);
			bestError = bestSolution.getError();
			worstError = worstSolution.getError();

			// dhs: Abbruchkriterium
			while (worstError - bestError > param2) {

				GnpPosition center = GnpPosition.getCenterSolution(solutions);
				GnpPosition newSolution1 = GnpPosition.getMovedSolution(worstSolution, center, 1 + alpha);

				newError = newSolution1.getError();

				if (newError <= bestError) {
					GnpPosition newSolution2 = GnpPosition.getMovedSolution(worstSolution, center, 1 + alpha + gamma);
					int IndexOfWorstSolution = solutions.indexOf(worstSolution);
					if (newSolution2.getError() <= newError) {
						solutions.set(IndexOfWorstSolution, newSolution2);
					} else {
						solutions.set(IndexOfWorstSolution, newSolution1);
					}
					bestSolution = solutions.get(IndexOfWorstSolution);
					bestError = bestSolution.getError();

				}

				else if (newError < worstError) {
					int IndexOfWorstSolution = solutions.indexOf(worstSolution);
					solutions.set(IndexOfWorstSolution, newSolution1);

				} else {
					for (int c = 0; c < solutions.size(); c++) {
						if (solutions.get(c) != bestSolution)
							solutions.set(c, GnpPosition.getMovedSolution(solutions.get(c), bestSolution, beta));
					}
					bestSolution = Collections.min(solutions);
					bestError = bestSolution.getError();
				}
				worstSolution = Collections.max(solutions);
				worstError = worstSolution.getError();
			}
		}

		peer.setCoordinateReference(bestSolution);

		return bestSolution;

	}

	public int getNumberOfMonitors() {
		return monitorIndex2.length;
		// return noOfMonitors;
	}

	/*
	 * public Coordinate getMonitor(long monitorIP) { return
	 * monitorIndex.get(monitorIP); }
	 */

	public GnpPosition getCoordinate(long peerIP) {
		return coordinateIndex.get(peerIP);
	}

	/*
	 * public void setMonitor(long monitorIP, Coordinate monitor) {
	 * monitorIndex.put(monitorIP, monitor); }
	 */

	public void setMonitor(int monitorIndex, GnpPosition monitor) {
		monitorIndex2[monitorIndex] = monitor;
	}

	public GnpPosition getMonitor(int monitorIndex) {
		return monitorIndex2[monitorIndex];
	}

	public GnpHostMap getMapRef() {
		return mapRef;
	}

	public double getObjectiveValueMonitor() {
		double value = 0.0;
		for (int i = 0; i < this.getNumberOfMonitors() - 1; i++) {
			for (int j = i + 1; j < this.getNumberOfMonitors(); j++) {
				value = value + monitorIndex2[i].getError(monitorIndex2[j]);
			}
		}
		return value;
	}

	public void printDiff() {

		NumberFormat f = NumberFormat.getNumberInstance();
		f.setMaximumFractionDigits(1);
		f.setMinimumFractionDigits(1);

		double value = 0.0;

		for (int i = 0; i < this.getNumberOfMonitors() - 1; i++) {
			for (int j = i + 1; j < this.getNumberOfMonitors(); j++) {
				double calc = monitorIndex2[i].getCalculatedRtt(monitorIndex2[j]);
				double meas = monitorIndex2[i].getMeasuredRtt(monitorIndex2[j]);
				double error = monitorIndex2[i].getError(monitorIndex2[j]);
				System.out.println("Monitor " + i + " -> " + j + "\tCalculated: " + f.format(calc) + "\tMeasured: " + f.format(meas) + "\t => Diff: " + f.format((meas - calc)) + "\tError: " + error);
				value = value + error;
			}
		}
		System.out.println("Sum Error: " + value);

	}

	public void diversifyMonitor(int monitorID, double maxDiversity) {
		this.monitorIndex2[monitorID].diversify(getDimension(), maxDiversity);
	}

	public double[][] getDimension() {

		double[][] returnvalue = new double[this.noOfDimensions][3];
		double min;
		double max;
		for (int c = 0; c < this.noOfDimensions; c++) {
			min = this.monitorIndex2[0].getGnpCoordinates(c);
			max = min;
			for (int d = 1; d < this.getNumberOfMonitors(); d++) {
				double current = this.monitorIndex2[d].getGnpCoordinates(c);
				min = (min < current) ? min : current;
				max = (max > current) ? max : current;
			}
			returnvalue[c][0] = min;
			returnvalue[c][1] = max;
			returnvalue[c][2] = max - min;
		}
		return returnvalue;

	}

	public int getNoOfDimensions() {
		return this.noOfDimensions;
	}

	private static GnpSpace getCenterSolution(ArrayList<GnpSpace> solutions) {
		GnpSpace returnValue = new GnpSpace(solutions.get(0).getNoOfDimensions(), solutions.get(0).getMapRef());
		for (int c = 0; c < returnValue.getNumberOfMonitors(); c++) {
			ArrayList<GnpPosition> coords = new ArrayList<GnpPosition>();
			for (int d = 0; d < solutions.size(); d++) {
				coords.add(solutions.get(d).getMonitor(c));
			}
			returnValue.setMonitor(c, GnpPosition.getCenterSolution(coords));
		}

		return returnValue;
	}

	private static GnpSpace getMovedSolution(GnpSpace solution, GnpSpace moveToSolution, double moveFactor) {
		GnpSpace returnValue = new GnpSpace(solution.getNoOfDimensions(), solution.getMapRef());
		for (int c = 0; c < returnValue.getNumberOfMonitors(); c++) {
			returnValue.setMonitor(c, GnpPosition.getMovedSolution(solution.getMonitor(c), moveToSolution.getMonitor(c), moveFactor));
		}
		return returnValue;
	}

	public int compareTo(GnpSpace gnp) {

		double val1 = this.getObjectiveValueMonitor();
		double val2 = gnp.getObjectiveValueMonitor();

		if (val1 < val2)
			return -1;
		if (val1 > val2)
			return 1;
		else
			return 0;

	}

	public String toString() {
		String returnValue = "";
		for (int c = 0; c < getNumberOfMonitors(); c++) {
			returnValue += "Monitor " + c + ": " + monitorIndex2[c] + "\n";
		}
		returnValue += this.getObjectiveValueMonitor();
		return returnValue;
	}



	public void removeCoordinate(long peerIP) {
		this.coordinateIndex.remove(peerIP);
	}



	// error[0][x] => measured rtt
	// error[1][x] => calculated rtt
	// error[2][x] => directional relativ error
	// error[3][x] => relativ error
	public double[][] getPopulationErrorData() {

		int numberOfConnetions = getNumberOfMonitors() * coordinateIndex.size();

		double[][] result = new double[4][numberOfConnetions];

		int x = 0;

		Set<Long> ips = coordinateIndex.keySet();
		for (Long ip : ips) {
			for (GnpPosition monitor : monitorIndex2) {
				if (monitor.getPeerRef() == coordinateIndex.get(ip).getPeerRef()) {
					System.out.println(x);
					continue;
				}
				result[0][x] = coordinateIndex.get(ip).getMeasuredRtt(monitor);
				result[1][x] = coordinateIndex.get(ip).getCalculatedRtt(monitor);
				result[2][x] = coordinateIndex.get(ip).getDirectionalRelativError(monitor);
				result[3][x] = Math.abs(result[2][x]);
				x++;
			}
		}
		System.out.println(x);

		return result;
	}

}
