/*
 * BTAlgorithmUploadPeerNumSelection.java
 *
 * Created on 17 de enero de 2008, 14:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uc3m.netcom.peerfactsim.overlay.bt.algorithm;

/**
 * This class contains our novel number of unchoking peers
 * algorithm.
 *
 * @author JMCamacho
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.random.RandomGenerator;
import org.apache.log4j.Logger;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.overlay.bt.BTConstants;
import de.tud.kom.p2psim.overlay.bt.BTContact;
import de.tud.kom.p2psim.overlay.bt.BTDocument;
import de.tud.kom.p2psim.overlay.bt.BTInternStatistic;
import de.tud.kom.p2psim.overlay.bt.manager.BTConnectionManager;
import de.tud.kom.p2psim.overlay.bt.algorithm.BTAlgorithmChoking;

public class BTAlgorithmUploadPeerNumSelection {
    
	private BTInternStatistic itsStatistic;
	private RandomGenerator itsRandomGenerator;
	private boolean itsIsSetup = false;
                
        /*****************
         *Hay que modificar estas constantes en BTConstants para ajustarlas al
         *nuevo dise�o.
         */
        
        private static final long itsPeriod = BTConstants.CHOKING_OPTIMISTIC_CHOKING_RECALC_PERIOD;
        private static final int INC_STATE = 0;
        private static final int EQ_STATE = 1;
        private static final int DEC_STATE = 2;
        
        private double itsMaxUR;
        private double itsMaxDR;
        private long lastExecutionTime = -1;
        private double lastDownloadRate = -1.0;
        private double lastUploadRate = -1.0;
        private int currentState = EQ_STATE;
        private int currentRU = BTConstants.CHOKING_NUMBER_OF_REGULAR_UNCHOKES;
        private int currentOU = BTConstants.CHOKING_NUMBER_OF_OPTIMISTIC_UNCHOKES;
        private int blockSize = (int) java.lang.Math.pow(2,BTConstants.DOCUMENT_DEFAULT_BLOCK_EXPONENT);
        static final Logger log = SimLogger.getLogger(BTAlgorithmChoking.class);
               
        private static final byte[][] stateMachine = new byte[][]{
                {1,1,1,0,1,2,2,2,0,2,2,0,1,1,1,2,2,0},
                {1,1,1,0,1,2,2,2,0,2,2,0,1,1,1,2,2,0},
                {1,1,1,1,0,2,2,2,0,2,2,0,1,1,1,0,1,2},
                {1,1,1,1,0,2,2,2,0,2,2,0,1,1,1,1,1,1},
                {1,1,1,1,0,2,2,0,0,2,0,0,1,1,1,1,1,1},
                {1,1,1,1,0,2,2,0,0,2,0,0,1,1,1,1,1,1}
        };
     
    
	/**
	 * Use this method to use the algorithm.
         * It will set up the amount of unchoked peers that will be unchoked when the
         * choking operation executes the next time.
	 */
    
   /*   Creo que incrementar el numero de conexiones siendo seed no tiene mucho sentido. A menos que se quiera
      probar el rendimiento global si los seeder sirve a m�s peers, aunque esto puede depender de otros
      factores tales como el ancho de banda que se desee proporcionar a cada peer.*/

    
	public int[] establishUnchokedNum(Collection<BTContact> theContacts) {
		if (! this.isSetup()) {
			log.error("You have to setup this algorithm first!");
			throw new RuntimeException("You have to setup this algorithm first!");
		}
		if (theContacts.isEmpty())
			return new int[]{currentRU,currentOU};
                             
                double currentDR = downloadRate(theContacts);
                double currentUR = uploadRate(theContacts);
                int newState = stateMachine[calcRow(currentDR)][calcColumn(currentUR)];            
                updateAlgorithm(currentDR, currentUR, newState);
                return new int[]{currentRU,currentOU};
	}
	

       
	public void setup(BTInternStatistic theStatistic, RandomGenerator theRandomGenerator,double maxUR, double maxDR) {
		this.itsRandomGenerator = theRandomGenerator;
		this.itsStatistic = theStatistic;
		this.itsIsSetup = true;
                this.itsMaxUR = maxUR;
                this.itsMaxDR = maxDR;
	}

        
        private void updateAlgorithm(double currentDR, double currentUR,int newState){
                
               /* System.out.println("El nuevo Estado es = "+ newState);
                System.out.println("DRold = "+this.lastDownloadRate);
                System.out.println("DRnew = "+currentDR);
                System.out.println("URold = "+this.lastUploadRate);
                System.out.println("URnew = "+currentUR);*/
            
                this.lastDownloadRate = currentDR;
                this.lastUploadRate = currentUR;                
                this.lastExecutionTime = Simulator.getCurrentTime();
                this.currentState = newState;
          
                switch(newState){
                    case BTAlgorithmUploadPeerNumSelection.INC_STATE:
                        this.currentRU++;
                        this.currentOU++;
                        break;
                    case BTAlgorithmUploadPeerNumSelection.DEC_STATE:
                        if(currentRU > 3){
                            this.currentRU--;
                            this.currentOU--;
                        }
                        break;
                    case BTAlgorithmUploadPeerNumSelection.EQ_STATE:
                        
                }
        }
        
        /** This method returns total download rate obtained during the last 30 seg in
         *bytes per second.
         *
         *@return double Download Rate in packets per second.
         */
        
        private double downloadRate(Collection<BTContact> contacts){
            
            int packets = 0;
            
            for(BTContact oneContact : contacts){
            
                //LinkedList<Long> times = (LinkedList) itsStatistic.getDownloadStatisticForPeer(oneContact);                
                List<Long> times1 = itsStatistic.getDownloadStatisticForPeer (oneContact);
                LinkedList<Long> times = new LinkedList<Long>(times1);
                while(times.size() > 0 && times.getLast() >= lastExecutionTime){
                  packets++;
                  times.removeLast();                                      
                }
            }            
            //System.out.println("Packets = "+ packets);
            //System.out.println("Block Size = "+ blockSize);
            //System.out.println("Current Time = "+ Simulator.getCurrentTime());
            //System.out.println("LastExecutionTime = "+ this.lastExecutionTime);
            double a = packets*blockSize;
            double b = Simulator.getCurrentTime()-this.lastExecutionTime;
            double c = a/b;
            //System.out.println("a = "+a);
            //System.out.println("b = "+b);
            //System.out.println("c = "+c);
            return c;
            //return (packets*blockSize)/(Simulator.getCurrentTime()-this.lastExecutionTime);
        }

        
        private double uploadRate(Collection<BTContact> contacts){
            
            int packets = 0;
            
            for(BTContact oneContact : contacts){         
                packets += uploadedSince(lastExecutionTime,oneContact);
            }
            double a = packets*blockSize;
            double b = Simulator.getCurrentTime()-lastExecutionTime;
            double c = a/b;
            return c;
            //return (packets*blockSize)/(Simulator.getCurrentTime()-lastExecutionTime);
        }
        
         
        
 
        
        private int calcRow(double currentUR){
            
            if(currentUR > this.lastUploadRate){
                
                if(currentUR == this.itsMaxUR) return 0;
                else return 1;
                    
            }else if(currentUR < this.lastUploadRate){
                
                if(this.lastUploadRate == this.itsMaxUR) return 2;
                else return 3;
                
            }else{
                
                if(currentUR == this.itsMaxUR) return 4;
                else return 5;
            }
        }
        
        
        private int calcColumn(double currentDR){
            
            if(currentDR > this.lastDownloadRate){
                
                if(currentDR == this.itsMaxDR) return 0+this.currentState;
                else return 3+this.currentState;
                    
            }else if(currentDR < this.lastDownloadRate){
                
                if(this.lastDownloadRate == this.itsMaxDR) return 6+currentState;
                else return 9+currentState;
                    
            }else{                
                
                if(currentDR == this.itsMaxDR) return 12+currentState;
                else return 15+currentState;
            }
        }
        
              
	public boolean isSetup() {
		return this.itsIsSetup;
	}
	

	private int uploadedSince(long theTime, BTContact theContact) {
		int result = 0;
		List<Long> uploadPiecesTimes = this.itsStatistic.getUploadStatisticForPeer(theContact);
		int currentPosition = uploadPiecesTimes.size() - 1;
		while ((currentPosition >= 0) && (uploadPiecesTimes.get(currentPosition) >= theTime)) {
			result += 1;
			currentPosition -= 1;
		}
		return result;
	}
	
	
}