
package uc3m.netcom.transport;


import java.net.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.IOException;
import uc3m.netcom.overlay.bt.message.*;


public class TransCon extends Thread{
    
    private TransInfo info;
    private TransMessageListener tml;
    private TransMessageCallback tmc;
    private InputStream in;
    private OutputStream out;
    private boolean idle;
    private boolean end = false;
    
    public TransCon(TransInfo info, TransMessageListener listener,Socket s,boolean idle)throws IOException{
        this.info = info;
        this.tml = listener;
        this.in = s.getInputStream();
        this.out = s.getOutputStream();
        this.idle = idle;
    }
    
    public TransCon(TransInfo info, TransMessageCallback tmc,Socket s,boolean idle)throws IOException{
        this.info = info;
        this.tmc = tmc;
        this.in = s.getInputStream();
        this.out = s.getOutputStream();
        this.idle = idle;
    }    
    
    
    @Override
    public void run(){
        
        if(this.idle){
            int type = 0;
            
            while(!end && type != -1){
                try{
                    int length = this.in.read();
                    
                    if(length==0) type = BTMessage.KEEPALIVE;
                    else type = this.in.read();
                
                    switch(type){
                        
                        case BTMessage.KEEPALIVE:
                            tml.messageArrived(new TransMsgEvent(new BTPeerMessageKeepAlive(),this.info,this));
                            break;
                        case BTMessage.CHOKE:
                            break;
                        case BTMessage.UNCHOKE:
                            break;
                        case BTMessage.INTERESTED:
                            break;
                        case BTMessage.UNINTERESTED:
                            break;
                        case BTMessage.HAVE:
                            break;
                        case BTMessage.BITFIELD:
                            break;
                        case BTMessage.REQUEST:
                            break;
                        case BTMessage.PIECE:
                            break;
                        case BTMessage.CANCEL:
                            break;
                        case BTMessage.PORT:
                            break;                    
                    }
                    
                }catch(IOException e){
                    type = -1;
                }
            }
        }else{
            
        }
    }
    
    public void send(byte[] data)throws IOException{
        this.out.write(data);
        this.out.flush();
    }
    
    public void send(String query)throws IOException{
        PrintWriter pw  = new PrintWriter(this.out);
        pw.println(query);
        pw.flush();
    }
    
    public void close()throws IOException{
        this.out.flush();
        this.out.close();
        this.in.close();
    }
    
    public boolean isIDLE(){
        return this.idle;
    }
    
    public TransInfo getTransInfo(){
        return this.info;
    }
    
    public TransMessageListener getTML(){
        return this.tml;
    }
    
    public TransMessageCallback getTMC(){
        return this.tmc;
    }
    
}