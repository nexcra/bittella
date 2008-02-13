package de.tud.kom.p2psim.impl.application.bt;

import de.tud.kom.p2psim.impl.transport.DefaultTransLayer;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;

import de.tud.kom.p2psim.api.common.Host;
import de.tud.kom.p2psim.api.common.Operation;
import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.common.SupportOperations;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.common.DefaultHostProperties;
import de.tud.kom.p2psim.impl.common.Operations;
//import de.tud.kom.p2psim.impl.network.simple.SimpleNetFactory;
//import de.tud.kom.p2psim.impl.network.simple.SimpleStaticLatencyModel;
//import de.tud.kom.p2psim.impl.network.simple.SimpleLatencyModel;
import de.tud.kom.p2psim.impl.overlay.DefaultOverlayID;
import de.tud.kom.p2psim.impl.overlay.dht.centralized.OverlayKeyImpl;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.simengine.SimulatorTest;
import de.tud.kom.p2psim.impl.storage.DefaultContentStorage;
import de.tud.kom.p2psim.impl.transport.DefaultTransLayer;
import de.tud.kom.p2psim.overlay.bt.BTClientApplication;
import de.tud.kom.p2psim.overlay.bt.BTDataStore;
import de.tud.kom.p2psim.overlay.bt.BTDocument;
import de.tud.kom.p2psim.overlay.bt.BTFactory;
import de.tud.kom.p2psim.overlay.bt.BTInternStatistic;
import de.tud.kom.p2psim.overlay.bt.BTPeerDistributeNode;
import de.tud.kom.p2psim.overlay.bt.BTPeerSearchNode;
import de.tud.kom.p2psim.overlay.bt.BTTorrent;
import de.tud.kom.p2psim.overlay.bt.BTTrackerApplication;
import de.tud.kom.p2psim.overlay.bt.BTTrackerNode;
import de.tud.kom.p2psim.overlay.bt.operation.BTOperationPeerStarter;

import uc3m.netcom.peerfactsim.impl.network.simple.UC3MNetFactory;
import uc3m.netcom.peerfactsim.impl.network.simple.UC3MLatencyModel;

public class BTSimulation extends SimulatorTest implements SupportOperations {

    private static int theirIdCounter = 0;
    private Set<OverlayID> usedIds = new HashSet<OverlayID>();
    BTTrackerApplication server;
    BTTrackerNode serverNode;
    private UC3MNetFactory netFactory;
    private BTFactory factory;
    private Collection<BTDataStore> itsDataBusses;
    private RandomGenerator itsRandomGenerator;
    private static final short DISTRIBUTION_NODE_PORT = 1;
    private static final short SEARCH_NODE_PORT = 2;
    private static final short TRACKER_NODE_PORT = 3;

    @Override
    public void setUp() {
        super.setUp();
        this.factory = new BTFactory();
        this.itsDataBusses = new LinkedList<BTDataStore>();
        this.itsRandomGenerator = new JDKRandomGenerator();
        //this.netFactory = new SimpleNetFactory();
        this.netFactory = new UC3MNetFactory();
        this.netFactory.setLatencyModel(new UC3MLatencyModel());
        //this.netFactory.setLatencyModel(new SimpleLatencyModel());
        this.server = this.createServer();
    }

    private BTClientApplication createAppl(int uploadBandwidth, int downloadBandwidth) {

        this.netFactory.setUpBandwidth(uploadBandwidth);
        this.netFactory.setDownBandwidth(downloadBandwidth);
        //this.netFactory.setLatencyModel(new UC3MLatencyModel());
        DefaultHost host = new DefaultHost();
        host.setProperties(new DefaultHostProperties());

        NetLayer net = this.netFactory.createComponent(host);
        host.setNetwork(net);

        TransLayer transLayer = new DefaultTransLayer(net);
        host.setTransport(transLayer);

        BTDataStore dataBus = new BTDataStore();
        this.itsDataBusses.add(dataBus);
        OverlayID theOverlayID = this.createNewId();
        BTInternStatistic theStatistic = new BTInternStatistic();
        dataBus.storeGeneralData("Statistic", theStatistic, theStatistic.getClass());

        BTPeerDistributeNode peerDistributeNode = new BTPeerDistributeNode(dataBus, theOverlayID, DISTRIBUTION_NODE_PORT, theStatistic, this.itsRandomGenerator);
        host.setOverlayNode(peerDistributeNode);

        BTPeerSearchNode peerSearchNode = new BTPeerSearchNode(dataBus, theOverlayID, SEARCH_NODE_PORT, DISTRIBUTION_NODE_PORT);
        host.setOverlayNode(peerSearchNode);

        BTClientApplication clientApplication = this.factory.newClient(dataBus, peerSearchNode, peerDistributeNode);
        host.setApplication(clientApplication);

        host.setContentStorage(new DefaultContentStorage());

        clientApplication.connect();

        return clientApplication;
    }

    private BTTrackerApplication createServer() {
        //this.netFactory.setUpBandwidth(16 * 1024);
        //this.netFactory.setDownBandwidth(16 * 1024);
        this.netFactory.setUpBandwidth(625001);
        this.netFactory.setDownBandwidth(1250001);
        DefaultHost host = new DefaultHost();
        host.setProperties(new DefaultHostProperties());

        NetLayer net = this.netFactory.createComponent(host);
        host.setNetwork(net);

        TransLayer transLayer = new DefaultTransLayer(net);
        host.setTransport(transLayer);

        BTTrackerNode trackerNode = new BTTrackerNode(new BTDataStore(), new DefaultOverlayID(-666), TRACKER_NODE_PORT, this.itsRandomGenerator);
        host.setOverlayNode(trackerNode);

        BTTrackerApplication trackerApplication = this.factory.newServer(trackerNode);
        host.setApplication(trackerApplication);

        this.serverNode = trackerNode;
        trackerApplication.connect();

        return trackerApplication;
    }

    public void makeFullTorrentMultipleTypesOfLeechers(int[] numberOfLeechersArray, long startTimeWindow, String documentName, long fileSize, long theDuration, long seederDuration, long leecherDuration, int URSeeder, int DRSeeder, int[] UROfLeechersArray, int[] DROfLeechersArray) {
        
        long startTimeOffset = 2 * Simulator.MINUTE_UNIT;
        OverlayKey overlayKey = new OverlayKeyImpl(documentName);
        BTDocument document = new BTDocument(overlayKey, fileSize);
        BTClientApplication seeder = this.createAppl(URSeeder, DRSeeder);

        /*Creation of the different types of Special Leechers*/

        //Type I Special Leechers
        LinkedList<BTClientApplication> SpecialLeecherTypeI = new LinkedList<BTClientApplication>();
        LinkedList<Long> SpecialLeecherTypeIStartTimes = new LinkedList<Long>();
        for (int i = 0; i < numberOfLeechersArray[1]; i++) {
            System.out.println("There are TypeI Special nodes with UR = " + UROfLeechersArray[0] + " and DR = " + DROfLeechersArray[0]);
            SpecialLeecherTypeI.addLast(this.createAppl(UROfLeechersArray[0], DROfLeechersArray[0]));
        }
        for (int i = 0; i < numberOfLeechersArray[1]; i++) {
            SpecialLeecherTypeIStartTimes.addLast(startTimeOffset + (i * (startTimeWindow / numberOfLeechersArray[1])));
        } //This results in a (nearly) equidistant number of start times.

        //Type II Special Leechers
        LinkedList<BTClientApplication> SpecialLeecherTypeII = new LinkedList<BTClientApplication>();
        LinkedList<Long> SpecialLeecherTypeIIStartTimes = new LinkedList<Long>();
        for (int i = 0; i < numberOfLeechersArray[3]; i++) {
            System.out.println("There are TypeII Special nodes with UR = " + UROfLeechersArray[1] + " and DR = " + DROfLeechersArray[1]);
            SpecialLeecherTypeII.addLast(this.createAppl(UROfLeechersArray[1], DROfLeechersArray[1]));
        }
        for (int i = 0; i < numberOfLeechersArray[3]; i++) {
            SpecialLeecherTypeIIStartTimes.addLast(startTimeOffset + (i * (startTimeWindow / numberOfLeechersArray[3])));
        } //This results in a (nearly) equidistant number of start times.

        //Type III Special Leechers
        LinkedList<BTClientApplication> SpecialLeecherTypeIII = new LinkedList<BTClientApplication>();
        LinkedList<Long> SpecialLeecherTypeIIIStartTimes = new LinkedList<Long>();
        for (int i = 0; i < numberOfLeechersArray[5]; i++) {
            System.out.println("There are TypeIII Special nodes with UR = " + UROfLeechersArray[2] + " and DR = " + DROfLeechersArray[2]);
            SpecialLeecherTypeIII.addLast(this.createAppl(UROfLeechersArray[2], DROfLeechersArray[2]));
        }
        for (int i = 0; i < numberOfLeechersArray[5]; i++) {
            SpecialLeecherTypeIIIStartTimes.addLast(startTimeOffset + (i * (startTimeWindow / numberOfLeechersArray[5])));
        } //This results in a (nearly) equidistant number of start times.

        //Type IV Special Leechers
        LinkedList<BTClientApplication> SpecialLeecherTypeIV = new LinkedList<BTClientApplication>();
        LinkedList<Long> SpecialLeecherTypeIVStartTimes = new LinkedList<Long>();
        for (int i = 0; i < numberOfLeechersArray[7]; i++) {
            System.out.println("There are TypeIV Special nodes with UR = " + UROfLeechersArray[3] + " and DR = " + DROfLeechersArray[3]);
            SpecialLeecherTypeIV.addLast(this.createAppl(UROfLeechersArray[3], DROfLeechersArray[3]));
        }
        for (int i = 0; i < numberOfLeechersArray[7]; i++) {
            SpecialLeecherTypeIVStartTimes.addLast(startTimeOffset + (i * (startTimeWindow / numberOfLeechersArray[7])));
        } //This results in a (nearly) equidistant number of start times.

        /*Creation of the different types of regular leechers*/

        //Type I Regular Leechers
        LinkedList<BTClientApplication> leecherTypeI = new LinkedList<BTClientApplication>();
        LinkedList<Long> leecherTypeIStartTimes = new LinkedList<Long>();
        for (int i = 0; i < numberOfLeechersArray[0]; i++) {
            System.out.println("There are TypeI Regular nodes with UR = " + UROfLeechersArray[0] + " and DR = " + DROfLeechersArray[0]);
            leecherTypeI.addLast(this.createAppl(UROfLeechersArray[0], DROfLeechersArray[0]));
        }
        for (int i = 0; i < numberOfLeechersArray[0]; i++) {
            leecherTypeIStartTimes.addLast(startTimeOffset + (i * (startTimeWindow / numberOfLeechersArray[0])));
        } //This results in a (nearly) equidistant number of start times.

        //Type II Regular Leechers
        LinkedList<BTClientApplication> leecherTypeII = new LinkedList<BTClientApplication>();
        LinkedList<Long> leecherTypeIIStartTimes = new LinkedList<Long>();
        for (int i = 0; i < numberOfLeechersArray[2]; i++) {
            System.out.println("There are TypeII Regular nodes with UR = " + UROfLeechersArray[1] + " and DR = " + DROfLeechersArray[1]);
            leecherTypeII.addLast(this.createAppl(UROfLeechersArray[1], DROfLeechersArray[1]));
        }
        for (int i = 0; i < numberOfLeechersArray[2]; i++) {
            leecherTypeIIStartTimes.addLast(startTimeOffset + (i * (startTimeWindow / numberOfLeechersArray[2])));
        } //This results in a (nearly) equidistant number of start times.                

        //Type III Regular Leechers
        LinkedList<BTClientApplication> leecherTypeIII = new LinkedList<BTClientApplication>();
        LinkedList<Long> leecherTypeIIIStartTimes = new LinkedList<Long>();
        for (int i = 0; i < numberOfLeechersArray[4]; i++) {
            System.out.println("There are TypeIII Regular nodes with UR = " + UROfLeechersArray[2] + " and DR = " + DROfLeechersArray[2]);
            leecherTypeII.addLast(this.createAppl(UROfLeechersArray[2], DROfLeechersArray[2]));
        }
        for (int i = 0; i < numberOfLeechersArray[4]; i++) {
            leecherTypeIIStartTimes.addLast(startTimeOffset + (i * (startTimeWindow / numberOfLeechersArray[4])));
        } //This results in a (nearly) equidistant number of start times.                

        //Type IV Regular Leechers
        LinkedList<BTClientApplication> leecherTypeIV = new LinkedList<BTClientApplication>();
        LinkedList<Long> leecherTypeIVStartTimes = new LinkedList<Long>();
        for (int i = 0; i < numberOfLeechersArray[6]; i++) {
            System.out.println("There are TypeIV Regular nodes with UR = " + UROfLeechersArray[3] + " and DR = " + DROfLeechersArray[3]);
            leecherTypeII.addLast(this.createAppl(UROfLeechersArray[3], DROfLeechersArray[3]));
        }
        for (int i = 0; i < numberOfLeechersArray[6]; i++) {
            leecherTypeIIStartTimes.addLast(startTimeOffset + (i * (startTimeWindow / numberOfLeechersArray[6])));
        } //This results in a (nearly) equidistant number of start times.                

        this.serverNode.addDocument(document.getKey());
        runSimulation(Simulator.SECOND_UNIT);

        document.setState(BTDocument.State.COMPLETE);
        seeder.storeDocument(document);
        seeder.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", seederDuration, (new Long(0)).getClass());

        for (BTClientApplication aLeecher : leecherTypeI) {
            aLeecher.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", leecherDuration, (new Long(0)).getClass());
        }

        for (BTClientApplication aLeecher : leecherTypeII) {
            aLeecher.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", leecherDuration, (new Long(0)).getClass());
        }

        for (BTClientApplication aLeecher : leecherTypeIII) {
            aLeecher.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", leecherDuration, (new Long(0)).getClass());
        }

        for (BTClientApplication aLeecher : leecherTypeIV) {
            aLeecher.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", leecherDuration, (new Long(0)).getClass());
        }


        for (BTClientApplication aLeecher : SpecialLeecherTypeI) {
            aLeecher.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", leecherDuration, (new Long(0)).getClass());
            aLeecher.getDataBus().storeGeneralData("Special", new Boolean(true), (new Boolean(true)).getClass());
        }


        for (BTClientApplication aLeecher : SpecialLeecherTypeII) {
            aLeecher.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", leecherDuration, (new Long(0)).getClass());
            aLeecher.getDataBus().storeGeneralData("Special", new Boolean(true), (new Boolean(true)).getClass());
        }


        for (BTClientApplication aLeecher : SpecialLeecherTypeIII) {
            aLeecher.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", leecherDuration, (new Long(0)).getClass());
            aLeecher.getDataBus().storeGeneralData("Special", new Boolean(true), (new Boolean(true)).getClass());
        }


        for (BTClientApplication aLeecher : SpecialLeecherTypeIV) {
            aLeecher.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", leecherDuration, (new Long(0)).getClass());
            aLeecher.getDataBus().storeGeneralData("Special", new Boolean(true), (new Boolean(true)).getClass());
        }

        runSimulation(Simulator.SECOND_UNIT);


        BTTorrent torrent = new BTTorrent(document.getKey(), document.getSize(), this.serverNode.getOverlayID(), this.serverNode.getTransLayer().getLocalTransInfo(this.serverNode.getPort()));

        BTOperationPeerStarter<BTSimulation> peerTypeIStarter = new BTOperationPeerStarter<BTSimulation>(leecherTypeIStartTimes, leecherTypeI, torrent, this, this);
        BTOperationPeerStarter<BTSimulation> peerTypeIIStarter = new BTOperationPeerStarter<BTSimulation>(leecherTypeIIStartTimes, leecherTypeII, torrent, this, this);
        BTOperationPeerStarter<BTSimulation> peerTypeIIIStarter = new BTOperationPeerStarter<BTSimulation>(leecherTypeIIIStartTimes, leecherTypeIII, torrent, this, this);
        BTOperationPeerStarter<BTSimulation> peerTypeIVStarter = new BTOperationPeerStarter<BTSimulation>(leecherTypeIVStartTimes, leecherTypeIV, torrent, this, this);

        BTOperationPeerStarter<BTSimulation> SpecialPeerTypeIStarter = new BTOperationPeerStarter<BTSimulation>(SpecialLeecherTypeIStartTimes, SpecialLeecherTypeI, torrent, this, this);
        BTOperationPeerStarter<BTSimulation> SpecialPeerTypeIIStarter = new BTOperationPeerStarter<BTSimulation>(SpecialLeecherTypeIIStartTimes, SpecialLeecherTypeII, torrent, this, this);
        BTOperationPeerStarter<BTSimulation> SpecialPeerTypeIIIStarter = new BTOperationPeerStarter<BTSimulation>(SpecialLeecherTypeIIIStartTimes, SpecialLeecherTypeIII, torrent, this, this);
        BTOperationPeerStarter<BTSimulation> SpecialPeerTypeIVStarter = new BTOperationPeerStarter<BTSimulation>(SpecialLeecherTypeIVStartTimes, SpecialLeecherTypeIV, torrent, this, this);

        seeder.downloadDocument(torrent);
        peerTypeIStarter.scheduleImmediately();
        peerTypeIIStarter.scheduleImmediately();
        peerTypeIIIStarter.scheduleImmediately();
        peerTypeIVStarter.scheduleImmediately();
        SpecialPeerTypeIStarter.scheduleImmediately();
        SpecialPeerTypeIIStarter.scheduleImmediately();
        SpecialPeerTypeIIIStarter.scheduleImmediately();
        SpecialPeerTypeIVStarter.scheduleImmediately();
        System.out.println("Duracion de la simulacion " + theDuration);
        runSimulation(theDuration);

        seeder.close(Operations.EMPTY_CALLBACK);
        for (BTClientApplication aLeecher : leecherTypeI) {
            aLeecher.close(Operations.EMPTY_CALLBACK);
        }
        for (BTClientApplication aLeecher : leecherTypeII) {
            aLeecher.close(Operations.EMPTY_CALLBACK);
        }
        for (BTClientApplication aLeecher : leecherTypeIII) {
            aLeecher.close(Operations.EMPTY_CALLBACK);
        }
        for (BTClientApplication aLeecher : leecherTypeIV) {
            aLeecher.close(Operations.EMPTY_CALLBACK);
        }

        for (BTClientApplication aLeecher : SpecialLeecherTypeI) {
            aLeecher.close(Operations.EMPTY_CALLBACK);
        }
        for (BTClientApplication aLeecher : SpecialLeecherTypeII) {
            aLeecher.close(Operations.EMPTY_CALLBACK);
        }
        for (BTClientApplication aLeecher : SpecialLeecherTypeIII) {
            aLeecher.close(Operations.EMPTY_CALLBACK);
        }
        for (BTClientApplication aLeecher : SpecialLeecherTypeIV) {
            aLeecher.close(Operations.EMPTY_CALLBACK);
        }

    }

    public void makeFullTorrent(int numberOfLeecher, int numberOfSpecialLeecher, long startTimeWindow, String documentName, long fileSize, long theDuration, long seederDuration, long leecherDuration, int seederUpload, int seederDownload, int leecherUpload, int leecherDownload) {
        long startTimeOffset = 5 * Simulator.MINUTE_UNIT;
        OverlayKey overlayKey = new OverlayKeyImpl(documentName);
        BTDocument document = new BTDocument(overlayKey, fileSize);
        BTClientApplication seeder = this.createAppl(seederUpload, seederDownload);



        LinkedList<BTClientApplication> sleecher = new LinkedList<BTClientApplication>();
        LinkedList<Long> sleecherStartTimes = new LinkedList<Long>();
        for (int i = 0; i < numberOfSpecialLeecher; i++) {
            sleecher.addLast(this.createAppl(leecherUpload, leecherDownload));
        }
        for (int i = 0; i < numberOfSpecialLeecher; i++) {
            sleecherStartTimes.addLast(startTimeOffset + (i * (startTimeWindow / numberOfSpecialLeecher)));
        } //This results in a (nearly) equidistant number of start times.

        LinkedList<BTClientApplication> leecher = new LinkedList<BTClientApplication>();
        LinkedList<Long> leecherStartTimes = new LinkedList<Long>();
        for (int i = 0; i < numberOfLeecher; i++) {
            leecher.addLast(this.createAppl(leecherUpload, leecherDownload));
        }
        for (int i = 0; i < numberOfLeecher; i++) {
            leecherStartTimes.addLast(startTimeOffset + (i * (startTimeWindow / numberOfLeecher)));
        } //This results in a (nearly) equidistant number of start times.


        this.serverNode.addDocument(document.getKey());
        runSimulation(Simulator.SECOND_UNIT);

        document.setState(BTDocument.State.COMPLETE);
        seeder.storeDocument(document);
        seeder.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", seederDuration, (new Long(0)).getClass());
        for (BTClientApplication aLeecher : leecher) {
            aLeecher.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", leecherDuration, (new Long(0)).getClass());
        }


        for (BTClientApplication aLeecher : sleecher) {
            aLeecher.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", leecherDuration, (new Long(0)).getClass());
            aLeecher.getDataBus().storeGeneralData("Special", new Boolean(true), (new Boolean(true)).getClass());
        }


        runSimulation(Simulator.SECOND_UNIT);

        BTTorrent torrent = new BTTorrent(document.getKey(), document.getSize(), this.serverNode.getOverlayID(), this.serverNode.getTransLayer().getLocalTransInfo(this.serverNode.getPort()));
        BTOperationPeerStarter<BTSimulation> peerStarter = new BTOperationPeerStarter<BTSimulation>(leecherStartTimes, leecher, torrent, this, this);
        BTOperationPeerStarter<BTSimulation> speerStarter = new BTOperationPeerStarter<BTSimulation>(sleecherStartTimes, sleecher, torrent, this, this);

        seeder.downloadDocument(torrent);
        peerStarter.scheduleImmediately();
        speerStarter.scheduleImmediately();
        System.out.println("Duracion de la simulaci�n " + theDuration);
        runSimulation(theDuration);

        seeder.close(Operations.EMPTY_CALLBACK);
        for (BTClientApplication aLeecher : leecher) {
            aLeecher.close(Operations.EMPTY_CALLBACK);
        }
        for (BTClientApplication aLeecher : sleecher) {
            aLeecher.close(Operations.EMPTY_CALLBACK);
        }
    }

    private static void runSimulation() {
        String filename = "c:\\temp\\BA_SimResult." + (System.currentTimeMillis() / 1000) + ".txt";
        long fileSize = 40 * 1000 * 1000;
        int numberOfLeecher = 24 * 30;
        int numberOfSpecialLeecher = 0;
        long duration = 7 * 24 * Simulator.HOUR_UNIT;
        long seederDuration = 2 * 24 * Simulator.HOUR_UNIT;
        long leecherDuration = 5 * Simulator.MINUTE_UNIT;
        long startWindow = 24 * Simulator.HOUR_UNIT;
        int seederUpload = 16 * 1024, seederDownload = 16 * 1024, leecherUpload = 16 * 1024, leecherDownload = 128 * 1024;
        runSimulation(filename, fileSize, numberOfLeecher, numberOfSpecialLeecher, duration, seederDuration, leecherDuration, startWindow, seederUpload, seederDownload, leecherUpload, leecherDownload);
    }

    //Run the simulation for the case of multiples types of different leechers (i.e. different UR and DR)
    public static void runSimulationMutipleTypesOfLeechers(String filename, long fileSize, int[] numberOfLeechersArray, long duration, long seederDuration, long leecherDuration, long startWindow, int URSeeder, int DRSeeder, int[] UROfLeechersArray, int[] DROfLeechersArray) {

        //Before
        BTSimulation testClass;
        testClass = new BTSimulation();
        String name = "SimulationFile";
        long startTime, endTime;

        //Simulation
        testClass.setUp();
        testClass.itsRandomGenerator.setSeed(System.nanoTime());
        startTime = System.currentTimeMillis();
        testClass.makeFullTorrentMultipleTypesOfLeechers(numberOfLeechersArray, startWindow, name, fileSize, duration, seederDuration, leecherDuration, URSeeder, DRSeeder, UROfLeechersArray, DROfLeechersArray);
        endTime = System.currentTimeMillis();
        Collection<BTDataStore> resultDataBusses = testClass.itsDataBusses;
        testClass.tearDown();

        //After
        long requiredMilliSeconds = endTime - startTime;
        long requiredSeconds = requiredMilliSeconds / 1000;
        long requiredMinutes = requiredSeconds / 60;
        long requiredHours = requiredMinutes / 60;
        long requiredDays = requiredHours / 24;
        String result = "It took " + requiredDays + " days, " + (requiredHours % 24) + " hours, " + (requiredMinutes % 60) + " minutes, " + (requiredSeconds % 60) + " seconds.\n";
//		String fileResult = "" + numberOfLeecher + "," + fileSize + "," + requiredMilliSeconds + ";\r\n";

//		Collection<BTCompressedStatistic> compressedStatistics = new LinkedList<BTCompressedStatistic>();
        System.out.println("Finished simulation. Results:");
        System.out.println(result);
//		FileWriter resultFile = null;
        Date currentDate = new Date();
        String textResults = "Real time: " + currentDate.toString() + "\n";
        textResults += ("Filesize: " + fileSize + " Byte\n");
        //textResults += ("Number of Peers: " + numberOfLeecher + "\n");
        textResults += ("Started in " + (startWindow / Simulator.SECOND_UNIT) + " seconds.\n");
        textResults += ("Seeder duration: " + (seederDuration / Simulator.SECOND_UNIT) + " seconds.\n");
        textResults += ("A second is " + Simulator.SECOND_UNIT + " ticks long\n");
        textResults += ("Peers stay online for " + (leecherDuration / Simulator.SECOND_UNIT) + " seconds\n");
        textResults += ("Bandwith in KiloBytes per second:\n");
        //textResults += ("Peer Upload: " + (leecherUpload / 1024) + "\n");
        //textResults += ("Peer Download: " + (leecherDownload / 1024) + "\n");
        //textResults += ("Seed Upload: " + (seederUpload / 1024) + "\n");
        //textResults += ("Seed Download: " + (seederDownload / 1024) + "\n");
        textResults += (result);
        textResults += ("This are " + requiredSeconds + " seconds.\n");

        if (filename != null) {
            FileWriter textFile = null;
            try {
//				resultFile = new FileWriter("c:\\temp\\BA_SimResult.csv", true);
                textFile = new FileWriter(filename, true);
                textFile.write(textResults + "\n");
//				resultFile.write(fileResult);
                for (BTDataStore aDataBus : resultDataBusses) {
                    BTCompressedStatistic currentStatistic = new BTCompressedStatistic((BTInternStatistic) aDataBus.getGeneralData("Statistic"), (int) (5 * Simulator.MINUTE_UNIT));
//					compressedStatistics.add(currentStatistic);
//					System.out.println(currentStatistic.toString() + "\n");
                    textFile.write(currentStatistic.toString(true) + "\n");
//					resultFile.write(currentStatistic.toCSV(true));
                }
            } catch (IOException e) {
                System.out.println("File operation failed: " + e);
            } finally {
//				if (resultFile != null)
//					try { resultFile.close(); if (textFile != null) textFile.close(); } catch (IOException e2) {/*Nothing to do*/}
                try {
                    if (textFile != null) {
                        textFile.close();
                    }
                } catch (IOException e2) {/*Nothing to do*/
                }
            }
        }

    }

    public static void runSimulation(String filename, long fileSize, int numberOfLeecher, int numberOfSpecialLeecher, long duration, long seederDuration, long leecherDuration, long startWindow, int seederUpload, int seederDownload, int leecherUpload, int leecherDownload) {

        //Before
        BTSimulation testClass;
        testClass = new BTSimulation();
        String name = "SimulationFile";
        long startTime, endTime;

        //Simulation
        testClass.setUp();
        testClass.itsRandomGenerator.setSeed(System.nanoTime());
        startTime = System.currentTimeMillis();
        testClass.makeFullTorrent(numberOfLeecher, numberOfSpecialLeecher, startWindow, name, fileSize, duration, seederDuration, leecherDuration, seederUpload, seederDownload, leecherUpload, leecherDownload);
        endTime = System.currentTimeMillis();
        Collection<BTDataStore> resultDataBusses = testClass.itsDataBusses;
        testClass.tearDown();

        //After
        long requiredMilliSeconds = endTime - startTime;
        long requiredSeconds = requiredMilliSeconds / 1000;
        long requiredMinutes = requiredSeconds / 60;
        long requiredHours = requiredMinutes / 60;
        long requiredDays = requiredHours / 24;
        String result = "It took " + requiredDays + " days, " + (requiredHours % 24) + " hours, " + (requiredMinutes % 60) + " minutes, " + (requiredSeconds % 60) + " seconds.\n";
//		String fileResult = "" + numberOfLeecher + "," + fileSize + "," + requiredMilliSeconds + ";\r\n";

//		Collection<BTCompressedStatistic> compressedStatistics = new LinkedList<BTCompressedStatistic>();
        System.out.println("Finished simulation. Results:");
        System.out.println(result);
//		FileWriter resultFile = null;
        Date currentDate = new Date();
        String textResults = "Real time: " + currentDate.toString() + "\n";
        textResults += ("Filesize: " + fileSize + " Byte\n");
        textResults += ("Number of Peers: " + numberOfLeecher + "\n");
        textResults += ("Started in " + (startWindow / Simulator.SECOND_UNIT) + " seconds.\n");
        textResults += ("Seeder duration: " + (seederDuration / Simulator.SECOND_UNIT) + " seconds.\n");
        textResults += ("A second is " + Simulator.SECOND_UNIT + " ticks long\n");
        textResults += ("Peers stay online for " + (leecherDuration / Simulator.SECOND_UNIT) + " seconds\n");
        textResults += ("Bandwith in KiloBytes per second:\n");
        textResults += ("Peer Upload: " + (leecherUpload / 1024) + "\n");
        textResults += ("Peer Download: " + (leecherDownload / 1024) + "\n");
        textResults += ("Seed Upload: " + (seederUpload / 1024) + "\n");
        textResults += ("Seed Download: " + (seederDownload / 1024) + "\n");
        textResults += (result);
        textResults += ("This are " + requiredSeconds + " seconds.\n");
        if (filename != null) {
            FileWriter textFile = null;
            try {
//				resultFile = new FileWriter("c:\\temp\\BA_SimResult.csv", true);
                textFile = new FileWriter(filename, true);
                textFile.write(textResults + "\n");
//				resultFile.write(fileResult);
                for (BTDataStore aDataBus : resultDataBusses) {
                    BTCompressedStatistic currentStatistic = new BTCompressedStatistic((BTInternStatistic) aDataBus.getGeneralData("Statistic"), (int) (5 * Simulator.MINUTE_UNIT));
//					compressedStatistics.add(currentStatistic);
//					System.out.println(currentStatistic.toString() + "\n");
                    textFile.write(currentStatistic.toString(true) + "\n");
//					resultFile.write(currentStatistic.toCSV(true));
                }
            } catch (IOException e) {
                System.out.println("File operation failed: " + e);
            } finally {
//				if (resultFile != null)
//					try { resultFile.close(); if (textFile != null) textFile.close(); } catch (IOException e2) {/*Nothing to do*/}
                try {
                    if (textFile != null) {
                        textFile.close();
                    }
                } catch (IOException e2) {/*Nothing to do*/
                }
            }
        }
    }

    public static void main(String[] args) {
        runSimulation();
    }

    @Override
    public void tearDown() {
        super.tearDown();
        this.factory = null;
        this.itsRandomGenerator = null;
        this.netFactory = null;
        this.server = null;
        this.serverNode = null;
        this.itsDataBusses = null;
    }

    private OverlayID createNewId() {
        OverlayID nextId;
        do {
            nextId = new DefaultOverlayID(theirIdCounter++);
        } while (this.usedIds.contains(nextId));
        return nextId;
    }

    public Operation createOperation(String opName, String[] params, OperationCallback caller) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Method 'createOperation' in class 'BTSimulation' not yet implemented!");
    //return null;
    }

    public Host getHost() {
        // TODO Auto-generated method stub
        throw new RuntimeException("Method 'getHost' in class 'BTSimulation' not yet implemented!");
    //return null;
    }

    public void setHost(Host host) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Method 'setHost' in class 'BTSimulation' not yet implemented!");
    //
    }

    public void calledOperationFailed(Operation theOperation) {
    //Who cares?
    }

    public void calledOperationSucceeded(Operation theOperation) {
    //Who cares?
    }
}
