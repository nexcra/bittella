package de.tud.kom.p2psim.overlay.bt;

import java.util.BitSet;

/**
 * This class contains some usefull method if you have to work with bitfields.
 * @author Jan Stolzenburg
 */
public class BTBitSetUtil {
	
	/**
	 * Checks whether all bits are set.
	 * @param theBitset the bitset to check
	 * @param initialSize the size of the bitset that matters (bitsets don't store the correct value)
	 * @return are all bits set?
	 */
	public static boolean isBitsetSet(BitSet theBitset, int initialSize) {
		return (theBitset.cardinality() == initialSize);
	}
	
	/**
	 * Checks whether no bit is set.
	 * @param theBitset the bitset to check
	 * @return is no bit set?
	 */
	public static boolean isBitsetEmpty(BitSet theBitset) {
		return (theBitset.cardinality() == 0);
	}
	
	/**
	 * Creates a new bitset with all bits set.
	 * @param size the wanted size of the bitset.
	 * @return the bitset with all bits set.
	 */
	public static BitSet getFullBitset(int size) {
		BitSet result = new BitSet(size);
		result.set(0, size);
		return result;
	}
	
	/**
	 * Creates a new bitset with no bit set.
	 * @param size the wanted size of the bitset.
	 * @return the bitset with no bit set.
	 */
	public static BitSet getEmptyBitset(int size) {
		BitSet result = new BitSet(size);
		return result;
	}
	
	/**
	 * Applies AND to both bitsets and stores the result in a new one.
	 * Both bitsets stay unchanged.
	 * @param theFirst the first bitset
	 * @param theSecond the second bitset
	 * @return the result of applying AND to both.
	 */
	public static BitSet and(BitSet theFirst, BitSet theSecond) {
		BitSet result = new BitSet(theFirst.size());
		result.or(theFirst);
		result.and(theSecond);
		return result;
	}
	
	/**
	 * Applies ORD to both bitsets and stores the result in a new one.
	 * Both bitsets stay unchanged.
	 * @param theFirst the first bitset
	 * @param theSecond the second bitset
	 * @return the result of applying OR to both.
	 */
	public static BitSet or(BitSet theFirst, BitSet theSecond) {
		BitSet result = new BitSet(theFirst.size());
		result.or(theFirst);
		result.or(theSecond);
		return result;
	}
	
	/**
	 * Applies AND_NOT to both bitsets and stores the result in a new one.
	 * Both bitsets stay unchanged.
	 * @param theFirst the first bitset
	 * @param theSecond the second bitset
	 * @return the result of applying AND_NOT to both.
	 */
	public static BitSet andNot(BitSet theFirst, BitSet theSecond) {
		BitSet result = new BitSet(theFirst.size());
		result.or(theFirst);
		result.andNot(theSecond);
		return result;
	}
	
	/**
	 * Applies XOR to both bitsets and stores the result in a new one.
	 * Both bitsets stay unchanged.
	 * @param theFirst the first bitset
	 * @param theSecond the second bitset
	 * @return the result of applying XOR to both.
	 */
	public static BitSet xor(BitSet theFirst, BitSet theSecond) {
		BitSet result = new BitSet(theFirst.size());
		result.or(theFirst);
		result.xor(theSecond);
		return result;
	}
	
}
