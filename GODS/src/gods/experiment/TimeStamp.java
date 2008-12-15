/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment;

import java.io.Serializable;

/**
 * The <code>TimeStamp</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class TimeStamp implements Comparable<TimeStamp>, Serializable,
		Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 593307222913722477L;

	/**
	 * 
	 */
	private long seconds;

	/**
	 * 
	 */
	private int microseconds;

	/**
	 * 
	 */
	private boolean positive;

	/**
	 * Default constructor
	 */
	public TimeStamp() {
		positive = true;
		seconds = 0;
		microseconds = 0;
	}

	/**
	 * @param seconds
	 * @param microseconds
	 */
	public TimeStamp(long seconds, int microseconds) {

		if (microseconds < 0) {
			seconds -= 1;
			microseconds += 1000000;
		}

		if (seconds < 0) {
			positive = false;
		} else {
			positive = true;
		}

		this.seconds = seconds;
		this.microseconds = microseconds;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TimeStamp o) {

		int result = 0;

		if (positive && o.positive) {
			result = absCompareTo(this, o);
		} else if (!positive && !o.positive) {
			result = (-1) * absCompareTo(this, o);
		} else if (!positive) {
			result = -1;
		} else {
			result = 1;
		}

		return result;
	}

	private static int absCompareTo(TimeStamp p, TimeStamp q) {
		int result = 0;

		if (p.seconds < q.seconds) {
			result = -1;
		} else if (p.seconds > q.seconds) {
			result = 1;
		} else if (p.microseconds < q.microseconds) {
			result = -1;
		} else if (p.microseconds > q.microseconds) {
			result = 1;
		}

		return result;
	}

	/**
	 * @return
	 */
	public TimeStamp difference(TimeStamp o) {

		TimeStamp diff = null;

		/*
		 * e.g., 4 -(+3) OR 3 -(+4) r = 1 OR -1
		 */
		/*
		 * e.g., (-3)-(-4) = (-3) + 4 OR (-3)-(-2) = (-3) + 2 r = 1 OR -1
		 */
		if (positive == o.positive) {
			diff = absdiff(this, o);
			/*
			 * e.g., 3 -(+4) r = -1
			 */
			if (positive && absCompareTo(this, o) < 0) {
				diff.positive = false;
			}
			/*
			 * e.g., (-3)-(-2) = (-3) + 2 r = -1
			 */
			else if (!positive && absCompareTo(this, o) > 0) {
				diff.positive = false;
			}
		}

		/*
		 * e.g., 3-(-4) = 3 + 4 OR (-3) - 4 = -3 - 4 r = 7 OR -7
		 */
		else {
			diff = unsignedAdd(o, this);
			diff.positive = positive;
		}

		return diff;
	}

	/**
	 * Adds TimeStamp o into this TimeStamp
	 * 
	 * @param o
	 */
	public void add(TimeStamp o) {

		/*
		 * e.g., 3 + 4 OR (-3) + (-4) = -3 - 4 r = 7 OR -7
		 */
		if (positive == o.positive) {
			TimeStamp r = unsignedAdd(this, o);
			seconds = r.seconds;
			microseconds = r.microseconds;
		}

		else {
			TimeStamp r = null;
			/*
			 * e.g., (-3) + 4 OR (-3) + 2 r = 1 OR -1
			 */
			if (!positive) {
				r = absdiff(o, this);
				if (absCompareTo(this, o) > 0) {
					r.positive = false;
				}
			}
			/*
			 * e.g., 3 + (-2) OR 3 + (-4) r = 1 OR -1
			 */
			else {
				r = absdiff(o, this);
				if (absCompareTo(this, o) < 0) {
					r.positive = false;
				}
			}

			seconds = r.seconds;
			microseconds = r.microseconds;
			positive = r.positive;
		}

	}

	/**
	 * @return absolute of difference between the two timestamp
	 */
	private static TimeStamp absdiff(TimeStamp p, TimeStamp q) {

		TimeStamp timediff = new TimeStamp(0, 0);

		timediff.seconds = p.seconds - q.seconds;
		timediff.microseconds = p.microseconds - q.microseconds;
		if (timediff.microseconds < 0) {
			timediff.seconds -= 1;
			timediff.microseconds += 1000000;
		}
		if (timediff.seconds < 0) {
			timediff.seconds = 0 - timediff.seconds;
		}

		return timediff;
	}

	/**
	 * @param p
	 *            The TimeStamp to be added to TimeStamp q
	 * @param q
	 *            The TimeStamp to be added to TimeStamp p
	 * @return TimeStamp that is the sum of both timestamps without considering
	 *         their signs
	 */
	private static TimeStamp unsignedAdd(TimeStamp p, TimeStamp q) {
		TimeStamp r = new TimeStamp();
		r.seconds = p.seconds + q.seconds;
		r.microseconds = p.microseconds + q.microseconds;
		if (r.microseconds >= 1000000) {
			r.seconds += 1;
			r.microseconds -= 1000000;
		}
		return r;
	}

	/**
	 * @return the microseconds
	 */
	public int getMicroseconds() {
		return microseconds;
	}

	/**
	 * @return the milliseconds
	 */
	public int getMilliseconds() {
		return microseconds / 1000;
	}

	/**
	 * @return factors microseconds into millis and nanos and returns the latter
	 */
	public int getNanoseconds() {
		return (microseconds % 1000) * 1000;
	}

	/**
	 * @param microseconds
	 *            the microseconds to set
	 */
	public void setMicroseconds(int microseconds) {
		this.microseconds = microseconds;
	}

	/**
	 * @return the seconds
	 */
	public long getSeconds() {
		return seconds;
	}

	/**
	 * @param seconds
	 *            the seconds to set
	 */
	public void setSeconds(long seconds) {
		this.seconds = seconds;
	}

	/** TimeStamp in String format
	 * The formatted string is in the form "%12d.%06d"
	 * @see java.lang.String#format
	 */
	public String toString() {
		if (positive) {
			return String.format("%12d.%06d", seconds, microseconds);
		}
		return String.format("%11d.%06d", (0 - seconds), microseconds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * @return the positive
	 */
	public boolean isPositive() {
		return positive;
	}
}
