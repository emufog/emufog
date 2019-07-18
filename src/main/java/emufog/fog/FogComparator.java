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
 * Custom comparator to sort a list of FogNodes.
 * The comparator uses two properties. First the comparator sorts descending
 * according to the average deployment costs and in case they are equal
 * the comparator sorts descending to the average connection costs.
 */
class FogComparator implements Comparator<FogNode> {

    @Override
    public int compare(FogNode fogNode, FogNode t1) {
        float cost1 = fogNode.getAverageDeploymentCosts();
        float cost2 = t1.getAverageDeploymentCosts();

        if (cost1 < cost2) {
            return -1;
        }

        if (cost2 < cost1) {
            return 1;
        }

        cost1 = fogNode.getAverageConnectionCosts();
        cost2 = t1.getAverageConnectionCosts();

        return Float.compare(cost1, cost2);
    }
}
