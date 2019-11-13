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
package emufog.reader

import emufog.graph.Graph
import java.io.IOException
import java.nio.file.Path

interface GraphReader {

    /**
     * Reads in the given list of files, parses the content and constructs a [Graph] object based
     * on the file format. Returns the newly created instance.
     *
     * @param files list of files to read in
     * @return the read in graph structure
     * @throws IOException              throws exception in case there is a problem with reading in
     * the respective file
     * @throws IllegalArgumentException throws exception if given file list is invalid
     * @throws TopologyFormatException  throws exception if the format does not match
     */
    @Throws(IOException::class, IllegalArgumentException::class, TopologyFormatException::class)
    fun readGraph(files: List<Path>): Graph
}
