/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler2;

import jBittorrentAPI.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author jmcamacho
 */
public class Connection2 extends Connection {

    private String meta_data;
    private boolean extended;

    public Connection2(byte[] id, TorrentFile t, String save_path, String local_addr, boolean extended) {

        super(id, t, save_path, local_addr);
        this.extended = extended;
    }

    /**
     * Thread method that regularly contact the tracker and process its response
     */
    @Override
    public void run() {

        int tryNB = 0;
        LinkedHashMap<String, Peer> auxL = new LinkedHashMap<String, Peer>();
        byte[] b = new byte[0];

        while (!this.end) {

            auxL = this.processResponse(this.contactTracker(idf,
                    torrentf, 0,
                    0,
                    this.torrentf.total_length, this.event));
            System.gc();
            if (auxL != null) {

                try{
                    Iterator it = auxL.keySet().iterator();
                    if(it == null) continue;
                    while (it.hasNext()) {
                        String key = (String) it.next();

                        if (!peerL.containsKey(key)) {
                            peerL.put(key, auxL.get(key));
                            tryNB = 0;
                        }
                    }
                }catch(Exception e){
                    System.out.println("Run-It: "+e.getMessage());
                    System.gc();
                }

                if (peerL.size() >= this.num_want) {

                    try {
                        this.end = true;
                        boolean finished = false;

                        if (!extended) {
                            finished = this.savePeers(peerL);
                        } else {
                            HandshakeManager hm = new HandshakeManager(this.torrentf, this.idf, this.dat_file);
                            hm.updatePeerList(peerL);
                            finished = hm.blockUntilCompletion();
                        }
                        
                        if (!finished){
                            finished = this.savePeers(peerL);
                        }
                        
                        if (finished && this.saveMetaData(-1, -1, -1, true)) {
                            Crawler.incCounter();
                            System.out.println("New IP set fetched: " + Crawler.count);
                            System.gc();
                            break;
                        }

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        System.gc();
                    }

                } else { //if numwant < peerList.size
                    tryNB++;
                }


                try {
                    synchronized (b) {
                        b.wait(interval);
                    }
                } catch (InterruptedException ie) {
                    System.out.println(ie.getMessage());
                    ie.printStackTrace();
                    dat_file.delete();
                    this.end = true;
                    System.gc();
                }

            } else { //if auxL == null
                dat_file.delete();
                this.end = true;
                System.gc();
            }

            if (tryNB == 5) {
                dat_file.delete();
                this.end = true;
                System.gc();
            }

        }//while

        Crawler.releaseCon();
    }

    /**
     * Process the map representing the tracker response, which should contain
     * either an error message or the peers list and other information such as
     * the interval before next update, aso
     * @param m The tracker response as a Map
     * @return LinkedHashMap A HashMap containing the peers and their ID as keys
     */
    @Override
    public LinkedHashMap<String, Peer> processResponse(Map m) {

        LinkedHashMap<String, Peer> l = null;
        long complete = 0;
        long incomplete = 0;
        long downloaded = 0;

        if (m != null) {
            if (m.containsKey("failure reason")) {
                System.out.println("The tracker returns the following error message:" +
                        "\t'" +
                        new String((byte[]) m.get(
                        "failure reason")) +
                        "'");
                this.end = true;
                return null;
            } else {
                try {
                    if (((Long) m.get("interval")).intValue() < this.interval) {
                        this.interval = ((Long) m.get("interval")).intValue();
                    }
                    complete = ((Long) m.get("complete"));
                    incomplete = ((Long) m.get("incomplete"));
                    downloaded = ((Long) m.get("downloaded"));
                    this.num_want = complete + incomplete;
                    if (this.num_want == 1) {
                        return null;
                    }
                } catch (NullPointerException e) {
                    return null;
                }//end catch

                System.out.println("Number of Seeds: " + ((Long) m.get("complete")) + " Number of Leechers: " + ((Long) m.get("incomplete")));

                Object peers = m.get("peers");
                ArrayList peerList = new ArrayList();
                l = new LinkedHashMap<String, Peer>();
                if (peers instanceof List) {
                    peerList.addAll((List) peers);
                    if (peerList != null && peerList.size() > 0) {
                        for (int i = 0; i < peerList.size(); i++) {
                            String peerID = new String((byte[]) ((Map) (peerList.get(i))).get(
                                    "peer_id"));
                            String ipAddress = new String((byte[]) ((Map) (peerList.get(
                                    i))).get("ip"));
                            int port = ((Long) ((Map) (peerList.get(i))).get(
                                    "port")).intValue();
                            Peer p = new Peer(peerID, ipAddress, port);
                            l.put(p.toString(), p);
                        }
                    }
                } else if (peers instanceof byte[]) {
                    byte[] p = ((byte[]) peers);
                    for (int i = 0; i < p.length; i += 6) {
                        try{
                            Peer peer = new Peer();
                            peer.setIP(Utils.byteToUnsignedInt(p[i]) + "." +
                                    Utils.byteToUnsignedInt(p[i + 1]) + "." +
                                    Utils.byteToUnsignedInt(p[i + 2]) + "." +
                                    Utils.byteToUnsignedInt(p[i + 3]));
                            peer.setPort(Utils.byteArrayToInt(Utils.subArray(p,
                                    i + 4, 2)));
                            l.put(peer.toString(), peer);
                           }catch(ArrayIndexOutOfBoundsException aio){
                               System.out.println("processResponse: "+aio.getMessage());
                               num_want--;

                           }
                    }
                }//end if instanceof
            }//end if failure_reason

            if (this.first) {
                first = false;
                if (!this.saveMetaData(complete, incomplete, downloaded, false)) {
                    return null;
                }
            }
            return l;

        } else { //m == null;
            return null;
        }
    }

    /**
     * Contact the tracker according to the HTTP/HTTPS tracker protocol and using
     * the information in the TorrentFile.
     * @param id byte[]
     * @param t TorrentFile
     * @param dl long
     * @param ul long
     * @param left long
     * @param event String
     * @return A Map containing the decoded tracker response
     */
    @Override
    public Map contactTracker(byte[] id,
            TorrentFile t, long dl, long ul,
            long left, String event) {
        try {

            URL source = new URL(t.announceURL + "?info_hash=" +
                    t.info_hash_as_url + "&peer_id=" +
                    Utils.byteArrayToURLString(id) + "&port=" +
                    6881 +
                    "&downloaded=" + dl + "&uploaded=" + ul +
                    "&left=" +
                    left + "&numwant=200&compact=1" + event);
            //System.out.println("Contact Tracker. URL source = " + source);   //DAVID
            URLConnection uc = source.openConnection();
            InputStream is = uc.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            // Decode the tracker bencoded response
            Map m = BDecoder.decode(bis);
            bis.close();
            is.close();
            return m;
        } catch (MalformedURLException murle) {
            System.out.println("Tracker URL is not valid... Check if your data is correct and try again");
        } catch (UnknownHostException uhe) {
            System.out.println("Tracker not available... Retrying...");
        } catch (IOException ioe) {
            System.out.println("Tracker unreachable... Retrying " + ioe.getMessage() + "@" + t.announceURL);
        } catch (Exception e) {
            System.out.println("Internal error");
        }
        return null;
    }

    private boolean saveMetaData(long complete, long incomplete, long downloaded, boolean onFile) {

        if (!onFile) {
            String torrentID = this.dat_file.getName();
            torrentID = torrentID.substring(0, torrentID.indexOf('.'));
            long timestamp = System.currentTimeMillis() / 1000;
            long created = this.torrentf.creationDate;
            int piece_length = this.torrentf.pieceLength;
            int num_files = this.torrentf.length.size();
            String total_length = Crawler.checkLength(this.torrentf.total_length);
            this.meta_data = new String(torrentID + "\t" + complete + "\t" + incomplete + "\t" + downloaded + "\t" +
                    timestamp + "\t" + created + "\t" + total_length + "\t" + num_files + "\t" + piece_length);
            return true;

        } else {
            return Crawler.saveMetaData(this.meta_data);
        }
    }

    private boolean savePeers(LinkedHashMap peerList) {

        Iterator it = peerList.values().iterator();
        PrintWriter pw = null;
        
        try{
            pw = new PrintWriter(new BufferedWriter(new FileWriter(this.dat_file,false)));
        }catch(IOException ioe){
            System.out.println(ioe.getMessage());
            return false;
        }

        while (it.hasNext()) {
            Peer p = (Peer) it.next();
            if (!p.getIP().equals(this.local_addr)) {
                pw.println(p.getIP() + "\t" + p.getPort());
            }
        }

        pw.flush();
        pw.close();
        return true;
    }
}
