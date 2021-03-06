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

import java.util.PriorityQueue

/**
 * The priority heap is an implementation of the [Heap] interface using the [PriorityQueue] implementation of the JDK.
 */
class PriorityHeap<T> : Heap<T> {

    private val heap: PriorityQueue<T>

    constructor() {
        heap = PriorityQueue()
    }

    constructor(comparator: Comparator<T>) {
        heap = PriorityQueue(comparator)
    }

    override fun add(element: T): Boolean = heap.add(element)

    override fun peek(): T? = heap.peek()

    override fun pop(): T? = heap.poll()

    override fun remove(element: T): Boolean = heap.remove(element)

    override fun isEmpty(): Boolean = heap.isEmpty()
}