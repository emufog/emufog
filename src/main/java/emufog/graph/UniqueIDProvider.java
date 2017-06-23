package emufog.graph;

import java.util.BitSet;

/**
 * The UniqueIDProvider keeps track of all IDs in use and calculates based
 * on that data the next available ID such that all IDs are unique.
 */
class UniqueIDProvider {

    /* the current ID is the last one in use */
    private int current;

    /* set to keep track of all used IDs */
    private final BitSet bitSet;

    /**
     * Creates a new ID provider.
     */
    UniqueIDProvider() {
        current = 0;
        bitSet = new BitSet();
    }

    /**
     * Calculates and returns the next available unique ID.
     * The ID is not marked as used.
     *
     * @return the new ID
     */
    int getNextID() {
        current = bitSet.nextClearBit(current);

        return current;
    }

    /**
     * Marks an ID as used so it cannot be assigned to another object.
     *
     * @param id the ID already in use
     */
    void markIDused(int id) {
        bitSet.set(id, true);
    }

    /**
     * Checks if the given ID is already in use.
     *
     * @param id ID to check
     * @return true if the ID already used, false otherwise
     */
    boolean isUsed(int id) {
        return bitSet.get(id);
    }
}
