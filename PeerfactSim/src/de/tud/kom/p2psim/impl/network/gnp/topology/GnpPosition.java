package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.io.Serializable;
import java.util.ArrayList;

import de.tud.kom.p2psim.api.network.NetPosition;
import de.tud.kom.p2psim.impl.simengine.Simulator;

public class GnpPosition implements NetPosition, Serializable, Comparable<GnpPosition> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1103996725403557900L;

	private double[] gnpCoordinates;

	private GnpSpace gnpRef;

	private GnpHost peerRef;

	private double error = -1.0;

	public GnpPosition(double[] gnpCoordinates) {
		super();
		this.gnpCoordinates = gnpCoordinates;
	}

	public GnpPosition(int noOfDimensions, GnpHost peerRef, GnpSpace gnpRef) {
		super();
		gnpCoordinates = new double[noOfDimensions];
		this.setPeerRef(peerRef);
		this.gnpRef = gnpRef;
		for (int c = 0; c < gnpCoordinates.length; c++)
			gnpCoordinates[c] = Simulator.getRandom().nextDouble();
	}

	public GnpPosition(int noOfDimensions, GnpHost peerRef, GnpSpace gnpRef, double[][] dimension) {
		super();
		gnpCoordinates = new double[noOfDimensions];
		this.setPeerRef(peerRef);
		this.gnpRef = gnpRef;
		for (int c = 0; c < gnpCoordinates.length; c++)
			gnpCoordinates[c] = dimension[c][0] + (Math.random() * dimension[c][2]);
	}

	/*
	 * public void setQuality(int quality) { this.quality = quality; }
	 */

	public void diversify(double[][] dimension, double maxDiversity) {
		for (int c = 0; c < this.gnpCoordinates.length; c++) {
			double rand = (2 * maxDiversity * Math.random()) - maxDiversity;
			gnpCoordinates[c] = gnpCoordinates[c] + (rand * dimension[c][2]);
		}
		error = -1.0;
	}

	public GnpSpace getGnpRef() {
		return gnpRef;
	}

	public void setCoordinatesToOne() {
		for (int c = 0; c < gnpCoordinates.length; c++)
			gnpCoordinates[c] = 200;
		error = -1.0;
	}

	public void setCoordinateToZero(int coordinate) {
		if (coordinate >= 0)
			gnpCoordinates[coordinate] = 0;
		error = -1.0;
	}

	public void setPeerRef(GnpHost peerRef) {
		this.peerRef = peerRef;
	}

	public double getGnpCoordinates(int pos) {
		return gnpCoordinates[pos];
	}

	public void setGnpCoordinates(int pos, double value) {
		gnpCoordinates[pos] = value;
		error = -1.0;
	}

	public GnpHost getPeerRef() {
		return peerRef;
	}

	public int getNoOfDimensions() {
		return gnpCoordinates.length;
	}

	public double getError() {
		if (this.error >= 0.0 && this.peerRef.getPeerType() == GnpHost.PEER)
			return this.error;
		double val1 = 0.0;
		for (int c = 0; c < getGnpRef().getNumberOfMonitors(); c++) {
			double error = getError(getGnpRef().getMonitor(c));
			val1 += error;
		}
		this.error = val1;
		return val1;
	}

	public double getError(GnpPosition monitor) {
		double calculatedDistance = this.getCalculatedRtt(monitor);
		double measuredDistance = this.getMeasuredRtt(monitor);
		if (Double.compare(measuredDistance, Double.NaN) == 0 || measuredDistance == 0.0)
			return 0;
		double error = Math.pow((calculatedDistance - measuredDistance) / calculatedDistance, 2);
		return error;
	}

	public double getDirectionalRelativError(GnpPosition monitor) {
		double calculatedDistance = this.getCalculatedRtt(monitor);
		double measuredDistance = this.getMeasuredRtt(monitor);
		if (Double.compare(measuredDistance, Double.NaN) == 0)
			return Double.NaN;
		double error = (calculatedDistance - measuredDistance) / Math.min(calculatedDistance, measuredDistance);
		return error;
	}

	public double[] getDirectionalRelativErrors() {
		double[] error = new double[getGnpRef().getNumberOfMonitors()];
		for (int c = 0; c < getGnpRef().getNumberOfMonitors(); c++) {
			error[c] = getDirectionalRelativError(getGnpRef().getMonitor(c));
		}
		return error;
	}

	public String toString() {
		String returnValue = "";
		for (int c = 0; c < gnpCoordinates.length; c++) {
			returnValue += gnpCoordinates[c] + "\t";
		}
		return returnValue;
	}

	public static GnpPosition getMovedSolution(GnpPosition solution, GnpPosition moveToSolution, double moveFactor) {

		GnpPosition returnValue = new GnpPosition(solution.getNoOfDimensions(), solution.getPeerRef(), solution.getGnpRef());

		for (int c = 0; c < solution.getNoOfDimensions(); c++) {
			returnValue.setGnpCoordinates(c, (moveToSolution.getGnpCoordinates(c) - solution.getGnpCoordinates(c)) * moveFactor + solution.getGnpCoordinates(c));
		}

		return returnValue;
	}

	public static GnpPosition getCenterSolution(ArrayList<GnpPosition> solutions) {
		GnpPosition returnValue = new GnpPosition(solutions.get(0).getNoOfDimensions(), solutions.get(0).getPeerRef(), solutions.get(0).getGnpRef());
		for (int d = 0; d < solutions.size(); d++) {
			for (int c = 0; c < solutions.get(0).getNoOfDimensions(); c++) {
				returnValue.setGnpCoordinates(c, returnValue.getGnpCoordinates(c) + solutions.get(d).getGnpCoordinates(c));
			}
		}
		for (int c = 0; c < returnValue.getNoOfDimensions(); c++) {
			returnValue.setGnpCoordinates(c, returnValue.getGnpCoordinates(c) / solutions.size());
		}
		return returnValue;
	}

	public int compareTo(GnpPosition arg0) {

		double val1 = this.getError();
		double val2 = arg0.getError();

		if (val1 < val2)
			return -1;
		if (val1 > val2)
			return 1;
		else
			return 0;

	}

	/**
	 * 
	 * @return Comma-separated List of Coordinates
	 */
	public String getCoordinateString() {
		String result = String.valueOf(gnpCoordinates[0]);
		for (int c = 1; c < gnpCoordinates.length; c++)
			result = result + "," + gnpCoordinates[1];
		return result;
	}

	public double getMeasuredRtt(GnpPosition monitor) {
		return this.getPeerRef().getRtt(monitor.getPeerRef().getIpAddress());
	}

	public double getCalculatedRtt(GnpPosition coord) {
		return getDistance(coord);
	}

	public double getDistance(NetPosition point) {
		GnpPosition coord = (GnpPosition) point;
		double distance = 0.0;
		for (int c = 0; c < gnpCoordinates.length; c++)
			distance += Math.pow(gnpCoordinates[c] - coord.getGnpCoordinates(c), 2);
		return Math.sqrt(distance);
	}

}
