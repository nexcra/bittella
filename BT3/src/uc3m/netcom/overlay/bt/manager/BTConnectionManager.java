package uc3m.netcom.overlay.bt.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import uc3m.netcom.overlay.bt.BTConnection;
import uc3m.netcom.overlay.bt.BTContact;

public class BTConnectionManager {
	
	private Map<BTContact, BTConnection> itsConnections;
	
	private BTContact itsOwnContact;
	
	public BTConnectionManager(BTContact theLocalAddress) {
		this.itsOwnContact = theLocalAddress;
		this.itsConnections = new HashMap<BTContact, BTConnection>(50);
	}
	
	public BTContact getLocalAddress() {
		return this.itsOwnContact;
	}
	
	public void addConnection(BTContact theOtherSide) {
		if (this.itsConnections.containsKey(theOtherSide))
			return;
		BTConnection newConnection = new BTConnection(this.itsOwnContact, theOtherSide);
		this.itsConnections.put(theOtherSide, newConnection);
	}
	
	public BTConnection getConnection(BTContact theOtherSide) {
		if (! this.itsConnections.containsKey(theOtherSide))
			return null;
		return this.itsConnections.get(theOtherSide);
	}
	
	public boolean isConnectionRegisteredTo(BTContact theOtherSide) {
		return this.itsConnections.containsKey(theOtherSide);
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
		for (BTContact aContact : this.itsConnections.keySet()) {
			if (this.itsConnections.get(aContact).isConnected())
				result.add(aContact);
		}
		return result;
	}
	
	public int getNumberOfConnectedContacts() {
		int result = 0;
		for (BTContact aContact : this.itsConnections.keySet()) {
			if (this.itsConnections.get(aContact).isConnected())
				result += 1;
		}
		return result;
	}
	
	public Collection<BTContact> getHandshakingContacts() {
		Collection<BTContact> result = new LinkedList<BTContact>();
		for (BTContact aContact : this.itsConnections.keySet()) {
			if (this.itsConnections.get(aContact).isHandshaking())
				result.add(aContact);
		}
		return result;
	}
	
}
