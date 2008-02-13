package de.tud.kom.p2psim.impl.simengine.queues;

import de.tud.kom.p2psim.api.simengine.EventQueue;
import de.tud.kom.p2psim.api.simengine.SimulationEvent;

/**
 * Implements an array-based heap scheduler for storing events.
 */
//DOCUMENT
final class Heap implements EventQueue {
	/**
	 * Internal array of heap items.
	 */
	private SimulationEvent[] items;

	/**
	 * Number of elements in heap.
	 */
	private int size;

	/**
	 * Collapse size.
	 */
	private int collaps;

	/**
	 * Create a new, empty heap with given initial heap length.
	 * 
	 * @param length
	 *            initial heap length.
	 */
	public Heap(int length) {
		this.items = new SimulationEvent[length];
		this.size = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public void reset() {
		while (!this.empty()) {
			this.remove();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return this.size;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean more() {
		return this.size > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean empty() {
		return this.size == 0;
	}

	/**
	 * Halve the capacity of the internal array.
	 */
	private void decrease() {
		int newLength = this.items.length / 2;
		if (newLength < Calendar.HEAP_LENGTH) {
			this.collaps = 0;
			return;
		}
		SimulationEvent[] array = new SimulationEvent[newLength];
		System.arraycopy(this.items, 0, array, 0, this.size);
		this.collaps = this.items.length / 4;
		this.items = array;
	}

	/**
	 * Double the capacity of the internal array.
	 */
	private void increase() {
		SimulationEvent[] array = new SimulationEvent[this.items.length * 2];
		System.arraycopy(this.items, 0, array, 0, this.size);
		this.collaps = this.items.length / 4;
		this.items = array;
	}

	/**
	 * Establish downward order of heap array starting at given index location.
	 * 
	 * @param index
	 *            start index of heap array.
	 */
	private void heapify(int index) {
		while (true) {
			int left = (index * 2) + 1;
			int right = left + 1;
			int largest;
			if ((left < this.size) && (this.items[left].getSimulationTime() < this.items[index].getSimulationTime())) {
				largest = left;
			} else {
				largest = index;
			}
			if ((right < this.size) && (this.items[right].getSimulationTime() < this.items[largest].getSimulationTime())) {
				largest = right;
			}
			if (largest == index) {
				return;
			}
			SimulationEvent item = this.items[index];
			this.items[index] = this.items[largest];
			this.items[index = largest] = item;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void insert(SimulationEvent event) {
		SimulationEvent item;
		if (this.size == this.items.length) {
			// Heap.stream.println("i " + this.items[0].time + " " +
			// event.time + " " + this.hashCode()
			// + " " + this.items.length);
			this.increase();
		}
		int index = this.size++, parent;
		while ((index > 0) && (event.getSimulationTime() < (item = this.items[parent = (index - 1) / 2]).getSimulationTime())) {
			this.items[index] = item;
			index = parent;
		}
		this.items[index] = event;
	}

	/**
	 * {@inheritDoc}
	 */
	public SimulationEvent remove() {
		SimulationEvent event = this.items[0];
		if (--this.size != 0) {
			this.items[0] = this.items[this.size];
			this.heapify(0);
			if (this.size < this.collaps) {
				this.decrease();
				// Heap.stream.println("i " + this.items[0].time + " " +
				// event.time + " " +
				// this.hashCode()
				// + " " + this.items.length);
			}
		}
		this.items[this.size] = null;
		return event;
	}

	/**
	 * {@inheritDoc}
	 */
	public SimulationEvent peek() {
		return this.items[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public void verify() {
		for (int index = 0; index < this.size; index++) {
			int left = (index + 1) * 2 - 1, right = (index + 1) * 2;
			if ((left < this.size) && !(this.items[index].getSimulationTime() <= this.items[left].getSimulationTime())) {
				throw new RuntimeException("error: verify (" + index + " >= " + left + ")");
			}
			if ((right < this.size) && !(this.items[index].getSimulationTime() <= this.items[right].getSimulationTime())) {
				throw new RuntimeException("error: verify (" + index + " >= " + right + ")");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder("heap");
		if (this.size != 0) {
			builder.append(" ");
			builder.append(this.size);
			for (int index = 0; index < this.size; index++) {
				builder.append(" ");
				builder.append(this.items[index].getSimulationTime());
			}
		}
		return builder.toString();
	}

	public boolean remove(SimulationEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}