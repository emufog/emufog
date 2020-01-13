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

import java.util.TreeSet

class BinaryMinHeap<T> : Heap<T> {

    private val heap: TreeSet<T>

    constructor() {
        heap = TreeSet()
    }

    constructor(comparator: Comparator<T>) {
        heap = TreeSet(comparator)
    }

    override fun updateElement(element: T) {
        remove(element)
        add(element)
    }

    override fun add(element: T): Boolean = heap.add(element)

    override fun peek(): T = heap.first()

    override fun pop(): T? = heap.pollFirst()

    override fun remove(element: T): Boolean = heap.remove(element)

    override fun isEmpty(): Boolean = heap.isEmpty()
}