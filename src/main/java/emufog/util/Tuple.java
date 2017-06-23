package emufog.util;

/**
 * This class represents a generic tuple consisting of two values.
 * The types of values can be specified by K and V.
 *
 * @param <K> Class of the first value
 * @param <V> Class of the second value
 */
public class Tuple<K, V> {

    /* the key or the first value of the tuple */
    private K key;

    /* the second value of the tuple */
    private V value;

    /**
     * Creates a new tuple with the given values.
     * Both can be set to null at this point.
     *
     * @param key   first value
     * @param value second value
     */
    public Tuple(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the first value of the tuple.
     *
     * @return first value of the tuple
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the second value of the tuple.
     *
     * @return second value of the tuple
     */
    public V getValue() {
        return value;
    }

    /**
     * Updates the key with the new given key value.
     *
     * @param key the new key to set
     */
    public void setKey(K key) {
        this.key = key;
    }

    /**
     * Updates the value of the tuple.
     *
     * @param value the new value to set
     */
    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;

        if (o instanceof Tuple) {
            result = key.equals(((Tuple) o).key);
        }

        return result;
    }
}
