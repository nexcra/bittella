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

import static gods.experiment.AbstractChurnEventGenerator.generatorConstructorParameters;

import gods.churn.ArgumentGenerator;
import gods.churn.ArgumentGeneratorFactory;
import gods.experiment.events.JoinExperimentEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.PropertyConfigurator;

/**
 * The <code>ExperimentGenerator</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ExperimentGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println(args.length);
			System.out
					.println("usage: java gods.experiment.ExperimentGenerator"
							+ " <experiment.gen.xml> <gods.churn.arggens.xml>");
			System.exit(1);
		}

		PropertyConfigurator.configure(System
				.getProperty("org.apache.log4j.config.file"));

		try {
			ExperimentProperties.initialize(args[0]);
			ExperimentProperties.dump();

			// Create Experiment
			Experiment experiment = new Experiment();
			// Create Experiment Generator
			String eventGeneratorClassName = ExperimentProperties
					.getExperimentGeneratorClass();

			Class eventGeneratorClass = Class.forName(eventGeneratorClassName);

			AbstractChurnEventGenerator churnEventGen = (AbstractChurnEventGenerator) eventGeneratorClass
					.getConstructor(generatorConstructorParameters)
					.newInstance(ExperimentProperties.getNumberOfSlots(),
							ExperimentProperties.getSeed());

			// Set experiment parameters
			experiment.setNetModelPath(ExperimentProperties.getNetModelPath());
			experiment.setDeployScript(ExperimentProperties
					.getAppDeployScript());
			experiment.setInitScript(ExperimentProperties.getAppInitScript());

			experiment.setLaunchScript(ExperimentProperties
					.getAppLaunchScript());

			experiment.setCentralizedlogFile(ExperimentProperties
					.getExperimentLog());
			experiment.setValidationFile(ExperimentProperties
					.getValidationFile());
			experiment.setResultFile(ExperimentProperties.getResultFile());
			experiment.setAppRemoteLog(ExperimentProperties.getAppRemoteLog());

			// Generate experiment events
			experiment.create(churnEventGen, new TimeStamp(ExperimentProperties
					.getExperimentTime(), 0));

			// Generate arguments for application nodes
			if (ExperimentProperties.getAppArgGenDisplayName() != null) {
				System.out.println("ArgGen in Exp Properties is:"
						+ ExperimentProperties.getAppArgGenDisplayName() + ".");
				
				experiment.setArgGenDisplayName(ExperimentProperties
						.getAppArgGenDisplayName());
				experiment.setArgGenParamsFile(ExperimentProperties
						.getArgGenParamsFile());
			
				ArgumentGeneratorFactory.initialize(args[1]);
				ArgumentGenerator argGen = ArgumentGeneratorFactory
						.getArgumentGenerator(ExperimentProperties
								.getAppArgGenDisplayName(),
								ExperimentProperties.getArgGenParamsFile());
				argGen
						.setNumberOfInstances(churnEventGen
								.getJoinedNodesCount());

				String[] appArgs = argGen.generateArguments();
				for (int i = 0, j = 0; (i < experiment.getNumberOfEvents() && j < appArgs.length); i++) {

					if (experiment.get(i) instanceof JoinExperimentEvent) {
						JoinExperimentEvent joinEvent = (JoinExperimentEvent) experiment
								.get(i);
						joinEvent.setAppArgs(appArgs[j]);
						++j;
					}

				}
			}
			// Dump experiment on out
			experiment.dump();

			// Store experiment in a file
			File expDirectory = new File(ExperimentProperties
					.getExperimentHome());
			expDirectory.mkdirs();
			Experiment.store(ExperimentProperties.getExperimentFile(),
					experiment);

			// Load it again to check
			Experiment experiment2 = Experiment.load(ExperimentProperties
					.getExperimentFile());
			experiment2.dump();

			generateValidationFile(experiment);
			generateDiffsFile(experiment);

		} catch (PropertyNotFoundException pnpe) {
			System.out.println(pnpe.getMessage());

		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());

		} catch (ClassNotFoundException cnfe) {
			System.out.println(cnfe.getMessage());

		} catch (IllegalAccessException iae) {
			System.out.println(iae.getMessage());

		} catch (InstantiationException ie) {
			System.out.println(ie.getMessage());

		} catch (NoSuchMethodException nsme) {
			System.out.println(nsme.getMessage());

		} catch (InvocationTargetException ite) {
			System.out.println(ite.getMessage());

		} catch (ClassCastException cce) {
			System.out.println(cce.getMessage());

		}
	}

	private static void generateValidationFile(Experiment experiment) {
		try {

			BufferedWriter output = new BufferedWriter(new FileWriter(
					experiment.getValidationFile()));

			int i = 0;
			ExperimentEvent expEvent = null;

			while ((expEvent = experiment.get(i)) != null) {

				output.write(expEvent.toString() + "\n");
				++i;
			}

			output.flush();
			output.close();

		} catch (IOException ioe) {

			System.out.println(ioe.getMessage());
		}
	}

	private static void generateDiffsFile(Experiment experiment) {
		try {

			// FileWriter fw = new FileWriter();
			BufferedWriter output = new BufferedWriter(new FileWriter(
					experiment.getValidationFile() + ".diff"));

			int i = 0;
			ExperimentEvent expEvent = null;
			ExperimentEvent prevEvent = null;

			while ((expEvent = experiment.get(i)) != null) {

				output.write(expEvent.toString());
				++i;

				if (prevEvent != null) {
					TimeStamp diff = expEvent.getTimeToHappen().difference(
							prevEvent.getTimeToHappen());
					output.write(diff.toString());
				}
				output.write("\n");
				prevEvent = expEvent;
			}

			output.flush();
			output.close();

		} catch (IOException ioe) {

			System.out.println(ioe.getMessage());
		}
	}
}
