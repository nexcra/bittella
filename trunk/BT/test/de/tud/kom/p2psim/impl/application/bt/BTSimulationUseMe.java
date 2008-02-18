package de.tud.kom.p2psim.impl.application.bt;

import de.tud.kom.p2psim.impl.simengine.Simulator;

public class BTSimulationUseMe {
/*
    public static void main(String[] args) {
            String filename = "D:\\Investigacion\\Bittorrent\\BT_simulaciones\\Resultados\\BA_SimResult." + (System.currentTimeMillis() / 1000) + ".txt";
            long fileSize = 50 * 1024 * 1024; //Size of the file in byte.
            int numberOfLeecher = 50; //Number of the leecher.
            int numberOfSpecialLeecher = 0; //Number of modified leechers.
            long duration = 1 * 24 * Simulator.HOUR_UNIT; //Maximum simulated time.
            long seederDuration = 1 * 6 * Simulator.HOUR_UNIT; //How long the seeder stays online.
            long leecherDuration = 1 * Simulator.MINUTE_UNIT; //How long leecher stay online after their download is finished.
            long startWindow = Simulator.MINUTE_UNIT; //In this time window, all leecher get started.
            int seederUpload =  300 * (1024/8), seederDownload = 1000 * (1024/8), leecherUpload = 300 * (1024/8), leecherDownload = 1000 * (1024/8); //The Bandwith in bytes per second.
            BTSimulation.runSimulation(filename, fileSize, numberOfLeecher, numberOfSpecialLeecher,duration, seederDuration, leecherDuration, startWindow, seederUpload, seederDownload, leecherUpload, leecherDownload);
    }
  */      
        	public static void main(String[] args) {
		String filename = "/tmp/BA_SimResult." + (System.currentTimeMillis() / 1000) + ".txt";
		long fileSize = 10 * 1024 * 1024; //Size of the file in byte.
	        int numberOfLeechers = 50; //Number of the leecher.
		long duration = 1 * 24 * Simulator.HOUR_UNIT; //Maximum simulated time.
	        long seederDuration = 1 * 1 * Simulator.HOUR_UNIT; //How long the seeder stays online.
		long leecherDuration = 1 * Simulator.MINUTE_UNIT; //How long leecher stay online after their download is finished.
		long startWindow = 10*Simulator.MINUTE_UNIT; //In this time window, all leecher get started.
                int numberOfLeechersType = 4;
                
                int numberOfTypeILeechers = (int) (0.5* numberOfLeechers);
                int numberOfTypeIILeechers = (int) (0.5* numberOfLeechers);
                int numberOfTypeIIILeechers = (int) (0.0* numberOfLeechers);
                int numberOfTypeIVLeechers = (int) (0.0* numberOfLeechers); 
                
                /*Download Rate for each type of leacher in bytes*/
                int DRTypeILeechers = 100*1024*1024;
                int DRTypeIILeechers = 100*1024*1024;
                int DRTypeIIILeechers = 10*1024*1024;
                int DRTypeIVLeechers = 1250001;
                
                /*Upload Rate for each type of leacher in bytes*/
                int URTypeILeechers = 20*1024*1024;
                int URTypeIILeechers = 20*1024*1024;
                int URTypeIIILeechers = 5*1024;
                int URTypeIVLeechers = 625001;
                
                /*Fraction of Special Leechers*/
                double fractionOfSpecialLeechers = 0.0;
          
                /* Seeder Download and Upload Rates*/
                int DRSeeder = 100*1024*1024;
                int URSeeder = 100*1024*1024;
               
                /*Array with the number of users of each type*/
                int numberOfLeechersArray[] = new int[numberOfLeechersType*2];
                numberOfLeechersArray[0] =(int) ((1-fractionOfSpecialLeechers)*numberOfTypeILeechers);
                numberOfLeechersArray[1] =(int) (fractionOfSpecialLeechers*numberOfTypeILeechers);
                numberOfLeechersArray[2] =(int) ((1-fractionOfSpecialLeechers)*numberOfTypeIILeechers);
                numberOfLeechersArray[3] =(int) (fractionOfSpecialLeechers*numberOfTypeIILeechers);
                numberOfLeechersArray[4] =(int) ((1-fractionOfSpecialLeechers)*numberOfTypeIIILeechers);
                numberOfLeechersArray[5] =(int) (fractionOfSpecialLeechers*numberOfTypeIIILeechers);
                numberOfLeechersArray[6] =(int) ((1-fractionOfSpecialLeechers)*numberOfTypeIVLeechers);
                numberOfLeechersArray[7] =(int) (fractionOfSpecialLeechers*numberOfTypeIVLeechers);
                
                /*Array with the DR used per each type of Leechers*/
                 int DROfLeechersArray[] = new int[numberOfLeechersType];
                DROfLeechersArray[0] = DRTypeILeechers;
                DROfLeechersArray[1] = DRTypeIILeechers;
                DROfLeechersArray[2] = DRTypeIIILeechers;
                DROfLeechersArray[3] = DRTypeIVLeechers;
                
                /*Array with the UR used per each type of Leechers*/
                int UROfLeechersArray[] = new int[numberOfLeechersType];
                UROfLeechersArray[0] = URTypeILeechers;
                UROfLeechersArray[1] = URTypeIILeechers;
                UROfLeechersArray[2] = URTypeIIILeechers;
                UROfLeechersArray[3] = URTypeIVLeechers;
                 
               BTSimulation.runSimulationMutipleTypesOfLeechers (filename, fileSize, numberOfLeechersArray, duration, seederDuration, leecherDuration, startWindow, URSeeder, DRSeeder, UROfLeechersArray, DROfLeechersArray);
	}


}
