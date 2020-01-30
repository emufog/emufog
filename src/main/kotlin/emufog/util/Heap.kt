/*
 * MIT License
 *
 * Copyright (c) 2020 emufog contributors
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
package emufog.util

/**
 * Interface of a heap that offers the minimum set of operations required.
 */
interface Heap<T> {

    /**
     * Adds an element to the heap. Returns the outcome of the operation.
     *
     * @param element element to add
     * @return `true` if the element was added, `false` if not
     */
    fun add(element: T): Boolean

    /**
     * Returns the top element of the heap but does not remove it from the heap. If the heap is empty the function
     * returns `null`.
     *
     * @return the current top element in the heap
     */
    fun peek(): T?

    /**
     * Returns and removes the top element of the heap. If the heap is empty the function returns `null`.
     *
     * @return the removed top element of the heap
     */
    fun pop(): T?

    /**
     * Removes an element from the heap. Returns the outcome of the operation.
     *
     * @param element element to remove
     * @return `true` if the element was removed, `false` if not found
     */
    fun remove(element: T): Boolean

    /**
     * Returns whether the heap is empty or not.
     *
     * @return `true` if the heap is empty, `false` if not
     */
    fun isEmpty(): Boolean
}