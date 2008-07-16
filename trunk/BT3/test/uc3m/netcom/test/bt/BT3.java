
import jBittorrentAPI.TorrentProcessor;
import jBittorrentAPI.TorrentFile;
import jBittorrentAPI.PeerUpdater;
import jBittorrentAPI.Peer;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Random;
import java.net.InetAddress;



public class BT3{
    
    public static void main(String[] args){
        
        try{
            TorrentProcessor tp = new TorrentProcessor();
            TorrentFile tf = tp.getTorrentFile(tp.parseTorrent(args[0]));
        
            //Aqui hay que generar una ID algo mas elaborada
            byte[] id = new byte[20];
            Random r = new Random();
            r.nextBytes(id);
            PeerUpdater pu = new PeerUpdater(id,tf);
            LinkedHashMap<String,Peer> list = pu.processResponse(pu.contactTracker(id, tf,0,0,tf.total_length,"&event=started"));
            
            java.util.Iterator<String> ki = list.keySet().iterator();
            
            while(ki.hasNext()){
                String k = ki.next();
                Peer p  = (Peer) list.get(k);
                System.out.println(k+" "+p.getID()+" "+p.getIP()+" "+p.getPort());
            }
            
            }catch(Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        
        
    }
}