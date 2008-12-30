/*
 * BTAlgorithmUploadPeerNumSelection.java
 *
 * Created on 17 de enero de 2008, 14:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package btj3m;

/**
 * This class contains our novel number of unchoking peers
 * algorithm.
 *
 * @author JMCamacho
 */

//import jBittorrentAPI.*;
//import java.util.Iterator;
//import java.util.List;



public class BTUnchokeNum {
    
//	private BTInternStatistic itsStatistic;
//	private RandomGenerator itsRandomGenerator;
	private boolean itsIsSetup = false;
                
        private static final int INC_STATE = 0;
        private static final int EQ_STATE = 1;
        private static final int DEC_STATE = 2;
        
        private double itsMaxUR;
        private double itsMaxDR;
        private long lastExecutionTime = -1;
        private double lastDownloadRate = -1.0;
        private double lastUploadRate = -1.0;
        private int currentState = EQ_STATE;
        private int currentRU;
        private int currentOU;
        private double uthreshold = itsMaxUR*0.1;
        private double dthreshold = itsMaxDR*0.1;

        private static java.util.HashMap<String,Integer> hits = new java.util.HashMap<String,Integer>();
        private static java.util.HashMap<String,Integer> transitions = new java.util.HashMap<String,Integer>();
        public java.util.HashMap<String,java.util.Vector> pulse = new java.util.HashMap<String,java.util.Vector>();
        private int[] lastTransition = new int[]{-1,-1};
        private boolean steadystate = false;
        //Original Matrix
  /*      private static final byte[][] stateMachine = new byte[][]{
                {1,1,1,0,1,2,2,2,0,2,2,0,1,1,1,2,2,0},
                {1,1,1,0,1,2,2,2,0,2,2,0,1,1,1,2,2,0},
                {1,1,1,0,1,2,2,2,0,2,2,0,1,1,1,0,1,2},
                {1,1,1,0,1,2,2,2,0,2,2,0,1,1,1,1,1,1},
                {1,1,1,0,1,2,2,0,0,2,0,0,1,1,1,1,1,1},
                {1,1,1,0,1,2,2,0,0,2,0,0,1,1,1,1,1,1}
        };
   */
        //Type 2  Matrix
    /*      private static final byte[][] stateMachine = new byte[][]{
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,1,1,0,1,1,1,1,1,2,1,1,1,1,1,0,0,0},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,1,1,1,1,0,1,1,1,2,1,1,1,1,1,0,0,0},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,1,1,0,1,0,1,1,1,2,1,1,1,1,1,0,0,0}
        };
    */
     
        //Type 4 Matrix
          private static final byte[][] stateMachine = new byte[][]{
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,1,1,0,0,0,1,1,1,2,2,2,1,1,1,0,0,0},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,1,1,0,0,0,1,1,1,2,2,2,1,1,1,0,0,0},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0}
        };
          
	/**
	 * Use this method to use the algorithm.
         * It will set up the amount of unchoked peers that will be unchoked when the
         * choking operation executes the next time.
	 */

    
	public int[] establishUnchokedNum(double downldR, double upldR) {
		if (! this.isSetup()) {
			throw new RuntimeException("You have to setup this algorithm first!");
		}
		if (downldR < 0 || upldR < 0)
			//return new int[]{currentRU,currentOU};
                        return new int[]{4,1};
                
        
                double currentDR = downldR;
                double currentUR = upldR;

                float aaux = (System.currentTimeMillis()-this.lastExecutionTime)/1000;
                this.lastExecutionTime = System.currentTimeMillis();
                
                if(currentDR > 0.3*this.itsMaxDR) steadystate = true;
                
                if(steadystate){
                    int r = calcRow(currentDR);
                    int c = calcColumn(currentUR);
                    int newState = stateMachine[r][c];            
                    updateAlgorithm(currentDR, currentUR, newState,r,c);
                    System.out.println("->->-> "+currentRU+" "+currentDR+" "+currentUR+" "+itsMaxDR+" "+aaux);//-this.lastExecutionTime)/Simulator.SECOND_UNIT));
                    return new int[]{currentRU,1};
                }
                
                //currentRU--;          
                System.out.println("-+-+-+ "+currentRU+" "+currentDR+" "+currentUR+" "+itsMaxDR+" "+aaux);
                return new int[]{currentRU,1};
	}

       
	public void setup(double maxUR, double maxDR) {

		this.itsIsSetup = true;
                this.itsMaxUR = maxUR;
                this.itsMaxDR = maxDR;
                this.pulse.put(this.toString(),new java.util.Vector());
                this.currentRU = 4;
	}

        
        private void updateAlgorithm(double currentDR, double currentUR,int newState,int row,int column){
                
            
                this.lastExecutionTime = System.currentTimeMillis();
                this.currentState = newState;
                
                if(currentDR > ((1.0+(Math.pow(currentRU,2)/10))*lastDownloadRate)){
                    
                    String lt = lastTransition[0]+"_"+lastTransition[1];
                    Integer hpp = BTUnchokeNum.hits.get(lt);
                    if(hpp == null){
                        BTUnchokeNum.hits.put(lt, new Integer(1));
                    }else{
                        int aux = hpp.intValue();
                        aux++;
                        hpp = new Integer(aux);
                        BTUnchokeNum.hits.put(lt, hpp);
                    }
                }   
                
                   lastTransition = new int[]{row,column};
                   
                   Integer tr = BTUnchokeNum.transitions.get(row+"_"+column);
                   
                   if(tr == null){
                       BTUnchokeNum.transitions.put(row+"_"+column,new Integer(1));               
                   }else{ 
                    int aux = tr.intValue();
                    aux++;
                    BTUnchokeNum.transitions.put(row+"_"+column,aux);
                   }
     
                switch(newState){
                    case BTUnchokeNum.INC_STATE:
                        if(currentRU <16){
                            if(this.currentRU <= 5) this.currentRU+=2;
                            else this.currentRU++;
                            this.currentOU++;
                            this.lastDownloadRate = currentDR;
                            this.lastUploadRate = currentUR;                     
                        }
                        break;
                        
                    case BTUnchokeNum.DEC_STATE:
                        if(currentRU > 3){
                            if(this.currentRU >= 8) this.currentRU-=3;
                            else this.currentRU--;
                            this.currentOU--;
                            this.lastDownloadRate = currentDR;
                            this.lastUploadRate = currentUR;
                        }
                        break;
                    case BTUnchokeNum.EQ_STATE:
                        lastDownloadRate = currentDR;
                        lastUploadRate = currentUR;
                }
        }
         
        
        private int calcRow(double currentUR){
            
            if(currentUR >= ((1.0+(Math.pow(currentRU, 2)/10))*this.lastUploadRate)){
                
                if(currentUR == this.itsMaxUR) return 0;
                else return 1;
                    
            }else if(currentUR <= ((1.0-(Math.pow(currentRU,2)/10))*this.lastUploadRate)){
                
                if(this.lastUploadRate == this.itsMaxUR) return 2;
                else return 3;
                
            }else{
                
                if(currentUR == this.itsMaxUR) return 4;
                else return 5;
            }
        }
        
        
        private int calcColumn(double currentDR){
            
            if(currentDR >= (1.0+(Math.pow(currentRU,2)/10))*this.lastDownloadRate){
                
                if(currentDR == this.itsMaxDR) return 0+this.currentState;
                else return 3+this.currentState;
                    
            }else if(currentDR <= (1.0+(Math.pow(currentRU,2)/10))*this.lastDownloadRate){
                
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
	

//	private int uploadedSince(long theTime, BTContact theContact) {
//		int result = 0;
//		List<Long> uploadPiecesTimes = this.itsStatistic.getUploadStatisticForPeer(theContact);
//		int currentPosition = uploadPiecesTimes.size() - 1;
//		while ((currentPosition >= 0) && (uploadPiecesTimes.get(currentPosition) >= theTime)) {
//			result += 1;
//			currentPosition -= 1;
//		}
//		return result;
//	}
//	
//	public void printHits(){
//            
//            logger.process(this.getClass().toString(), new Object[]{BTAlgorithmUploadPeerNumSelection.hits,BTAlgorithmUploadPeerNumSelection.transitions});
//            
//        }
}
