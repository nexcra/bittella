/*
 * Java Bittorrent API as its name indicates is a JAVA API that implements the Bittorrent Protocol
 * This project contains two packages:
 * 1. jBittorrentAPI is the "client" part, i.e. it implements all classes needed to publish
 *    files, share them and download them.
 *    This package also contains example classes on how a developer could create new applications.
 * 2. trackerBT is the "tracker" part, i.e. it implements a all classes needed to run
 *    a Bittorrent tracker that coordinates peers exchanges. *
 *
 * Copyright (C) 2007 Baptiste Dubuis, Artificial Intelligence Laboratory, EPFL
 *
 * This file is part of jbittorrentapi-v1.0.zip
 *
 * Java Bittorrent API is free software and a free user study set-up;
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Java Bittorrent API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java Bittorrent API; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @version 1.0
 * @author Baptiste Dubuis
 * To contact the author:
 * email: baptiste.dubuis@gmail.com
 *
 * More information about Java Bittorrent API:
 *    http://sourceforge.net/projects/bitext/
 */
package crawler2;

import jBittorrentAPI.*;
import java.util.*;
import java.io.*;
import java.net.Socket;
/**
 * Object that manages all concurrent downloads. It chooses which piece to request
 * to which peer.
 */
public class HandshakeManager implements DTListener, PeerUpdateListener,
        ConListenerInterface {

    private byte[] clientID;
    private TorrentFile torrent = null;
    private long length = 0;
    private int nbPieces;
    private File dat_file;
    private ConnectionListener cl = null;
    private LinkedHashMap<String, Peer> peerList = null;
    private LinkedHashMap<String, Peer> unknown = null;
    private TreeMap<String, DownloadTask> task = null;
    private HashMap<String,Long> timestamp = null;
    LinkedHashMap unchoken = new LinkedHashMap<String, Integer>();
    private long lastUnchoking = 0;
    private boolean end = false;
    private long cTasks = 0;
    
    /**
     * Create a new manager according to the given torrent and using the client id provided
     * @param torrent TorrentFile
     * @param clientID byte[]
     */
    public HandshakeManager(TorrentFile torrent, final byte[] clientID, File dat_file) {
        this.clientID = clientID;
        this.peerList = new LinkedHashMap<String, Peer>();
        this.unknown = new LinkedHashMap<String, Peer>();
        this.task = new TreeMap<String, DownloadTask>();
        this.timestamp = new HashMap<String,Long>();
        this.torrent = torrent;
        this.nbPieces = torrent.piece_hash_values_as_binary.size();
        this.length = Long.parseLong(Crawler.checkLength(this.torrent.total_length));
        this.dat_file = dat_file;
        this.lastUnchoking = System.currentTimeMillis();
    }

    /**
     * Update the piece availabilities for a given peer
     * @param peerID String
     * @param has BitSet
     */
    public synchronized void peerAvailability(String peerID, BitSet has) {

        BitSet interest = (BitSet) (has.clone());
        int dwl_pieces = interest.cardinality();
        String bit_string = "";

        for (int i = 0; i < this.nbPieces; i++) {
            if (interest.get(i)) {
                bit_string += "1";
            } else {
                bit_string += "0";
            }
        }


        boolean result = this.savePeer(peerID, dwl_pieces, bit_string);
        
        if(!result) System.err.println("ERROR WHILE SAVING PEER INFO: data file might be inconsistent "+this.dat_file.getName());
        else this.unknown.remove(peerID);
        this.taskCompleted(peerID, 0);
    }

    public boolean savePeer(String peerID, long dwl_pieces, String bit_string) {
        
        PrintWriter pw = null;
        
        try{
            pw = new PrintWriter(new BufferedWriter(new FileWriter(this.dat_file,true)));
        }catch(IOException ioe){
             System.out.println("savePeer: "+ioe.getMessage());
             return false;
        }
        
        Peer p  = this.peerList.get(peerID);
        if(p==null) return false;
        String type = "1";//Leecher
        if(dwl_pieces == -1) type = "2"; //Unknown Type
        else if(dwl_pieces == this.nbPieces) type = "0"; //Seed
        
        pw.println(p.getIP()+"\t"+p.getPort()+"\t"+type+"\t"+bit_string);
        pw.flush();
        pw.close();

        return true;
    }

    public synchronized void connect(Peer p) {
        DownloadTask dt = new DownloadTask(p,
                this.torrent.info_hash_as_binary,
                this.clientID, true,
                this.getBitField());
        dt.addDTListener(this);
        dt.start();
    }

    public synchronized void disconnect(Peer p) {
        if(p == null) return;
        DownloadTask dt = task.remove(p.toString());
        if (dt != null) {
            dt.end();
            dt = null;
        }
    }

    /**
     * Given the list in parameter, check if the peers are already present in
     * the peer list. If not, then add them and create a new task for them
     * @param list LinkedHashMap
     */
    public synchronized void updatePeerList(LinkedHashMap list) {
        //this.lastUnchoking = System.currentTimeMillis();
        synchronized (this.task) {
            //this.peerList.putAll(list);
            Set keyset = list.keySet();
            for (Iterator i = keyset.iterator(); i.hasNext();) {
                String key = (String) i.next();
                if (!this.task.containsKey(key)) {
                    Peer p = (Peer) list.get(key);
                    this.peerList.put(p.toString(), p);
                    this.unknown.put(p.toString(), p);
                    this.connect(p);
                }
            }
        }
        System.out.println("Peer List updated from tracker with " + list.size() +
                " peers");
    }

    /**
     * Called when an update try fail. At the moment, simply display a message
     * @param error int
     * @param message String
     */
    public void updateFailed(int error, String message) {
        System.err.println(message);
        System.err.flush();
        this.end = true;
        this.clientID.notifyAll();
    }

    /**
     * Add the download task to the list of active (i.e. Handshake is ok) tasks
     * @param id String
     * @param dt DownloadTask
     */
    public synchronized void addActiveTask(String id, DownloadTask dt) {
        synchronized (this.task) {
            this.task.put(id, dt);
        }
        
        synchronized(this.timestamp){
            this.timestamp.put(id, System.currentTimeMillis());
        }
    }

    /**
     * Called when a new peer connects to the client. Check if it is already
     * registered in the peer list, and if not, create a new DownloadTask for it
     * @param s Socket
     */
    public synchronized void connectionAccepted(Socket s) {
        synchronized (this.task) {

            String id = s.getInetAddress().getHostAddress() +
                    ":" + s.getPort();
            if (!this.task.containsKey(id)) {
                DownloadTask dt = new DownloadTask(null,
                        this.torrent.info_hash_as_binary,
                        this.clientID, false, this.getBitField(), s);
                dt.addDTListener(this);
                this.peerList.put(dt.getPeer().toString(), dt.getPeer());
                this.unknown.put(dt.getPeer().toString(), dt.getPeer());
                this.task.put(dt.getPeer().toString(), dt);
                dt.start();
            }
        }
    }

    /**
     * Compute the bitfield byte array from the isComplete BitSet
     * @return byte[]
     */
    public byte[] getBitField() {
        int l = (int) Math.ceil((double) this.nbPieces / 8.0);
        byte[] bitfield = new byte[l];
        return bitfield;
    }

    /**
     * Create the ConnectionListener to accept incoming connection from peers
     * @param minPort The minimal port number this client should listen on
     * @param maxPort The maximal port number this client should listen on
     * @return True if the listening process is started, false else
     * @todo Should it really be here? Better create it in the implementation
     */
    public boolean startListening(int minPort, int maxPort) {

        this.cl = new ConnectionListener();
        if (this.cl.connect(minPort, maxPort)) {
            this.cl.addConListenerInterface(this);
            return true;
        } else {
            System.err.println("Could not create listening socket...");
            System.err.flush();
            return false;
        }
    }

    /**
     * Removes a task and peer after the task sends a completion message.
     * Completion can be caused by an error (bad request, ...) or simply by the
     * end of the connection
     * @param id Task idendity
     * @param reason Reason of the completion
     */
    public synchronized void taskCompleted(String id, int reason) {
        switch (reason) {
            case DownloadTask.CONNECTION_REFUSED:

                //System.err.println("Connection refused by host " + id);
                break;
            case DownloadTask.MALFORMED_MESSAGE:

                //System.err.println("Malformed message from " + id + ". Task ended...");
                break;
            case DownloadTask.UNKNOWN_HOST:
            //System.err.println("Connection could not be established to " + id + ". Host unknown...");

        }

        this.disconnect(this.peerList.get(id));
        this.cTasks++;
        
        if(cTasks == this.peerList.size()){
            synchronized(this.clientID){
                this.end = true;
                this.clientID.notify();
            }
        }
    //System.err.flush();
    }
    
    public synchronized void pieceCompleted(String peerID, int i,
            boolean complete) {}
    public synchronized void pieceRequested(int i, boolean requested) {}
    public synchronized void peerReady(String peerID) {}
    public synchronized void peerRequest(String peerID, int piece, int begin,
            int length) {}
    
    
    /**
     * Periodically call the unchokePeers method. This is an infinite loop.
     * User have to exit with Ctrl+C, which is not good... Todo is change this
     * method...
     */
    public boolean blockUntilCompletion()throws InterruptedException {

        
            synchronized(this.clientID){
        
                while (!this.end) {

                    this.clientID.wait(60000);
                    long time = System.currentTimeMillis();
                    if(this.lastUnchoking-time > 120000) return false;
                    Iterator<String> it = this.task.keySet().iterator();
                    
                    while(it.hasNext()){
                        
                        String id = it.next();
                        Long t = null;
                        synchronized(this.timestamp){
                            t = this.timestamp.get(id);
                        }
                       
                        if(t.longValue()-time > 180000){
                            this.taskCompleted(id, 0);
                        }
                    }
                }
                
            }
            
            if(this.cTasks != this.peerList.size()) return false;
            
            Iterator<String> uit = this.unknown.keySet().iterator();
            boolean res = true;
            
            while(uit.hasNext() && res){
                String next = uit.next();
                res = this.savePeer(next, -1, "null");
                
            }
            
            if(!res) return false;
            
            return true;
    }

}
