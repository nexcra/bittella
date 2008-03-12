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
    private String out;
    private HashMap<String,PrintWriter> output;
    private HashMap<String,String> methods;
    private HashMap<String,Class[]> paramTypes;
    private String lastOut = "";
    
    public UC3MLogBT(){
       
        try{
            
            LineNumberReader reader = new LineNumberReader(new FileReader(new File(props)));
            String pointer = "";
            this.methods = new HashMap<String,String>();
            this.paramTypes = new HashMap<String,Class[]>();
            this.output = new HashMap<String,PrintWriter>();
            
            while(pointer != null){
            
                pointer = reader.readLine();
                if(pointer == null || pointer.length()==0 || pointer.startsWith("#")) continue;
                StringTokenizer tok = new StringTokenizer(pointer,",",false);
                out = tok.nextToken();
                String c = tok.nextToken();
                
                File file = new File(out);
                if(!file.exists()){
                       file.createNewFile();
                       this.output.put(c,new PrintWriter(new FileWriter(file,false)));
                }else{
                       this.output.put(c,new PrintWriter(new FileWriter(file,true)));
                }
                
                methods.put(c, tok.nextToken());
                int l = tok.countTokens();
                Class[] classes  = new Class[l+1];
                classes[l] = PrintWriter.class;
                for(int i = 0;i<l;i++){
                    classes[i]  = Class.forName(tok.nextToken());
                
                }
                this.paramTypes.put(c,classes);
            }
        
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void finish()throws IOException{
        java.util.Collection<PrintWriter> collec = output.values();
        for(PrintWriter pw : collec){
            pw.flush();
            pw.close();
        }
        
    }
    
    public void process(String org1,Object[] params1){
        
        try{
            String org = org1.substring(org1.indexOf(" ")+1);
            String method = methods.get(org);
            Class[] param = paramTypes.get(org);
            java.lang.reflect.Method m = this.getClass().getMethod(method,param);
            Object[] objs = new Object[params1.length+1];
            System.arraycopy(params1, 0, objs, 0, params1.length);
            objs[objs.length-1] = output.get(org);
            m.invoke(this, objs);
            }catch(Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
    }
      
    
   public void processOperationFinished(de.tud.kom.p2psim.overlay.bt.operation.BTOperationDownload opd,java.lang.Long sim_t,java.lang.Boolean finished,PrintWriter pw){
       
       if(!finished) return;
       
       //String node = opd.getComponent().getHost().getOverlay(de.tud.kom.p2psim.api.overlay.DistributionStrategy.class).getOverlayID().toString();
       String time = String.valueOf(sim_t);
       //String real_t  = this.simTime2RealTime(sim_t.longValue());
       String outline = time;
       if(!outline.equals(lastOut)){
           pw.print(outline+" ");
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
    
   public void processMessage(NetMessage msg,Long eatl,String sense,PrintWriter pw){
        
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
            
            pw.println(file_output);
        
    }
   
   
   
    
    
            
}
