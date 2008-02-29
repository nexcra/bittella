/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uc3m.netcom.peerfactsim.overlay.bt.algorithm;

/**
 *
 * @author jmcamacho
 */
public class BTAlgorithmChoking {

    
    
            public int getRegularUnchokeNumber(boolean asLeecher){
            
                if(asLeecher) return theirRegularUnchokeNumberAsLeecher;
                else return theirRegularUnchokeNumberAsSeeder;            
            
        }
        
        public void setRegularUnchokeNumber(int number, boolean asLeecher){
            
            if(asLeecher) theirRegularUnchokeNumberAsLeecher = number;
            else theirRegularUnchokeNumberAsSeeder = number;
            
        } 
        
        public int getOptimisticUnchokeNumber(boolean asLeecher){
            
                if(asLeecher) return theirOptimisticUnchokeNumberAsLeecher;
                else return theirOptimisticUnchokeNumberAsSeeder;            
            
        }
        
        public void setOptimisticUnchokeNumber(int number, boolean asLeecher){
            
            if(asLeecher) theirOptimisticUnchokeNumberAsLeecher = number;
            else theirOptimisticUnchokeNumberAsSeeder = number;
            
            
        }
        
}
