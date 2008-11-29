/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

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
import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author jmcamacho
 */
public class Connection extends Thread {// extends PeerUpdater{

    private File dat_file;
    private byte[] idf;
    private TorrentFile torrentf;
    private String event = "&event=started";
    private int interval = 150;
    private boolean first = true;
    private boolean end = false;
    private LinkedHashMap<String, Peer> peerL;
    private long num_want;
    private String local_addr;

    public Connection(byte[] id, TorrentFile t, String save_path, String local_addr) {

        // super(id,t);
        this.torrentf = t;
        this.idf = id;
        dat_file = new File(save_path);
        try {
            dat_file.createNewFile();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        this.num_want = 0;
        this.local_addr = local_addr;
        this.peerL = new LinkedHashMap<String, Peer>();

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
            tryNB++;

            auxL = this.processResponse(this.contactTracker(idf,
                    torrentf, 0,
                    0,
                    this.torrentf.total_length, this.event));
            if (auxL != null) {
                /*      if (first) {
                this.event = "";
                first = false;
                }
                
                }*/
                tryNB = 0;
                Iterator it = auxL.keySet().iterator();

                while (it.hasNext()) {
                    String key = (String) it.next();

                    if (!peerL.containsKey(key)) {
                        peerL.put(key, auxL.get(key));
                    }
                }


                if (peerL.size() >= this.num_want) {

                    try {
                        this.end = true;
                        Iterator itf = peerL.values().iterator();
                        PrintWriter pw = new PrintWriter(dat_file);

                        while (itf.hasNext()) {
                            Peer p = (Peer) itf.next();
                            if (!p.getIP().equals(local_addr)) {
                                pw.println(p.getIP() + " " + p.getPort());
                            }
                        }


                        pw.flush();
                        pw.close();
                        synchronized (Crawler.class) {
                            Crawler.count++;
                            System.out.println("New IP set fetched: " + Crawler.count);
                        }

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }

                } else {
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

            } else {
                dat_file.delete();
                this.end = true;
                System.gc();
                

            }

            if (tryNB == 5) {
                dat_file.delete();
                this.end = true;
                System.gc();
                continue;
            }

        }

        synchronized (Crawler.lock) {
            Crawler.open_con--;
            Crawler.lock.notify();
        }
    }

    /**
     * Process the map representing the tracker response, which should contain
     * either an error message or the peers list and other information such as
     * the interval before next update, aso
     * @param m The tracker response as a Map
     * @return LinkedHashMap A HashMap containing the peers and their ID as keys
     */
    public synchronized LinkedHashMap<String, Peer> processResponse(Map m) {
        LinkedHashMap<String, Peer> l = null;
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
                
                    this.num_want = ((Long) m.get("complete")) + ((Long) m.get("incomplete"));
                    if (this.num_want == 1) {
                        return null;
                    }
                } catch (NullPointerException e) {
                    return null;
                }

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
                        Peer peer = new Peer();
                        peer.setIP(Utils.byteToUnsignedInt(p[i]) + "." +
                                Utils.byteToUnsignedInt(p[i + 1]) + "." +
                                Utils.byteToUnsignedInt(p[i + 2]) + "." +
                                Utils.byteToUnsignedInt(p[i + 3]));
                        peer.setPort(Utils.byteArrayToInt(Utils.subArray(p,
                                i + 4, 2)));
                        l.put(peer.toString(), peer);
                    }
                }
            }
            return l;
        } else {
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
    public synchronized Map contactTracker(byte[] id,
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
            System.out.println("Tracker unreachable... Retrying " + ioe.getMessage());
        } catch (Exception e) {
            System.out.println("Internal error");
        }
        return null;
    }
}
