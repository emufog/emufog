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
package emufog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

/**
 * This reader reads in YAML documents to build config object for EmuFog.
 */
public class YamlReader {

    /**
     * Reads in the given YAML file and parses the content.
     * Creates and returns a new config object with it.
     *
     * @param path path to YAML file
     * @return config object or null if impossible to read
     * @throws IllegalArgumentException if the given path is null
     * @throws IOException              in case of an error while parsing
     */
    public static Config read(Path path) throws IllegalArgumentException, IOException {
        if (path == null) {
            throw new IllegalArgumentException("The given file path is not initialized.");
        }
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.yaml");
        if (!matcher.matches(path)) {
            throw new IllegalArgumentException("The file ending does not match .yaml.");
        }
        // parse YAML document to a java object
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        EmuFogConfig loadedConfig = mapper.readValue(path.toFile(), EmuFogConfig.class);
        if (loadedConfig == null) {
            throw new IllegalArgumentException("Failed to parse the YAML file: " + path);
        }

        // create the actual config object with the information of the read in objects
        return new Config(loadedConfig);
    }
}
