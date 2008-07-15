
package uc3m.netcom.transport;

import java.net.*;
import java.io.*;


public class TransReceiver extends Thread{
    
    private TransLayer hub;
    private short listening_port;
    private ServerSocket ss;
    private boolean accept_con;
    
    
    public TransReceiver(TransLayer layer, short port){
        
        hub = layer;
        listening_port = port;
        accept_con = true;
        try {
            ss = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public TransLayer getTransLayer(){
        return hub;
    }
    
    public void setTransLayer(TransLayer layer){
        hub = layer;
    }
    
    public short getPort(){
        return listening_port;
    }
    
    public void setPort(short port){
        listening_port = port;
    }
    
    
    public void setAccept(boolean accept){
        
        this.accept_con = accept;
    }
    
    @Override
    public void run(){
        
        while(this.accept_con){
            
            try{
                
                Socket s = ss.accept();
                String ip = s.getInetAddress().getHostAddress();
                short port = (short)s.getPort();
                TransInfo addr = new TransInfo(ip,port);
                TransCon tc = this.hub.createConnection(hub,addr, hub.getDefaultListener());
                hub.addConnection(addr, tc);
                
            }catch(Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            
        }
        
    }
    
    
    
    
}