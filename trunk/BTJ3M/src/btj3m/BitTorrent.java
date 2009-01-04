/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package btj3m;

import jBittorrentAPI.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileReader;

/**
 *
 * @author jmcamacho
 */
public class BitTorrent extends Thread implements DMListener { //EEListener

    private int init_port = 6881;
    private boolean end;
    private HashMap<Integer, AvPieces> pieceManagers;
    private HashMap<Integer, BTUnchokeNum> unchokeAlgs;
    private ArrayList<DwManager> dwManagers;
    private HashMap<String, DwManager> torrentMap;
    private HashMap<Long, Event> events;
    private ArrayList<Long> timestamps;
    private HashMap<InputStream, ExtEventHandler> ext;
    private LinkedHashMap<Integer, String> savepaths;

    public BitTorrent() {
        this.pieceManagers = new HashMap<Integer, AvPieces>();
        this.unchokeAlgs = new HashMap<Integer, BTUnchokeNum>();
        this.dwManagers = new ArrayList<DwManager>();
        this.torrentMap = new HashMap<String, DwManager>();
        this.events = new HashMap<Long, Event>();
        this.timestamps = new ArrayList<Long>();
        this.savepaths = new LinkedHashMap<Integer, String>();
        this.end = false;
    }

    /**
     * This method is called everytime one or more peers are discovered. Peers
     * can be found either by means of the tracker list or a peer contact us or
     * by peer exchange like techniques.
     * 
     * @param dwm       - Torrent download that generated the event.
     * @param newPeers  - List of new peers discovered.
     * 
     */
    public void peerListUpdated(DwManager dwm, LinkedHashMap<String, Peer> newPeers) {
    }

    /**
     * This method is called everytime a BITFIELD or a HAVE message is received.
     *  
     * @param p     - Peer that originated the message.
     * @param dwm   - Torrent download to which the peer belongs.
     */
    public void hasSetUpdated(Peer p, DwManager dwm) {
    }

    /**
     * This method is called everytime a connection to another peer changes its
     * state (interested/ing, un/choked).
     * 
     * @param p     - 
     * @param dwm
     * @param event
     */
    public void connectionUpdated(Peer p, DwManager dwm, int event) {
    }

    /**
     * This method is called wether a piece is completed or requested by a peer.
     * 
     * @param p     - Peer requesting the piece or downloaded from.
     * @param dwm   - Torrent download generating the event
     * @param event - Event type (completed or requested)
     * @param piece - Piece number
     */
    public void pieceUpdated(Peer p, DwManager dwm, int event, int piece) {
        //      if(completed) then
        //                 dwm.stopTrackerUpdate();
        //                 dwm.closeTempFiles();    
    }

    /**
     * 
     * @param dwm
     */
    public void peersUnchoke(DwManager dwm) {
    }

    public void dwSeeding(DwManager dwm) {
    }

    private boolean parseInitFile(String file) {

        ArrayList<String> files = new ArrayList<String>();
        ArrayList<Float> maxUR = new ArrayList<Float>();
        ArrayList<Float> maxDR = new ArrayList<Float>();
        ArrayList<Boolean> enabled = new ArrayList<Boolean>();
        ArrayList<Integer> initU = new ArrayList<Integer>();
        ArrayList<String> paths = new ArrayList<String>();

        TorrentProcessor tp = new TorrentProcessor();
        LineNumberReader lnr = null;
        try {
            lnr = new LineNumberReader(new FileReader(new File(file)));
        } catch (IOException e) {
            return false;
        }
        String line = "";

        while (line != null) {

            try {
                line = lnr.readLine();
            } catch (IOException e) {
                continue;
            }
            if (line == null) {
                continue;
            }
            if (line.startsWith("#")) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line, ",");

            if (st.countTokens() < 4) {
                continue;
            }
            files.add(st.nextToken());
            maxUR.add(Float.parseFloat(st.nextToken()));
            maxDR.add(Float.parseFloat(st.nextToken()));
            if (st.countTokens() < 1) {
                paths.add("./");
            } else {
                paths.add(st.nextToken());
            }
            if (st.countTokens() < 1) {
                enabled.add(false);
            } else {
                enabled.add(Boolean.getBoolean(st.nextToken()));
            }
            if (st.countTokens() < 1) {
                initU.add(4);
            } else {
                initU.add(Integer.parseInt(st.nextToken()));
            }
        }//while


        for (int i = 0; i < files.size(); i++) {
            TorrentFile t = tp.getTorrentFile(tp.parseTorrent(files.get(i)));
            AvPieces pManager = new AvPieces(t.piece_hash_values_as_binary.size());
            boolean en = enabled.get(i);
            int init_u = initU.get(i);
            BTUnchokeNum unAlg = new BTUnchokeNum();
            unAlg.setup(maxUR.get(i), maxDR.get(i), en, init_u);
            DwManager dm = new DwManager(t, Utils.generateID(), paths.get(i), pManager, unAlg);
            this.savepaths.put(dm.hashCode(), paths.get(i));
            this.pieceManagers.put(dm.hashCode(), pManager);
            this.unchokeAlgs.put(dm.hashCode(), unAlg);
            this.torrentMap.put(files.get(i), dm);
        }

        return true;

    }

    public boolean parseEventFile(String file) {

        LineNumberReader lnr = null;
        try {
            lnr = new LineNumberReader(new FileReader(new File(file)));
        } catch (IOException e) {
            return false;
        }
        String line = "";

        while (line != null) {

            try {
                line = lnr.readLine();
            } catch (IOException e) {
                continue;
            }
            if (line == null) {
                continue;
            }
            if (line.startsWith("#")) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line, ",");
            if (st.countTokens() == 3) {
                String eventType = st.nextToken();
                int type = -1;
                if (eventType.equals("START")) {
                    type = Event.START;
                } else if (eventType.equals("STOP")) {
                    type = Event.STOP;
                } else if (eventType.equals("END")) {
                    type = Event.END;
                } else {
                    type = Event.ERROR;
                }

                Event e = new Event(type, Long.parseLong(st.nextToken()), st.nextToken());
                this.addEvent(e);
            }
        }

        return true;
    }

    public void addExtEventHandler(InputStream is) {

        if (this.ext == null) {
            ext = new HashMap<InputStream, ExtEventHandler>();
        }
        ext.put(is, new ExtEventHandler(is, this));
    }

    public boolean delExtEventHandler(InputStream is) {
        if (this.ext == null) {
            return false;
        }
        ExtEventHandler ex = this.ext.remove(is);
        if (ex != null) {
            return ex.end();
        } else {
            return false;
        }
    }

    public void addEvent(Event ev) {

        synchronized (this.timestamps) {
            this.events.put(ev.getDelay(), ev);
            this.timestamps.add(ev.getDelay());
            this.timestamps.notify();
        }
    }

    @Override
    public void run() {
        try {
            long w = 1000;
            while (!this.end) {
                synchronized (this.timestamps) {
                    try {
                        this.timestamps.wait(w);
                    } catch (InterruptedException ioe) {
                        System.out.println(ioe.getMessage());
                        this.end = true;
                        continue;
                    }
                    long ctime = System.currentTimeMillis();
                    ArrayList<Long> toRemove = new ArrayList<Long>();
                    for (Long event : this.events.keySet()) {
                        ctime = System.currentTimeMillis();
                        if (event < ctime) {
                            Event ev = this.events.get(event);
                            System.out.println("TYPE: " + ev.getType());
                            switch (ev.getType()) {
                                case Event.START:
                                    DwManager dm = this.torrentMap.get(ev.torrent);
                                    dm.addDMListener(this);
                                    if (dm.startListening(this.init_port, this.init_port)) {
                                        this.init_port++;
                                        dm.startTrackerUpdate();
                                        dm.start();
                                        this.dwManagers.add(dm);
                                    } else {
                                        System.out.println("ERROR WHILE OPENING PORTS");
                                    }
                                    toRemove.add(event);
                                    break;
                                case Event.STOP:
                                    DwManager dm1 = this.torrentMap.get(ev.torrent);
                                    if (this.dwManagers.contains(dm1)) {
                                        dm1.end();
                                        int dw_id = dm1.hashCode();
                                        this.dwManagers.remove(dm1);
                                        this.pieceManagers.remove(dw_id);
                                        this.savepaths.remove(dw_id);
                                        this.unchokeAlgs.remove(dw_id);
                                        this.torrentMap.remove(dw_id);
                                    }
                                    toRemove.add(event);
                                    break;
                                case Event.END:
                                    if (this.dwManagers.size() != 0) {
                                        for (DwManager dwm : this.dwManagers) {
                                            dwm.end();
                                        }
                                    }
                                    
                                    for (ExtEventHandler eeh : this.ext.values()) {
                                        eeh = null;
                                    }
                                    System.exit(0);

                            }//switch
                            this.timestamps.remove(event);
                            java.util.Collections.sort(this.timestamps);
                        }//if time<ctime
                    }//for
                    long ntime = ctime;
                    if (!this.timestamps.isEmpty()) {
                        ntime = this.timestamps.get(0);
                    } else {
                        ntime += 10000;
                    }
                    w = ntime - ctime;
                    if (w == 0) {
                        w = 1;
                    }
                    System.out.println("DELAY: " + w);
                    for (Long l : toRemove) {
                        this.events.remove(l);
                    }
                    toRemove.clear();
                    this.timestamps.notify();
                }//synchro
            }//while

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }

    }//run

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println(
                    "Incorrect use, please provide the path of the config file...\r\n" +
                    "\r\nUsage:\r\n" +
                    "BitTorrent <config_file>");

            System.exit(1);
        }

        BitTorrent bt = new BitTorrent();

        if (!bt.parseInitFile(args[0])) {
            System.out.println("Error while processing torrent files. Exiting Client...");
            System.exit(-1);
        }

        if (!bt.parseEventFile(args[1])) {
            System.err.println("Error while processing event files. Exiting Client");
        }

        bt.addExtEventHandler(System.in);
        bt.start();

    }

    private class ExtEventHandler extends Thread {

        private BitTorrent bt;
        private LineNumberReader lnr;
        private boolean stop;

        public ExtEventHandler(InputStream is, BitTorrent bt) {
            this.bt = bt;
            this.stop = false;
            this.lnr = new LineNumberReader(new BufferedReader(new InputStreamReader(is)));
            this.start();
        }

        @Override
        public void run() {
            String line = "";

            while (!this.stop || line == null) {

                try {
                    line = lnr.readLine();
                } catch (IOException ioe) {
                    this.stop = true;
                    bt.addEvent(new Event(Event.ERROR, -1, "Cannot read from command line"));
                    line = null;
                    continue;
                }

                StringTokenizer st = new StringTokenizer("line", ",");

                if (st.countTokens() != 3) {
                    bt.addEvent(new Event(Event.ERROR, -1, "Wrong command syntax"));
                    continue;
                }

                String eventType = st.nextToken();
                if (eventType.equals("END")) {
                    bt.addEvent(new Event(Event.END, Long.parseLong(st.nextToken()), st.nextToken()));
                } else if (eventType.equals("START")) {
                    bt.addEvent(new Event(Event.START, Long.parseLong(st.nextToken()), st.nextToken()));
                } else if (eventType.equals("STOP")) {
                    bt.addEvent(new Event(Event.STOP, Long.parseLong(st.nextToken()), st.nextToken()));
                } else {
                    bt.addEvent(new Event(Event.ERROR, -1, "Wrong command syntax"));
                }//else
            }//while
        }//run

        public boolean end() {

            this.stop = true;
            //try {
                
                lnr = null;
                
            //} catch (IOException ioe) {
            //    return false;
            //}

            return true;
        }
    }

    protected class Event {

        public static final int START = 0;
        public static final int STOP = 1;
        public static final int END = 2;
        public static final int ERROR = 3;
        private int type;
        private long delay;
        private String torrent;

        public Event(int type, long delay, String torrent) {
            this.type = type;
            this.delay = System.currentTimeMillis() + delay;
            this.torrent = torrent;
        }

        public int getType() {
            return type;
        }

        public long getDelay() {
            return delay;
        }

        public String getTorrent() {
            return this.torrent;
        }
    }
}
