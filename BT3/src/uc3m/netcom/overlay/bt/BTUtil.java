package uc3m.netcom.overlay.bt;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math.random.RandomGenerator;

import uc3m.netcom.transport.TransProtocol;

/**
 * This class defines some small helper methods.
 * @author Jan Stolzenburg
 */
public class BTUtil {
	
	/**
	 * A constant for a UDP like transport protocol.
	 */
	public static TransProtocol UDP = TransProtocol.UDP;
	
	/**
	 * A constant for a TCP like transport protocol.
	 */
	public static TransProtocol TCP = TransProtocol.UDP;
	
	/**
	 * If you have a list of entities and want a random subset of it,
	 * use this method. It calculates the indices of the entities that should be taken.
	 * @param theWantedAmount The number of elements you want.
	 * @param theSourceAmount The number of elements you have.
	 * @param theRandomGenerator The random source.
	 * @return The indices of the selected 
	 */
	public static Set<Integer> getRandomSubSetIndexes(int theWantedAmount, int theSourceAmount, RandomGenerator theRandomGenerator) {
		HashSet<Integer> result = new HashSet<Integer>(theWantedAmount, 1);
		if (theWantedAmount >= theSourceAmount) { //If there are to few objects, we take all.
			for (int i = 0; i < theSourceAmount; i++)
				result.add(i);
			return result;
		}
		if (theWantedAmount * 2 >= theSourceAmount) { //If there are just some objects to much, we throw out objects, until we have the wanted size. (That's faster than the standard-way)
			HashSet<Integer> tmp = new HashSet<Integer>(theSourceAmount, 1);
			for (int i = 0; i < theSourceAmount; i++)
				tmp.add(i);
			while (tmp.size() > theWantedAmount) { //We cannot use "for", because there could be duplicated numbers.(It's random!)
				tmp.remove(theRandomGenerator.nextInt(theSourceAmount));
			}
			result.addAll(tmp);
			return result;
		}
		while (result.size() < theWantedAmount) { //We cannot use "for", because there could be duplicated numbers.(It's random!)
			result.add(theRandomGenerator.nextInt(theSourceAmount));
		}
		return result;
	}
	
}
