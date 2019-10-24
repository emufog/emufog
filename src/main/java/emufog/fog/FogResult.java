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
package emufog.fog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class represents the outcome of a fog node placement algorithm.
 * The {@link #status} represents the success or failure of the placement
 * and {@link #placements} the individual fog node placements.
 */
public class FogResult {

    /**
     * the outcome of the result set
     */
    private boolean status;

    /**
     * list of all fog node placements in the result set
     */
    private final List<FogNodePlacement> placements;

    /**
     * Creates a new empty result object. Initially the outcome is {@code false} and
     * there are no placements.
     */
    FogResult() {
        status = false;
        placements = new ArrayList<>();
    }

    /**
     * Returns the outcome of the fog node placement.
     * If {@code true} the {@link #getPlacements()} is returning the
     * correct result. For {@code false} {@link #getPlacements()} can
     * be any intermediate result.
     *
     * @return outcome status of the fog node placement
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * Returns the list of fog node placements associated with this fog node placement
     * result object.
     *
     * @return list off all fog node placements identified
     */
    public List<FogNodePlacement> getPlacements() {
        return placements;
    }

    /**
     * Adds a new fog node placement to the list of the result object.
     *
     * @param placement fog node placement to add
     */
    void addPlacement(FogNodePlacement placement) {
        placements.add(placement);
    }

    /**
     * Adds a collection of fog node placements to the list of the result object.
     *
     * @param placements collection of fog node placements to add
     */
    void addPlacements(Collection<FogNodePlacement> placements) {
        this.placements.addAll(placements);
    }

    /**
     * Sets the {@link #status} of the result object to {@code true}.
     */
    void setSuccess() {
        status = true;
    }

    /**
     * Sets the {@link #status} of the result object to {@code false}.
     */
    void setFailure() {
        status = false;
    }
}
