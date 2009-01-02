/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package btj3m;

/**
 *
 * @author jmcamacho
 */

import jBittorrentAPI.Peer;
import java.util.LinkedHashMap;
import java.util.EventListener;

public interface DMListener extends EventListener{

    /**
     * This method is called everytime one or more peers are discovered. Peers
     * can be found either by means of the tracker list or a peer contact us or
     * by peer exchange like techniques.
     * 
     * @param dwm       - Torrent download that generated the event.
     * @param newPeers  - List of new peers discovered.
     * 
     */
    public void peerListUpdated(DwManager dwm, LinkedHashMap<String,Peer> newPeers);
    
    
    /**
     * This method is called everytime a BITFIELD or a HAVE message is received.
     *  
     * @param p     - Peer that originated the message.
     * @param dwm   - Torrent download to which the peer belongs.
     */
    public void hasSetUpdated(Peer p, DwManager dwm);
    
    /**
     * This method is called everytime a connection to another peer changes its
     * state (interested/ing, un/choked).
     * 
     * @param p     - 
     * @param dwm
     * @param event
     */
    public void connectionUpdated(Peer p,DwManager dwm,int event);
    
    /**
     * This method is called wether a piece is completed or requested by a peer.
     * 
     * @param p     - Peer requesting the piece or downloaded from.
     * @param dwm   - Torrent download generating the event
     * @param event - Event type (completed or requested)
     * @param piece - Piece number
     */
    public void pieceUpdated(Peer p, DwManager dwm, int event, int piece);
    
    /**
     * 
     * @param dwm
     */
    public void peersUnchoke(DwManager dwm);
    
}
