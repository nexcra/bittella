package uc3m.netcom.overlay.bt.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import uc3m.netcom.overlay.bt.BTConnection;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.transport.TransInfo;

public class BTConnectionManager {
	
	private Map<String, BTConnection> itsConnections;
	
	private BTContact itsOwnContact;
	
	public BTConnectionManager(BTContact theLocalAddress) {
		this.itsOwnContact = theLocalAddress;
		this.itsConnections = new HashMap<String, BTConnection>(50);
	}
	
	public BTContact getLocalAddress() {
		return this.itsOwnContact;
	}
	
	public void addConnection(BTContact theOtherSide) {
            
                String key = theOtherSide.getTransInfo().getNetId()+":"+theOtherSide.getTransInfo().getPort();
		if (this.itsConnections.containsKey(key))
			return;
		BTConnection newConnection = new BTConnection(this.itsOwnContact, theOtherSide);
		this.itsConnections.put(key, newConnection);
                System.out.println("Connection: "+newConnection);
	}
	
	public BTConnection getConnection(BTContact theOtherSide) {
            
            String key = theOtherSide.getTransInfo().getNetId()+":"+theOtherSide.getTransInfo().getPort();
		if (! this.itsConnections.containsKey(key))
			return null;
		return this.itsConnections.get(key);
	}
	
	public boolean isConnectionRegisteredTo(BTContact theOtherSide) {
            
            String key = theOtherSide.getTransInfo().getNetId()+":"+theOtherSide.getTransInfo().getPort();
		return this.itsConnections.containsKey(key);
	}
	
//	public void handshakingWith(BTContact theOtherSide) {
//		this.itsConnections.get(theOtherSide).handshaking();
//	}
//	
//	public void connectedTo(BTContact theOtherSide) {
//		this.itsConnections.get(theOtherSide).connected();
//	}
//	
//	public void disconnectedFrom(BTContact theOtherSide) {
//		this.itsConnections.get(theOtherSide).disconnected();
//	}
//	
//	public boolean isConnectedTo(BTContact theOtherSide) {
//		if (! this.itsConnections.containsKey(theOtherSide))
//			return false;
//		return this.itsConnections.get(theOtherSide).isConnected();
//	}
//	
//	public boolean isHandshakingWith(BTContact theOtherSide) {
//		if (! this.itsConnections.containsKey(theOtherSide))
//			return false;
//		return this.itsConnections.get(theOtherSide).isHandshaking();
//	}
//	
//	public long isHandshakingSince(BTContact theOtherSide) {
//		if (! this.itsConnections.containsKey(theOtherSide))
//			return BTConnection.getHandshakingTimeFalseValue();
//		return this.itsConnections.get(theOtherSide).isHandshakingSince();
//	}
	
	public Collection<BTContact> getConnectedContacts() {
		Collection<BTContact> result = new LinkedList<BTContact>();
		for (String aContact : this.itsConnections.keySet()) {
			if (this.itsConnections.get(aContact).isConnected()){
                            String addr = aContact.substring(0,aContact.indexOf(":"));
                            String port = aContact.substring(aContact.indexOf(":")+1);
                            BTID id = new BTID();
                            id.setID(aContact);
                            BTContact bc = new BTContact(id,new TransInfo(addr,Integer.parseInt(port)));
                            result.add(bc);
                       }
		}
		return result;
	}
	
	public int getNumberOfConnectedContacts() {
		int result = 0;
		for (String aContact : this.itsConnections.keySet()) {
			if (this.itsConnections.get(aContact).isConnected())
				result += 1;
		}
		return result;
	}
	
	public Collection<BTContact> getHandshakingContacts() {
		Collection<BTContact> result = new LinkedList<BTContact>();
		for (String aContact : this.itsConnections.keySet()) {
			if (this.itsConnections.get(aContact).isHandshaking()){
                            String addr = aContact.substring(0,aContact.indexOf(":"));
                            String port = aContact.substring(aContact.indexOf(":")+1);
                            BTID id = new BTID();
                            id.setID(aContact);
                            BTContact bc = new BTContact(id,new TransInfo(addr,Integer.parseInt(port)));
                            result.add(bc);                            
                        }
			
		}
		return result;
	}
	
}
