/*
 * MIT License
 *
 * Copyright (c) 2018 emufog contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package emufog.util;

/**
 * This class represents a generic tuple consisting of two values.
 * The types of values can be specified by K and V.
 *
 * @param <K> Class of the first value
 * @param <V> Class of the second value
 */
public class Tuple<K, V> {

    /**
     * the key or the first value of the tuple
     */
    private K key;

    /**
     * the second value of the tuple
     */
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
        if (o == this) {
            return true;
        }
        if (!(o instanceof Tuple)) {
            return false;
        }

        Tuple other = (Tuple) o;

        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "Tuple: <" + key.toString() + ", " + value.toString() + ">";
    }
}
