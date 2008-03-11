/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uc3m.netcom.peerfactsim.impl.util.logging;


import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.overlay.bt.message.*;
import de.tud.kom.p2psim.impl.simengine.Simulator;
/**
 *
 * @author jmcamacho
 */
public class UC3MLogBT {
    
    private final static String props = "UC3MLogBT.properties";
    private String out = "UC3MLogBT_";
    private PrintWriter output;
    private HashMap<String,String> methods;
    private HashMap<String,Class[]> paramTypes;
    private String lastOut = "";
    
    public UC3MLogBT(){
       
        try{
            
            LineNumberReader reader = new LineNumberReader(new FileReader(new File(props)));
            File file = new File(out+System.currentTimeMillis()+".dat");
            file.createNewFile();
            this.output = new PrintWriter(new FileWriter(file));
            String pointer = "";
            this.methods = new HashMap<String,String>();
            this.paramTypes = new HashMap<String,Class[]>();
        
            while(pointer != null){
            
                pointer = reader.readLine();
                if(pointer == null || pointer.length()==0 || pointer.startsWith("#")) continue;
                StringTokenizer tok = new StringTokenizer(pointer,",",false);
                String c = tok.nextToken();
                methods.put(c, tok.nextToken());
                int l = tok.countTokens();
                Class[] classes  = new Class[l];
            
                for(int i = 0;i<l;i++){
                    classes[i]  = Class.forName(tok.nextToken());
                
                }
                this.paramTypes.put(c, classes);
            }
        
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void finish()throws IOException{
        output.flush();
        output.close();
        
    }
    
    public void process(String org1,Object[] params){
        
        try{
            String org = org1.substring(org1.indexOf(" ")+1);
            String method = methods.get(org);
            Class[] param = paramTypes.get(org);
            java.lang.reflect.Method m = this.getClass().getMethod(method,param);
            m.invoke(this, params);
            }catch(Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
    }
      
    
   public void processOperationFinished(de.tud.kom.p2psim.overlay.bt.operation.BTOperationDownload opd,java.lang.Long sim_t,java.lang.Boolean finished){
       
       if(!finished) return;
       
       String node = opd.getComponent().getHost().getOverlay(de.tud.kom.p2psim.api.overlay.DistributionStrategy.class).getOverlayID().toString();
       String time = String.valueOf(sim_t);
       String real_t  = this.simTime2RealTime(sim_t.longValue());
       String outline = node+" "+time+" "+real_t;
       if(!outline.equals(lastOut)){
           output.println(outline);
           lastOut = outline;
       }
       
   }
   
   
   private String simTime2RealTime(long sim_t){
       long t_msec  = sim_t/Simulator.MILLISECOND_UNIT;
       long t_sec = t_msec/1000;
       long t_min  = t_sec/60;
       long t_h = t_min/60;
       t_msec -= (t_sec*1000);
       t_sec -= (t_min*60);
       t_min -= (t_h*60);
       String t_aux = "";
            if(t_h<10) t_aux+="0";
            t_aux += t_h+":";
            if(t_min<10) t_aux+="0";
            t_aux += t_min+":";
            if(t_sec < 10) t_aux+="0";
            t_aux += t_sec+".";
            if(t_msec < 100) t_aux+="0";
            if(t_msec < 10) t_aux+="0";
            t_aux += t_msec;
       
        return t_aux;
       
   }
    
   public void processMessage(NetMessage msg,Long eatl,String sense){
        
            long eat = eatl.longValue();
            BTMessage ms = (BTMessage) msg.getPayload().getPayload();
            String sender = ms.getSender().toString();
            String receiver = ms.getReceiver().toString();
            String type = ms.getType().toString();
            if(type.equals("HAVE"))
                type += "\t"+((BTPeerMessageHave)ms).getPieceNumber()+"\t  ";
            else if(type.equals("PIECE"))
                type += "\t"+((BTPeerMessagePiece)ms).getPieceNumber()+"\t"+((BTPeerMessagePiece)ms).getBlockNumber();
            else if(type.equals("REQUEST"))
                type += "\t"+((BTPeerMessageRequest)ms).getRequest().getPieceNumber()+"\t"+((BTPeerMessageRequest)ms).getRequest().getBlockNumber();
            else if(type.equals("CANCEL"))
                type += "\t"+((BTPeerMessageCancel)ms).getPieceNumber()+"\t"+((BTPeerMessageCancel)ms).getBlockNumber();
            else if(type.equals("TRACKER_REQUEST"))
                type = "TR_RQ\t  \t  ";
            else if(type.equals("TRACKER_REPLY"))
                type = "TR_RP\t  \t  ";
            else if(type.equals("INTERESTED"))
                type = "INTER\t  \t  ";
            else if(type.equals("UNINTERESTED"))
                type = "UNINTR\t  \t  ";
            else if(type.equals("HANDSHAKE"))
                type = "HANDSHK\t  \t  ";
            else if(type.equals("BITFIELD"))
                type = "BITFLD\t  \t  ";
            else if(type.equals("KEEPALIVE"))
                type = "KEEPAL\t  \t  ";
            else
                type += "\t  \t  ";
            
            long t = ms.getTimestamp();
            long size = ms.getSize();
            
            long t_msec  = t/Simulator.MILLISECOND_UNIT;
            long t_sec = t_msec/1000;
            long t_min  = t_sec/60;
            long t_h = t_min/60;
            t_msec -= (t_sec*1000);
            t_sec -= (t_min*60);
            t_min -= (t_h*60);
            String t_aux = "";
            if(t_h<10) t_aux+="0";
            t_aux += t_h+":";
            if(t_min<10) t_aux+="0";
            t_aux += t_min+":";
            if(t_sec < 10) t_aux+="0";
            t_aux += t_sec+".";
            if(t_msec < 100) t_aux+="0";
            if(t_msec < 10) t_aux+="0";
            t_aux += t_msec;
            
            t_msec = eat/Simulator.MILLISECOND_UNIT;
            t_sec = t_msec/1000;
            t_min  = t_sec/60;
            t_h = t_min/60;
            t_msec -= (t_sec*1000);
            t_sec -= (t_min*60);
            t_min -= (t_h*60);
            String eat_aux = "";
            if(t_h<10) eat_aux+="0";
            eat_aux += t_h+":";
            if(t_min<10) eat_aux+="0";
            eat_aux += t_min+":";
            if(t_sec < 10) eat_aux+="0";
            eat_aux += t_sec+".";
            if(t_msec < 100) eat_aux+="0";
            if(t_msec < 10) eat_aux+="0";
            eat_aux += t_msec;
            
            String file_output = null;
            
            if(sense.equals("Sending"))
                file_output = sense+"\t"+sender+"\t"+receiver+"\t"+type+"\t"+size+"\t"+t+"\t"+t_aux+"\t"+eat+"\t"+eat_aux;
            else
                file_output = sense+"\t"+sender+"\t"+receiver+"\t"+type+"\t"+size+"\t"+eat+"\t"+eat_aux+"\t"+t+"\t"+t_aux;
            
            output.println(file_output);
            
            
            
            
        
        
    }
   
   
   
    
    
            
}
