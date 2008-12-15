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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The <code>Validator</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class Validator {

	public static boolean validate(String logFileName, String valFileName,
			String resFileName) throws FileNotFoundException, IOException {

		boolean result = true;

		BufferedReader logFile = new BufferedReader(new FileReader(logFileName));
		BufferedReader valFile = new BufferedReader(new FileReader(valFileName));

		BufferedWriter resFile = new BufferedWriter(new FileWriter(resFileName));

		String logLine = null, valLine = null;
		String splitter = "[ \t.]+";

		TimeStamp logTimeStamp = new TimeStamp();
		TimeStamp valTimeStamp = new TimeStamp();
		TimeStamp totalDiff = new TimeStamp();
		TimeStamp diff = null;
		int events = 0;
		
		TimeStamp minDiff = new TimeStamp();
		TimeStamp maxDiff = new TimeStamp();
		
		resFile.write("Logged Time" + "\t"
				+ "Validation Time" + "\t" + "Difference"
				+ "\t" + "Total Difference" + "\n");

		while ((logLine = logFile.readLine()) != null
				&& (valLine = valFile.readLine()) != null) {

			// Log Event
			String[] logEvent = logLine.split(splitter);
			long logSeconds = Long.parseLong(logEvent[1]);
			int logMicroseconds = Integer.parseInt(logEvent[2]);
			logTimeStamp.setSeconds(logSeconds);
			logTimeStamp.setMicroseconds(logMicroseconds);

			int logNodeId = Integer.parseInt(logEvent[3]);
			String logEventType = logEvent[4];

			// Val Event
			String[] valEvent = valLine.split(splitter);
			long valSeconds = Long.parseLong(valEvent[1]);
			int valMicroseconds = Integer.parseInt(valEvent[2]);
			valTimeStamp.setSeconds(valSeconds);
			valTimeStamp.setMicroseconds(valMicroseconds);

			int valNodeId = Integer.parseInt(valEvent[3]);
			String valEventType = logEvent[4];

			// Comparison
			if (logEventType == valEventType) {

				if (logNodeId == valNodeId) {

					diff = logTimeStamp.difference(valTimeStamp);
					totalDiff.add(diff);
					resFile.write(logTimeStamp.toString() + "\t"
							+ valTimeStamp.toString() + "\t" + diff.toString()
							+ "\t" + totalDiff.toString() + "\n");
					if(minDiff.compareTo(diff) > 0 ){
						minDiff = diff;
					}
					if(maxDiff.compareTo(diff) < 0){
						maxDiff = diff;
					}
				} else {
					result = false;
					resFile.write("Nodeid in validation file at Val Time: "
									+ valTimeStamp.toString() + " is "
									+ valNodeId + " and in log file is "
									+ logNodeId);
					break;
				}

			} else {
				result = false;
				resFile.write("Event Type in validation file at Val Time: "
								+ valTimeStamp.toString() + " is "
								+ valEventType + " and in log file is "
								+ logEventType);
				break;
			}
			++events;
		}
		resFile.write("Total Difference is: " + totalDiff.toString()
				+ " seconds\n");

		resFile.write("Minimum Difference is: " + minDiff.toString()
				+ " seconds\n");
		
		resFile.write("Maximum Difference is: " + maxDiff.toString()
				+ " seconds\n");
		
		float total = totalDiff.getSeconds()
				+ (float) totalDiff.getMicroseconds() / (float) 1000000;

		float avgDiff = total / events;
		resFile.write("Average Difference is: \t" + avgDiff + " seconds\n");
		
		resFile.flush();
		resFile.close();
		
		return result;
	}

	public static void main(String[] args) {

		try {
			String logfile = "/home/ozair/workspace/gods-www/experiments/dummy.100/dummy.100.log";
			String valfile = "/home/ozair/workspace/gods-www/experiments/dummy.100/dummy.100.val";
			String resFile = "/home/ozair/workspace/gods-www/experiments/dummy.100/dummy.100.res";

			validate(logfile, valfile, resFile);

		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe.getMessage());

		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());

		}

	}

}
