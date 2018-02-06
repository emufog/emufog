package emufog.util;

import java.util.BitSet;

/**
 * The UniqueIDProvider keeps track of all IDs in use and calculates based
 * on that data the next available ID such that all IDs are unique.
 */
public class UniqueIDProvider {

    /* the current ID is the last one in use */
    private int current;

    /* set to keep track of all used IDs */
    private final BitSet bitSet;

    private static UniqueIDProvider INSTANCE;

    /**
     * Creates a new ID provider.
     */
    private UniqueIDProvider() {
        current = 0;
        bitSet = new BitSet();
    }

    /**
     * Returns the unique IDProvider instance. If not yet instantiated the method creates a new instance of IDProvider.
     * @return instance of UniqueIDProvider
     */
    public static UniqueIDProvider getInstance(){
        if(INSTANCE == null){
            INSTANCE = new UniqueIDProvider();
        }
        return INSTANCE;
    }

    /**
     * Calculates and returns the next available unique ID.
     * The ID is not marked as used.
     *
     * @return the new ID
     */
    public int getNextID() {
        current = bitSet.nextClearBit(current);

        return current;
    }

    /**
     * Marks an ID as used so it cannot be assigned to another object.
     *
     * @param id the ID already in use
     */
    public void markIDused(int id) {
        bitSet.set(id, true);
    }

    /**
     * Checks if the given ID is already in use.
     *
     * @param id ID to check
     * @return true if the ID already used, false otherwise
     */
    public boolean isUsed(int id) {
        return bitSet.get(id);
    }
}
