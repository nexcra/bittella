package uc3m.netcom.peerfactsim.test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;

//import com.sun.jdi.connect.Connector.IntegerArgument;

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
import de.tud.kom.p2psim.impl.network.gnp.GnpLatencyModel;
import de.tud.kom.p2psim.impl.network.gnp.GnpNetBandwidthManagerEvent;
import de.tud.kom.p2psim.impl.network.gnp.GnpNetBandwidthManagerPeriodical;
import de.tud.kom.p2psim.impl.network.gnp.GnpNetLayer;
import de.tud.kom.p2psim.impl.network.gnp.GnpNetLayerFactory;
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
import uc3m.netcom.peerfactsim.overlay.bt.BTUC3MDistributeNode;
import uc3m.netcom.peerfactsim.impl.util.logging.UC3MLogBT;

public class GNP_BTSimulation_Periodical extends SimulatorTest implements SupportOperations {

    private static int theirIdCounter = 0;
    private Set<OverlayID> usedIds = new HashSet<OverlayID>();
    private BTTrackerApplication server;
    private BTTrackerNode serverNode;
    private GnpNetLayerFactory netFactory;
    private BTFactory factory;
    private Collection<BTDataStore> itsDataBusses;
    private RandomGenerator itsRandomGenerator;
    private static final short DISTRIBUTION_NODE_PORT = 1;
    private static final short SEARCH_NODE_PORT = 2;
    private static final short TRACKER_NODE_PORT = 3;
    public static UC3MLogBT logger = new UC3MLogBT();
    
    @Override
    public void setUp() {
        super.setUp();

        itsRandomGenerator = Simulator.getRandom();

        this.factory = new BTFactory();
        this.itsDataBusses = new LinkedList<BTDataStore>();

        this.netFactory = new GnpNetLayerFactory();
        this.netFactory.setGnpFile("../PeerfactSim/config/gnp/7391_0.2.xml");

        GnpLatencyModel latencyModel = new GnpLatencyModel();
        latencyModel.setUsePingErPacketLoss(true);
        latencyModel.setUsePingErJitter(true);
        latencyModel.setUsePingErRttData(true);

        this.netFactory.setLatencyModel(latencyModel);
        //this.netFactory.setBandwidthManager(new GnpNetBandwidthManagerEvent());
        this.netFactory.setBandwidthManager(new GnpNetBandwidthManagerPeriodical());
        this.netFactory.setPbaPeriod(1.0);

        this.server = this.createServer();
    }

    /* old original setUp();
    public void setUp() {
    super.setUp();
    this.factory = new BTFactory();
    this.itsDataBusses = new LinkedList<BTDataStore>();
    this.itsRandomGenerator = new JDKRandomGenerator();
    this.netFactory = new SimpleNetFactory();
    this.netFactory.setLatencyModel(new SimpleStaticLatencyModel(10l));
    this.server = this.createServer();
    }
     */
    private BTClientApplication createAppl(int uploadBandwith, int downloadBandwith) {
        this.netFactory.setUpBandwidth(uploadBandwith);
        this.netFactory.setDownBandwidth(downloadBandwith);

        DefaultHost host = new DefaultHost();
        DefaultHostProperties prop = new DefaultHostProperties();
        prop.setGroupID("Spain");
        host.setProperties(prop);

        GnpNetLayer net = this.netFactory.createComponent(host);
        host.setNetwork(net);
//		System.out.println(net);

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


    private BTClientApplication createModAppl(int uploadBandwith, int downloadBandwith) {
        this.netFactory.setUpBandwidth(uploadBandwith);
        this.netFactory.setDownBandwidth(downloadBandwith);

        DefaultHost host = new DefaultHost();
        DefaultHostProperties prop = new DefaultHostProperties();
        prop.setGroupID("Spain");
        host.setProperties(prop);

        GnpNetLayer net = this.netFactory.createComponent(host);
        host.setNetwork(net);
//		System.out.println(net);

        TransLayer transLayer = new DefaultTransLayer(net);
        host.setTransport(transLayer);

        BTDataStore dataBus = new BTDataStore();
        this.itsDataBusses.add(dataBus);
        OverlayID theOverlayID = this.createNewId();
        BTInternStatistic theStatistic = new BTInternStatistic();
        dataBus.storeGeneralData("Statistic", theStatistic, theStatistic.getClass());

        BTUC3MDistributeNode peerDistributeNode = new BTUC3MDistributeNode(dataBus, theOverlayID, DISTRIBUTION_NODE_PORT, theStatistic, this.itsRandomGenerator);
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

        // Tracker hat Breitband Download: 100 Mbit/s  Upload: 100 Mbit/s
        this.netFactory.setUpBandwidth(12500000);
        this.netFactory.setDownBandwidth(12500000);

        // Tracker steht in Deutschland
        DefaultHost host = new DefaultHost();
        DefaultHostProperties prop = new DefaultHostProperties();
        prop.setGroupID("Germany");
        host.setProperties(prop);

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

    public void makeFullTorrent(long startTimeWindow, String documentName, long fileSize, long theDuration, long seederDuration, long leecherDuration) {


        long startTimeOffset = 5 * Simulator.MINUTE_UNIT;
        OverlayKey overlayKey = new OverlayKeyImpl(documentName);
        BTDocument document = new BTDocument(overlayKey, fileSize);

        // Seeder hat Breitband Download: 1 Mbit/s  Upload: 1 Mbit/s
        BTClientApplication seeder = this.createAppl(125000, 125000);

        LinkedList<BTClientApplication> leecher = new LinkedList<BTClientApplication>();

        int numberOfLeecher = 80;

        // 75 % DSL Download: 768 Kbit/s  Upload: 128 Kbit/s
        for (int i = 0; i < 55; i++) {
            leecher.addLast(this.createModAppl(16000, 96000));
        }

        // 15 % Modem Download: 128 Kbit/s  Upload: 16 Kbit/s
        for (int i = 0; i < 15; i++) {
            leecher.addLast(this.createModAppl(125000, 125000));
        }

        // 8 % Breitband Download: 1 Mbit/s  Upload: 1 Mbit/s
        for (int i = 0; i < 8; i++) {
            leecher.addLast(this.createModAppl(1250000, 1250000));
        }

        // 2 % Breitband Download: 10 Mbit/s  Upload: 10 Mbit/s
        for (int i = 0; i < 2; i++) {
            leecher.addLast(this.createModAppl(1250000, 1250000));
        }


        /*		int numberOfLeecher = 300;
        // 75 % DSL Download: 768 Kbit/s  Upload: 128 Kbit/s
        for (int i = 0; i < 225; i++)
        leecher.addLast(this.createAppl(16000, 96000));
        // 15 % Modem Download: 128 Kbit/s  Upload: 16 Kbit/s
        for (int i = 0; i < 45; i++)
        leecher.addLast(this.createAppl(2000, 16000));
        // 8 % Breitband Download: 1 Mbit/s  Upload: 1 Mbit/s
        for (int i = 0; i < 24; i++)
        leecher.addLast(this.createAppl(125000, 125000));
        // 2 % Breitband Download: 10 Mbit/s  Upload: 10 Mbit/s
        for (int i = 0; i < 6; i++)
        leecher.addLast(this.createAppl(1250000, 1250000));
         */


        LinkedList<Long> leecherStartTimes = new LinkedList<Long>();
        for (int i = 0; i < numberOfLeecher; i++) {
            leecherStartTimes.addLast(startTimeOffset + (i * (startTimeWindow / numberOfLeecher)));
        } //This results in a (nearly) equidistant number of start times.

        this.serverNode.addDocument(document.getKey());
        //	runSimulation(Simulator.SECOND_UNIT);

        document.setState(BTDocument.State.COMPLETE);
        seeder.storeDocument(document);
        seeder.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", seederDuration, (new Long(0)).getClass());
        for (BTClientApplication aLeecher : leecher) {
            aLeecher.getDataBus().storeGeneralData("DurationOfUploadAfterDownloadFinished", leecherDuration, (new Long(0)).getClass());
        }
        //	runSimulation(Simulator.SECOND_UNIT);

        BTTorrent torrent = new BTTorrent(document.getKey(), document.getSize(), this.serverNode.getOverlayID(), this.serverNode.getTransLayer().getLocalTransInfo(this.serverNode.getPort()));
        BTOperationPeerStarter<GNP_BTSimulation_Periodical> peerStarter = new BTOperationPeerStarter<GNP_BTSimulation_Periodical>(leecherStartTimes, leecher, torrent, this, this);

        seeder.downloadDocument(torrent);
        peerStarter.scheduleImmediately();

        runSimulation(theDuration);

        seeder.close(Operations.EMPTY_CALLBACK);
        for (BTClientApplication aLeecher : leecher) {
            aLeecher.close(Operations.EMPTY_CALLBACK);
        }
    }

    public static void main(String[] args) {
        int days = 3;
        String filename = "" + args[1];
        long fileSize = Integer.valueOf(args[2]) * 1024 * 1024; //Size of the file in byte.
        long duration = days * 24 * Simulator.HOUR_UNIT; //Maximum simulated time.
        long seederDuration = days * 24 * Simulator.HOUR_UNIT; //How long the seeder stays online.
        long leecherDuration = 5 * Simulator.MINUTE_UNIT; //How long leecher stay online after their download is finished.
        long startWindow = Simulator.MINUTE_UNIT; //In this time window, all leecher get started.
        Simulator.getInstance().setSeed(Integer.valueOf(args[0]));
        Simulator.getInstance().setStatusInterval(10000000);
        
        for(int i=0;i<1;i++){
          try{
            GNP_BTSimulation_Periodical.runSimulation(filename, fileSize, duration, seederDuration, leecherDuration, startWindow);  
            GNP_BTSimulation_Periodical.logger.flush();
            GNP_BTSimulation_Periodical.logger.finish();
         }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            try{
                GNP_BTSimulation_Periodical.logger.finish();
            }catch(java.io.IOException eio){
                System.out.println(eio.getMessage());
                eio.printStackTrace();
            }
         }       
        }
        
        try{
            GNP_BTSimulation_Periodical.logger.finish();
        }catch(java.io.IOException em){
            System.out.println(em.getMessage());
            em.printStackTrace();
        }
    }

    public static void runSimulation(String filename, long fileSize, long duration, long seederDuration, long leecherDuration, long startWindow) {

        //Before
        GNP_BTSimulation_Periodical testClass;
        testClass = new GNP_BTSimulation_Periodical();
        String name = "SimulationFile";
        long startTime, endTime;

        //Simulation
        testClass.setUp();

        startTime = System.currentTimeMillis();
        testClass.makeFullTorrent(startWindow, name, fileSize, duration, seederDuration, leecherDuration);
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
        textResults += ("Started in " + (startWindow / Simulator.SECOND_UNIT) + " seconds.\n");
        textResults += ("Seeder duration: " + (seederDuration / Simulator.SECOND_UNIT) + " seconds.\n");
        textResults += ("Peers stay online for " + (leecherDuration / Simulator.SECOND_UNIT) + " seconds\n");
        textResults += (result);
        textResults += ("This are " + requiredSeconds + " seconds.\n");
        if (filename != null) {
            FileWriter textFile = null;
            try {
                textFile = new FileWriter(filename, true);
                textFile.write(textResults + "\n");
//				resultFile.write(fileResult);
                for (BTDataStore aDataBus : resultDataBusses) {
                    BTCompressedStatistic currentStatistic = new BTCompressedStatistic((BTInternStatistic) aDataBus.getGeneralData("Statistic"), (int) (1 * Simulator.MINUTE_UNIT));
//					compressedStatistics.add(currentStatistic);
//					System.out.println(currentStatistic.toString() + "\n");
                    textFile.write(currentStatistic.toString(true) + "\n");
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
