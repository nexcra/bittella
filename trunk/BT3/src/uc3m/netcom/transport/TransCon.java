
package uc3m.netcom.transport;


import java.net.*;
import java.util.BitSet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import uc3m.netcom.overlay.bt.message.*;
import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.overlay.bt.BTConstants;
import uc3m.netcom.overlay.bt.BTInternRequest;
import jBittorrentAPI.Message;
import jBittorrentAPI.Message_HS;
import jBittorrentAPI.Message_PP;
import jBittorrentAPI.MessageReceiver;
import jBittorrentAPI.MessageSender;
import jBittorrentAPI.IncomingListener;
import jBittorrentAPI.OutgoingListener;

public class TransCon implements IncomingListener,OutgoingListener{
    
    private BTID localID;
    private BTID remoteID;
    private TransLayer layer;
    private TransInfo info;
    private TransMessageListener tml;
    private MessageReceiver mr;
    private MessageSender ms;
    
    public TransCon(BTID localID,BTID remoteID,TransLayer layer,TransInfo info, TransMessageListener listener,Socket s)throws IOException{
      
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
        if(msg instanceof BTPeerMessageBitField){
            ms.addMessageToQueue(new Message_PP(BTMessage.BITFIELD,toByteArray(((BTPeerMessageBitField)msg).getBitset()) ));
        }else if(msg instanceof BTPeerMessageCancel){
            BTPeerMessageCancel cancel = ((BTPeerMessageCancel)msg);
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write(cancel.getPieceNumber());
            ba.write(cancel.getBlockNumber());
            ba.write((int)Math.pow(2,BTConstants.DOCUMENT_DEFAULT_BLOCK_EXPONENT));
            byte[] payload = ba.toByteArray();
            ms.addMessageToQueue(new Message_PP(BTMessage.CANCEL,payload));
        }else if(msg instanceof BTPeerMessageChoke){
            ms.addMessageToQueue(new Message_PP(BTMessage.CHOKE));
        }else if(msg instanceof BTPeerMessageHandshake){
            BTPeerMessageHandshake msh = (BTPeerMessageHandshake)msg;
            byte[] peer_id = msh.getSender().getID();
            byte[] key = msh.getOverlayKey().getBytes();
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
            ba.write((int)Math.pow(2,BTConstants.DOCUMENT_DEFAULT_BLOCK_EXPONENT));
            byte[] payload = ba.toByteArray();
            ms.addMessageToQueue(new Message_PP(BTMessage.REQUEST,payload));            
        }else if(msg instanceof BTPeerMessageUninterested){
            ms.addMessageToQueue(new Message_PP(BTMessage.UNINTERESTED));
        }else if(msg instanceof BTPeerToTrackerRequest){
            
        }
    }
    
    // Returns a bitset containing the values in bytes.
    // The byte-ordering of bytes must be big-endian which means the most significant bit is in element 0.
    public static BitSet fromByteArray(byte[] bytes) {
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
    public static byte[] toByteArray(BitSet bits) {
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
            BTID sender = new BTID();
            sender.setID(msh.getPeerID());
            BTPeerMessageHandshake hs = new BTPeerMessageHandshake(new String(msh.getFileID()),null,sender,null);
            this.tml.messageArrived(new TransMsgEvent(hs,this.info,this.layer,this));
        }else if(m instanceof Message_PP){
            BTMessage msg = generateBTMessage((Message_PP)m);
            
        }

    }
    
    
    private BTMessage generateBTMessage(Message_PP m){
        
        int tipo = m.getType();
        byte[] length = m.getLength();
        byte[] payload = m.getPayload();
        if(length.equals(new byte[length.length])) return new BTPeerMessageKeepAlive("0000",this.remoteID,this.localID);

        
        switch(tipo){
            
            case BTMessage.BITFIELD:
                return new BTPeerMessageBitField(this.fromByteArray(payload),null,this.remoteID,this.localID);
                break;
            case BTMessage.CANCEL:
                ByteArrayInputStream bi = new ByteArrayInputStream(payload);
                int piece = bi.read();
                int block = bi.read();
                return new BTPeerMessageCancel(piece,block,null,this.remoteID,this.localID);
                break;
            case BTMessage.CHOKE:
                return new BTPeerMessageChoke(null,this.remoteID,this.localID);
                break;
            case BTMessage.HAVE:
                ByteArrayInputStream bi2 = new ByteArrayInputStream(payload);
                int piece_num = bi2.read();
                return new BTPeerMessageHave(piece_num,null,this.remoteID,this.localID);
                break;
            case BTMessage.INTERESTED:
                return new BTPeerMessageInterested(null,this.remoteID,this.localID);
                break;
            case BTMessage.PIECE:
                ByteArrayInputStream bi3 = new ByteArrayInputStream(payload);
                int piece_num2 = bi3.read();
                int block_num2 = bi3.read();
                byte[] block2 = new byte[bi3.available()];
                bi3.read(block2);
                return new BTPeerMessagePiece(piece_num2,block_num2,block2,null,this.remoteID,this.localID);
                break;
            case BTMessage.REQUEST:
                ByteArrayInputStream bi4 = new ByteArrayInputStream(payload);
                int piece_num4 = bi4.read();
                int block_num4 = bi4.read();
                int length4 = bi4.read();
                BTInternRequest req = new BTInternRequest(null,null,null,piece_num4,block_num4);
                return new BTPeerMessageRequest(req,this.remoteID,this.localID);
                break;
            case BTMessage.UNCHOKE:
                return new BTPeerMessageUnchoke(null,this.remoteID,this.localID);
                break;
            case BTMessage.UNINTERESTED:
                return new BTPeerMessageUninterested(null,this.remoteID,this.localID);
                break;
            
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
    
    public void stop(){
        this.mr.stopThread();
        this.ms.stopThread();
    }
      
}