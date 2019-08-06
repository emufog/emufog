/*
 * MIT License
 *
 * Copyright (c) 2019 emufog contributors
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
package emufog.fog2;

import emufog.container.FogType;
import emufog.graph.Node;
import emufog.util.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FogResult {

    private boolean status;

    private final List<Tuple<Node, FogType>> placements;

    FogResult() {
        status = false;
        placements = new ArrayList<>();
    }

    public boolean getStatus() {
        return status;
    }

    public List<Tuple<Node, FogType>> getPlacements() {
        return placements;
    }

    void addPlacement(Tuple<Node, FogType> placement) {
        placements.add(placement);
    }

    void addPlacements(Collection<Tuple<Node, FogType>> placements) {
        this.placements.addAll(placements);
    }

    void setSuccess() {
        status = true;
    }

    void setFailure() {
        status = false;
    }
}
