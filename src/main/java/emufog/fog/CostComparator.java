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
package emufog.fog;

import java.util.Comparator;

/**
 * The cost comparator compares fog nodes based on their associated costs for a given edge node.
 */
class CostComparator implements Comparator<FogNode> {

    /* edge node to compare costs for */
    private final EdgeNode edge;

    /**
     * Creates a new comparator to compare fog nodes based on their cost from the given edge.
     *
     * @param edge edge node to sort for
     */
    CostComparator(EdgeNode edge) {
        this.edge = edge;
    }

    @Override
    public int compare(FogNode o1, FogNode o2) {
        float cost1 = o1.getCosts(edge);
        float cost2 = o2.getCosts(edge);

        if (cost1 < cost2) {
            return -1;
        }
        if (cost1 > cost2) {
            return 1;
        }

        return 0;
    }
}