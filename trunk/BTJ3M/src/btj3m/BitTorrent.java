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
import java.io.FileReader;
/**
 *
 * @author jmcamacho
 */
public class BitTorrent implements DMListener{ //EEListener

    private static int init_port = 6881;
    private static boolean end = false;
    private HashMap<Integer,AvPieces> pieceManagers;
    private HashMap<Integer,BTUnchokeNum> unchokeAlgs;
    private ArrayList<DwManager> dwManagers;
    private HashMap<String,DwManager> torrentMap;
    private HashMap<Long,Event> events;
    private ArrayList<Long> timestamps;
    //private ExternalEventManager ext;
    private static LinkedHashMap<Integer,String> savepaths;
    
    
    
    
     /**
     * This method is called everytime one or more peers are discovered. Peers
     * can be found either by means of the tracker list or a peer contact us or
     * by peer exchange like techniques.
     * 
     * @param dwm       - Torrent download that generated the event.
     * @param newPeers  - List of new peers discovered.
     * 
     */
    public void peerListUpdated(DwManager dwm, LinkedHashMap<String,Peer> newPeers){}
    
    
    /**
     * This method is called everytime a BITFIELD or a HAVE message is received.
     *  
     * @param p     - Peer that originated the message.
     * @param dwm   - Torrent download to which the peer belongs.
     */
    public void hasSetUpdated(Peer p, DwManager dwm){}
    
    /**
     * This method is called everytime a connection to another peer changes its
     * state (interested/ing, un/choked).
     * 
     * @param p     - 
     * @param dwm
     * @param event
     */
    public void connectionUpdated(Peer p,DwManager dwm,int event){}
    
    /**
     * This method is called wether a piece is completed or requested by a peer.
     * 
     * @param p     - Peer requesting the piece or downloaded from.
     * @param dwm   - Torrent download generating the event
     * @param event - Event type (completed or requested)
     * @param piece - Piece number
     */
    public void pieceUpdated(Peer p, DwManager dwm, int event, int piece){}
    
    /**
     * 
     * @param dwm
     */
    public void peersUnchoke(DwManager dwm){}
    
    
    private boolean parseInitFile(String file){
            
            ArrayList<String> files = new ArrayList<String>();
            ArrayList<Float> maxUR = new ArrayList<Float>();
            ArrayList<Float> maxDR = new ArrayList<Float>();
            ArrayList<Boolean> enabled = new ArrayList<Boolean>();
            ArrayList<Integer> initU = new ArrayList<Integer>();
            ArrayList<String> paths = new ArrayList<String>();
            ArrayList<DwManager> dwm = new ArrayList<DwManager>();
            
            TorrentProcessor tp = new TorrentProcessor();
            LineNumberReader lnr = null;
            try{
                lnr = new LineNumberReader(new FileReader(new File(file)));
                }catch(IOException e){
                    return false;
                }
            String line = "";
            
            while(line!=null){
                
                try{
                    line = lnr.readLine();
                    }catch(IOException e){
                        continue;
                    }
                if(line==null) continue;
                StringTokenizer st = new StringTokenizer(line,"\t");
                
                if(st.countTokens() < 4) continue;
                files.add(st.nextToken());
                maxUR.add(Float.parseFloat(st.nextToken()));
                maxDR.add(Float.parseFloat(st.nextToken()));
                if(st.countTokens() < 1) paths.add("./");
                else paths.add(st.nextToken());
                if(st.countTokens() < 1) enabled.add(false);
                else enabled.add(Boolean.getBoolean(st.nextToken()));
                if(st.countTokens() < 1) initU.add(4);
                else initU.add(Integer.parseInt(st.nextToken()));
                
                
            }//while
            
            
            for(int i=0;i<files.size();i++){
                TorrentFile t = tp.getTorrentFile(tp.parseTorrent(files.get(i)));
                AvPieces pManager = new AvPieces(t.piece_hash_values_as_binary.size());
                boolean en = enabled.get(i);
                int init_u = initU.get(i);
                BTUnchokeNum unAlg = new BTUnchokeNum();
                unAlg.setup(maxUR.get(i), maxDR.get(i),en,init_u);
                DwManager dm = new DwManager(t, Utils.generateID(),pManager,unAlg);
                BitTorrent.savepaths.put(dm.hashCode(), paths.get(i));
                this.pieceManagers.put(dm.hashCode(), pManager);
                this.unchokeAlgs.put(dm.hashCode(), unAlg);
                this.torrentMap.put(files.get(i), dm);
                dwm.add(dm);
            }
            
            return true;
        
    }
    
    public boolean parseEventFile(String file){
        
            LineNumberReader lnr = null;
            try{
                lnr = new LineNumberReader(new FileReader(new File(file)));
                }catch(IOException e){
                    return false;
                }
            String line = "";
            
            while(line!=null){
                
                try{
                    line = lnr.readLine();
                    }catch(IOException e){
                        continue;
                    }
                if(line == null) continue;
                StringTokenizer st = new StringTokenizer(line,"\t");
                if(st.countTokens() == 3){
                    Event e = new Event(Integer.parseInt(st.nextToken()),Long.parseLong(st.nextToken()),st.nextToken());
                    this.addEvent(e);
                }
           }
            
            return true;
    }
    
    public void addEvent(Event ev){
        
        this.events.put(ev.getDelay(),ev);
        
        synchronized(this.timestamps){
            this.timestamps = new ArrayList(this.events.keySet());
            java.util.Collections.sort(this.timestamps);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
            if(args.length < 1){
                System.err.println(
                        "Incorrect use, please provide the path of the config file...\r\n" +
                        "\r\nUsage:\r\n"+
                        "BitTorrent <config_file>");

                System.exit(1);
            }
            
            BitTorrent bt = new BitTorrent();
            
            if(!bt.parseInitFile(args[0])){
                System.out.println("Error while processing torrent files. Exiting Client...");
                System.exit(-1);
            }
            
            if(!bt.parseEventFile(args[1])){
                System.err.println("Error while processing event files. Exiting Client");
            }
            
  /*NOTA: Falta crear:
   *    1.- El gestor de eventos externos (Ver manejo de señales).
   *    3.- Bucle que ejecuta las acciones de los eventos
   *    4.- Colocar en los metodos en el DwManager donde se generan los eventos
   * */         
            
//            for(int i=0;i<initT.size();i++){
//                long wTime = initT.get(i);
//                try{
//                    Thread.currentThread().wait(wTime);
//                }catch(InterruptedException e){}
//                 DwManager dm = dwm.get(i);
//                 dm.startTrackerUpdate();
//                 dm.start();
//                 dm.stopTrackerUpdate();
//                 dm.closeTempFiles();
//            } 

    }
    
    private class ExtEventHandler{
        
        
        
        
        
        
    }
    
    protected class Event{
        
        public static final int START = 0;
        public static final int STOP = 1;
        private int type;
        private long delay;
        private String torrent;
        
        public Event(int type,long delay,String torrent){
            this.type = type;
            this.delay = delay;
            this.torrent = torrent;
        }
        
        public int getType(){
            return type;
        }
        
        public long getDelay(){
            return delay;
        }
        
        public String getTorrent(){
            return this.torrent;
        }
        
        
    }

}
