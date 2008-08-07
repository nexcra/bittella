
package uc3m.netcom.transport;

import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.overlay.bt.BTTorrent;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.message.BTMessage;
import uc3m.netcom.overlay.bt.message.BTPeerToTrackerRequest;
import uc3m.netcom.overlay.bt.message.BTTrackerToPeerReply;
import jBittorrentAPI.PeerUpdater;
import jBittorrentAPI.Peer;
import jBittorrentAPI.TorrentFile;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Iterator;



public class TransTrackerCon{

    private PeerUpdater pu;
    private TransMessageCallback tmc;
    
    
    public TransTrackerCon(BTID id, TorrentFile torrent,TransMessageCallback tmc,int port){
        pu = new PeerUpdater(id.getID(),torrent);
        pu.setListeningPort(port);
        this.tmc = tmc;
    }
    
        public void sendAndWait(BTPeerToTrackerRequest request){
             LinkedHashMap<String,Peer> list = pu.processResponse(pu.contactTracker(request.getPeerID().getID(), request.getDocument(),request.getDownloaded(),request.getUploaded(),request.getLeft(),request.getReason()));
             LinkedList<BTContact> t_list = new LinkedList<BTContact>();
             if(list != null){
                
                Iterator<String> it = list.keySet().iterator();
                while(it.hasNext()){
                    String key = it.next();
                    Peer p = list.get(key);
                    BTID id = new BTID();
                    id.setID(key);
                    BTContact c = new BTContact(id,new TransInfo(p.getIP(),p.getPort()));
                    t_list.add(c);
                }
             }
             BTTrackerToPeerReply reply =  new BTTrackerToPeerReply(t_list,request.getPeerID(),request.getTrackerID());
             tmc.receive((BTMessage)reply, getTrackerInfo(request.getDocument().announceURL),0);
    }
        
        
        public TransInfo getTrackerInfo(String announce){
            
            int colon = announce.indexOf(":");
            
            if(colon == -1){
                String ip = announce.substring(7,colon);
                short port = (short) Short.parseShort(announce.substring(colon+1));
                
                return new TransInfo(ip,port);
            }else{
                String ip = announce.substring(7);
                return new TransInfo(ip,(short)80);
            }
        }
    
}


