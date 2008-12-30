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
public class AvPieces extends LinkedHashMap<String, BitSet> {

    private BitSet isComplete;
    private BitSet isRequested;
    private int nbPieces;

    public AvPieces(int nbPieces) {
        super();
        this.nbPieces = nbPieces;
        this.isComplete = new BitSet(this.nbPieces);
        this.isRequested = new BitSet(this.nbPieces);
    }

    public int[] rarest(ArrayList<Integer> possible) {

        int[] freq = new int[possible.size()];
        Iterator<BitSet> bitfields = this.values().iterator();

        while (bitfields.hasNext()) {

            BitSet set = bitfields.next();
            for (int i = 0; i < possible.size(); i++) {
                int pos = possible.get(i);
                try {
                    if (set.get(pos)) {
                        freq[i]++;
                    }
                } catch (Exception e) {
                    System.out.println("Rarest: " + e.getMessage());
                    return null;
                }
            }
        }

        int max = 0;

        for (int i = 0; i < freq.length; i++) {
            if (freq[max] < freq[i]) {
                max = i;
            }
        }


        return new int[]{possible.get(max), freq[max]};
    }

    public Integer random(ArrayList<Integer> possible) {

        int index = 0;

        if (possible.size() > 0) {
            Random r = new Random(System.currentTimeMillis());
            index = possible.get(r.nextInt(possible.size()));
            return (index);
        }
        return -1;
    }

    public Integer endGameMode(String id) {
        return null;
    } //???

    public ArrayList<Integer> calculatePossible(String id) {
        synchronized (this.isComplete) {
            ArrayList<Integer> possible = new ArrayList<Integer>(this.nbPieces);

            for (int i = 0; i < this.nbPieces; i++) {
                if ((!this.isPieceRequested(i) ||
                        (this.isComplete.cardinality() > this.nbPieces - 3)) &&
                        //(this.isRequested.cardinality() == this.nbPieces)) &&
                        (!this.isPieceComplete(i)) &&
                        this.get(id) != null) {

                    if (this.get(id).get(i)) {
                        possible.add(i);
                    }
                }
            }

            return possible;
        }
    }
    
    public BitSet getPeerBitfield(String id){
        return this.get(id);
    }
    
    public BitSet getCompletedSet(){
        return this.isComplete;
    }

    public BitSet getRequestedSet(){
        return this.isRequested;
    }
    
    public int getNumOfPieces(){
        return this.nbPieces;
    }
    
    /**
     * Compute the bitfield byte array from the isComplete BitSet
     * @return byte[]
     */
    public byte[] getBitField() {
        int l = (int) Math.ceil((double) this.nbPieces / 8.0);
        byte[] bitfield = new byte[l];
        for (int i = 0; i < this.nbPieces; i++) {
            if (this.isComplete.get(i)) {
                bitfield[i / 8] |= 1 << (7 - i % 8);
            }
        }
        return bitfield;
    }

    public float getCompleted() {
        try {
            return (float) (((float) (100.0)) * ((float) (this.isComplete.cardinality())) /
                    ((float) (this.nbPieces)));
        } catch (Exception e) {
            return 0.00f;
        }
    }

    /**
     * Returns a String representing the piece being requested by peers.
     * Used only for pretty-printing.
     * @return String
     */
    public synchronized String requestedBits() {
        String s = "";
        synchronized (this.isRequested) {
            for (int i = 0; i < this.nbPieces; i++) {
                s += this.isRequested.get(i) ? 1 : 0;
            }
        }
        return s;
    }

    /**
     * Mark a piece as requested or not according to the parameters
     * @param piece The index of the piece to be updated
     * @param is True if the piece is now requested, false otherwise
     */
    public synchronized void setRequested(int piece, boolean is) {
        synchronized (this.isRequested) {
            this.isRequested.set(piece, is);
        }
    }

    /**
     * Check if the piece with the given index is complete and verified
     * @param piece The piece index
     * @return boolean
     */
    public synchronized boolean isPieceComplete(int piece) {
        synchronized (this.isComplete) {
            return this.isComplete.get(piece);
        }
    }

    /**
     * Check if the piece with the given index is requested by a peer
     * @param piece The piece index
     * @return boolean
     */
    public synchronized boolean isPieceRequested(int piece) {
        synchronized (this.isRequested) {
            return this.isRequested.get(piece);
        }
    }

    /**
     * Mark a piece as complete or not according to the parameters
     * @param piece The index of the piece to be updated
     * @param is True if the piece is now complete, false otherwise
     */
    public synchronized void setComplete(int piece, boolean is) {
        synchronized (this.isComplete) {
            this.isComplete.set(piece, is);
        }
    }

    /**
     * Check if the current download is complete
     * @return boolean
     */
    public synchronized boolean isComplete() {
        synchronized (this.isComplete) {
            return (this.isComplete.cardinality() == this.nbPieces);
        }
    }

    /**
     * Returns the number of pieces currently requested to peers
     * @return int
     */
    public synchronized int cardinalityR() {
        return this.isRequested.cardinality();
    }
    
    public synchronized int cardinalityC(){
        return this.isComplete.cardinality();
    }
}

//    public String get(T torrent, P id) {
//
//        LinkedHashMap<P, S> l = this.get(torrent);
//        return l.get(id);
//    }
//
//    public void put(T torrent, P id, S set) {
//        if (this.containsKey(torrent)) {
//            this.get(torrent).put(id, set);
//        } else {
//            LinkedHashMap<P, S> l = new LinkedHashMap<P, S>();
//            l.put(id, set);
//            this.put(torrent, l);
//        }
//    }
//
//    public Collection<S> values(T torrent) {
//        return this.get(torrent).values();
//    }
//
//    public boolean containsKey(T torrent, P key) {
//
//        LinkedHashMap l = this.get(torrent);
//        return l.containsKey(key);
//    }
//
//    public boolean containsValue(T torrent, S value) {
//
//        LinkedHashMap<P, S> l = this.get(torrent);
//        return l.containsValue(value);
//    }
//
//    public Set<P> keySet(T torrent) {
//        return this.get(torrent).keySet();
//    }
//}
