/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uc3m.netcom.peerfactsim.api.network;


import de.tud.kom.p2psim.api.network.NetLatencyModel;
import de.tud.kom.p2psim.api.network.NetLayer;

/**
 *
 * @author jmcamacho
 */
public interface UC3MNetLatencyModel extends NetLatencyModel{

    public abstract long getLatency(NetLayer sender, NetLayer receiver);
    public abstract long getLatency(NetLayer sender, NetLayer receiver, long packetLength);
}
