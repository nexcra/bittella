
package uc3m.netcom.test.bt;
        
import org.apache.commons.math.random.JDKRandomGenerator;
import jBittorrentAPI.TorrentProcessor;
import jBittorrentAPI.TorrentFile;
import java.net.InetAddress;
import uc3m.netcom.common.ContentStorage;
import uc3m.netcom.overlay.bt.*;
import uc3m.netcom.transport.TransLayer;
import uc3m.netcom.transport.TransInfo;



public class BT3{
    
    public static void main(String[] args){
        
        try{
            TorrentProcessor tp = new TorrentProcessor();
            TorrentFile tf = tp.getTorrentFile(tp.parseTorrent(args[0]));
        
            //Aqui hay que generar una ID algo mas elaborada 
            BTID id = new BTID();
            InetAddress addr = InetAddress.getLocalHost();
            BTDataStore dataBus = new BTDataStore();
            BTInternStatistic theStatistic = new BTInternStatistic();
            ContentStorage cs = new ContentStorage();
            BTDocument doc = new BTDocument(tf.info_hash_as_hex,tf.total_length);
            dataBus.storeGeneralData("Statistic", theStatistic, theStatistic.getClass());
            BTTorrent btt = new BTTorrent(tf,tf.total_length,new BTID(),BT3.getTrackerInfo(tf.announceURL));
            dataBus.addTorrent(btt);
            BTPeerDistributeNode peerDistributeNode = new BTPeerDistributeNode(dataBus, id, (short)6881, theStatistic, new JDKRandomGenerator());
            BTPeerSearchNode peerSearchNode = new BTPeerSearchNode(dataBus, id,(short) 6882, (short)6881);
            TransLayer transLayer = new TransLayer(addr.getHostAddress(),(short)6881,peerDistributeNode);
            peerDistributeNode.setTransLayer(transLayer);
            peerSearchNode.setTransLayer(transLayer);
            peerDistributeNode.connect(cs);
            peerSearchNode.connect();
            BTClientApplication clientApplication = new BTClientApplication(dataBus, peerSearchNode, peerDistributeNode);
            clientApplication.connect(cs);
            clientApplication.downloadDocument(btt);
            
            }catch(Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        
        
    }
    
       public static TransInfo getTrackerInfo(String announce){
            
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