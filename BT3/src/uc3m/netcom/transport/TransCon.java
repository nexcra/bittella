
package uc3m.netcom.transport;


import java.net.*;
import java.util.BitSet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import uc3m.netcom.overlay.bt.message.*;
import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTInternRequest;
import uc3m.netcom.overlay.bt.BTConnection;
import jBittorrentAPI.Message;
import jBittorrentAPI.Message_HS;
import jBittorrentAPI.Message_PP;
import jBittorrentAPI.MessageReceiver;
import jBittorrentAPI.MessageSender;
import jBittorrentAPI.IncomingListener;
import jBittorrentAPI.OutgoingListener;
import jBittorrentAPI.Utils;

public class TransCon implements IncomingListener,OutgoingListener{
    
    private String overlayKey;
    private BTID localID;
    private BTID remoteID;
    private TransLayer layer;
    private TransInfo info;
    private TransMessageListener tml;
    private MessageReceiver mr;
    private MessageSender ms;
    
    public TransCon(String overlayKey,BTID localID,BTID remoteID,TransLayer layer,TransInfo info, TransMessageListener listener,Socket s)throws IOException{
      
        this.overlayKey = overlayKey;
        this.localID = localID;
        this.remoteID = remoteID;
        this.layer = layer;
        this.info = info;
        this.tml = listener;
        this.mr = new MessageReceiver(info.getNetId()+":"+info.getPort(),s.getInputStream());
        mr.addIncomingListener(this);
        mr.start();
        this.ms = new MessageSender(info.getNetId()+":"+info.getPort(),s.getOutputStream());
        ms.addOutgoingListener(this);
        ms.start();
    }
    
    public TransCon(TransLayer layer,TransInfo info, TransMessageListener listener,Socket s)throws IOException{
      
        this.layer = layer;
        this.info = info;
        this.tml = listener;
        this.mr = new MessageReceiver(info.getNetId()+":"+info.getPort(),s.getInputStream());
        mr.addIncomingListener(this);
        mr.start();
        this.ms = new MessageSender(info.getNetId()+":"+info.getPort(),s.getOutputStream());
        ms.addOutgoingListener(this);
        ms.start();
    }    
    
    
    public void send(BTMessage msg)throws IOException{
        //Traducción a Message
        //ms.addMessageToQueue(msg);
        System.out.println("Sending Message: "+msg.getType());
        if(msg instanceof BTPeerMessageBitField){
            ms.addMessageToQueue(new Message_PP(BTMessage.BITFIELD,toByteArray(((BTPeerMessageBitField)msg).getBitset()) ));
        }else if(msg instanceof BTPeerMessageCancel){
            BTPeerMessageCancel cancel = ((BTPeerMessageCancel)msg);
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write(cancel.getPieceNumber());
            ba.write(cancel.getBlockNumber());
            ba.write(cancel.getChunkSize());
            byte[] payload = ba.toByteArray();
            ms.addMessageToQueue(new Message_PP(BTMessage.CANCEL,payload));
        }else if(msg instanceof BTPeerMessageChoke){
            ms.addMessageToQueue(new Message_PP(BTMessage.CHOKE));
        }else if(msg instanceof BTPeerMessageHandshake){
            BTPeerMessageHandshake msh = (BTPeerMessageHandshake)msg;
            byte[] peer_id = msh.getSender().getID();
            byte[] key = new java.math.BigInteger(msh.getOverlayKey(), 16).toByteArray();
            ms.addMessageToQueue(new Message_HS(key,peer_id));
        }else if(msg instanceof BTPeerMessageHave){
            BTPeerMessageHave have = (BTPeerMessageHave) msg;
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write(have.getPieceNumber());
            byte [] payload = ba.toByteArray();
            ms.addMessageToQueue(new Message_PP(BTMessage.HAVE,payload));
        }else if(msg instanceof BTPeerMessageInterested){
            ms.addMessageToQueue(new Message_PP(BTMessage.INTERESTED));
        }else if(msg instanceof BTPeerMessageKeepAlive){
            ms.addMessageToQueue(new Message_PP());
        }else if(msg instanceof BTPeerMessagePiece){
            BTPeerMessagePiece piece = ((BTPeerMessagePiece)msg);
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write(piece.getPieceNumber());
            ba.write(piece.getBlockNumber());
            ba.write(piece.getPayload());
            byte[] payload = ba.toByteArray();
            ms.addMessageToQueue(new Message_PP(BTMessage.PIECE,payload));           
        }else if(msg instanceof BTPeerMessageUnchoke){
            ms.addMessageToQueue(new Message_PP(BTMessage.UNCHOKE));
        }else if(msg instanceof BTPeerMessageRequest){
            BTPeerMessageRequest req = ((BTPeerMessageRequest)msg);
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write(req.getRequest().getPieceNumber());
            ba.write(req.getRequest().getBlockNumber());
            ba.write(req.getRequest().getChunkLength());
            byte[] payload = ba.toByteArray();
            ms.addMessageToQueue(new Message_PP(BTMessage.REQUEST,payload));            
        }else if(msg instanceof BTPeerMessageUninterested){
            ms.addMessageToQueue(new Message_PP(BTMessage.UNINTERESTED));
        }else if(msg instanceof BTPeerToTrackerRequest){
            
        }
    }
    
    // Returns a bitset containing the values in bytes.
    // The byte-ordering of bytes must be big-endian which means the most significant bit is in element 0.
    private static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i=0; i<bytes.length*8; i++) {
            if ((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    // Returns a byte array of at least length 1.
    // The most significant bit in the result is guaranteed not to be a 1
    // (since BitSet does not support sign extension).
    // The byte-ordering of the result is big-endian which means the most significant bit is in element 0.
    // The bit at index 0 of the bit set is assumed to be the least significant bit.
    private static byte[] toByteArray(BitSet bits) {
//        return Utils.toByteArray(bits);
        byte[] bytes = new byte[bits.length()/8+1];
        for (int i=0; i<bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length-i/8-1] |= 1<<(i%8);
            }
        }
        return bytes;
    }

    public void messageReceived(Message m){
        
        if(m instanceof Message_HS){

            Message_HS msh = (Message_HS) m;
            String peer_id = new java.math.BigInteger(msh.getPeerID()).toString();
            System.out.println("Handshake Received: "+this.info.getNetId()+" "+peer_id);
            BTID sender = new BTID();
            String s = this.info.getNetId()+":"+this.info.getPort();
            sender.setID(s);
            BTContact remote = new BTContact(sender,this.info);
            BTContact local = new BTContact(this.localID,this.layer.getLocalTransInfo((short)0));
            BTConnection connection = new BTConnection(remote,local);
            String key = new java.math.BigInteger(msh.getFileID()).toString(16).toUpperCase();
            BTPeerMessageHandshake hs = new BTPeerMessageHandshake(key,connection,sender,this.localID);
            this.tml.messageArrived(new TransMsgEvent(hs,this.info,this.layer,this));
        }else if(m instanceof Message_PP){
            System.out.println("Protocol Received: "+m.getType());
            BTMessage msg = generateBTMessage((Message_PP)m);
            this.tml.messageArrived(new TransMsgEvent(msg,this.info,this.layer,this));
        }

    }
    
    
    private BTMessage generateBTMessage(Message_PP m){
        
        int tipo = m.getType();
        byte[] length = m.getLength();
        byte[] payload = m.getPayload();
        if(length.equals(new byte[length.length])) return new BTPeerMessageKeepAlive("0000",this.remoteID,this.localID);

        
        switch(tipo){
            
            case BTMessage.BITFIELD:
                return new BTPeerMessageBitField(fromByteArray(payload),this.overlayKey,this.remoteID,this.localID);

            case BTMessage.CANCEL:
                ByteArrayInputStream bi = new ByteArrayInputStream(payload);
                int piece = bi.read();
                int block = bi.read();
                int chunk = bi.read();
                return new BTPeerMessageCancel(piece,block,chunk,this.overlayKey,this.remoteID,this.localID);

            case BTMessage.CHOKE:
                return new BTPeerMessageChoke(this.overlayKey,this.remoteID,this.localID);

            case BTMessage.HAVE:
                ByteArrayInputStream bi2 = new ByteArrayInputStream(payload);
                int piece_num = bi2.read();
                return new BTPeerMessageHave(piece_num,this.overlayKey,this.remoteID,this.localID);

            case BTMessage.INTERESTED:
                return new BTPeerMessageInterested(this.overlayKey,this.remoteID,this.localID);

            case BTMessage.PIECE:
                ByteArrayInputStream bi3 = new ByteArrayInputStream(payload);
                int piece_num2 = bi3.read();
                int block_num2 = bi3.read();
                int chunk_num2 = bi3.available();
                byte[] block2 = new byte[chunk_num2];
                try{
                    bi3.read(block2);
                }catch(IOException ioe){
                    System.out.println(ioe.getMessage());
                    ioe.printStackTrace();
                }
                System.out.println("Piece Received: "+piece_num2+" "+block_num2);
                return new BTPeerMessagePiece(piece_num2,block_num2,chunk_num2,block2,this.overlayKey,true,this.remoteID,this.localID);
                
            case BTMessage.REQUEST:
                ByteArrayInputStream bi4 = new ByteArrayInputStream(payload);
                int piece_num4 = bi4.read();
                int block_num4 = bi4.read();
                int length4 = bi4.read();
                BTContact requesting = new BTContact(this.remoteID,this.info);
                BTContact requested = new BTContact(this.localID,this.layer.getLocalTransInfo((short)0));
                BTInternRequest req = new BTInternRequest(requesting,requested,this.overlayKey,piece_num4,block_num4,length4);
                System.out.println("Request Received: "+piece_num4+" "+block_num4);
                return new BTPeerMessageRequest(req,this.remoteID,this.localID);

            case BTMessage.UNCHOKE:
                return new BTPeerMessageUnchoke(this.overlayKey,this.remoteID,this.localID);

            case BTMessage.UNINTERESTED:
                return new BTPeerMessageUninterested(this.overlayKey,this.remoteID,this.localID);
            
            default:
                return null;
            
        }
        
    }
    
    public void keepAliveSent(){
        System.out.println("Keep Alive Sent");
    }
    
    public void connectionClosed(){
        System.out.println("Connection Closed");
    }
    
    public TransInfo getTransInfo(){
        return this.info;
    }
    
    public TransMessageListener getTML(){
        return this.tml;
    }
    
    public void setLocalID(BTID local){
        this.localID = local;
    }
    public void stop(){
        this.mr.stopThread();
        this.ms.stopThread();
    }
      
}