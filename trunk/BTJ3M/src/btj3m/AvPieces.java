/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package btj3m;

import java.util.*;
/**
 *
 * @author jmcamacho
 */
public class AvPieces <T,P,S> extends LinkedHashMap<T, LinkedHashMap<P,S>>{
    
    public AvPieces(){
        super();
    }

    public Integer rarest(T torrent,P id){
        
        return null;
    }
    
    public Integer random(T torrent, P id){
        return null;
    }
    
    public Integer endGameMode(T torrent, P id){return null;} //???
    
    public ArrayList calculateAvailable(P id){
        
        return null;
    }
    
    public S get(T torrent,P id){
        
        LinkedHashMap l = (LinkedHashMap<P,S>)this.get(torrent);
        return (S)l.get(id);
    }
    
    public void put(T torrent,P id, S set){
        if(this.containsKey(torrent)){
            ((LinkedHashMap)this.get(torrent)).put(id, set);
        }else{
            LinkedHashMap l = new LinkedHashMap<P,S>();
            l.put(id,set);
            this.put(torrent,l);
        }
    }
    
    public Collection<S> values(T torrent){
        return this.get(torrent).values();
    }
    
    public boolean containsKey(T torrent,P key){
        
        LinkedHashMap l = this.get(torrent);
        return l.containsKey(key);
    }
    
    public boolean containsValue(T torrent, S value){
        
        LinkedHashMap l = this.get(torrent);
        return l.containsValue(value);
    }
    
    public Set<P> keySet(T torrent){
        return this.get(torrent).keySet();
    }
    
}
