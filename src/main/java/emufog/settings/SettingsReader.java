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
package emufog.settings;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;

/**
 * This reader reads in JSON documents to build settings object for EmuFog.
 */
public class SettingsReader {

    /**
     * Reads in the given JSON file and parses the content.
     * Creates and returns a new settings object with it.
     *
     * @param path path to JSON file
     * @return settings object or null if impossible to read
     * @throws IllegalArgumentException if the given path is null
     * @throws FileNotFoundException    if the given path can not be found
     */
    public static Settings read(Path path) throws IllegalArgumentException, FileNotFoundException {
        if (path == null) {
            throw new IllegalArgumentException("The given file path is not initialized.");
        }
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.json");
        if (!matcher.matches(path)) {
            throw new IllegalArgumentException("The file ending does not match .json.");
        }

        Settings settings = null;
        // parse JSON document to a java object
        JSONSettings json = new Gson().fromJson(new FileReader(path.toFile()), JSONSettings.class);

        if (json != null) {
            // create the actual settings object with the information of the read in objects
            settings = new Settings(json);
        }

        return settings;
    }

    /**
     * Top level settings object of the JSON document.
     */
    class JSONSettings {
        String BaseAddress;
        boolean OverWriteOutputFile;
        int MaxFogNodes;
        float CostThreshold;
        float HostDeviceLatency;
        float HostDeviceBandwidth;
        int ThreadCount;
        boolean ParalleledFogBuilding;
        boolean timeMeasuring;
        Collection<DeviceType> DeviceNodeTypes;
        Collection<FogType> FogNodeTypes;
    }

    /**
     * Abstract container type class for host devices and fog nodes.
     */
    abstract class DockerType {
        DockerName DockerImage;
        int MemoryLimit;
        float CPUShare;
    }

    /**
     * Docker type for host devices extending the abstract container type with scaling
     * factor and the device count.
     */
    class DeviceType extends DockerType {
        int ScalingFactor;
        int AverageDeviceCount;
    }

    /**
     * Name of a Docker container consisting of the name of the image and the version to use.
     */
    class DockerName {
        String Name;
        String Version;

        @Override
        public String toString() {
            return Name + ':' + Version;
        }
    }

    /**
     * Docker type for fog nodes with their respective dependencies and properties for
     * the placement algorithm.
     */
    class FogType extends DockerType {
        int ID;
        int MaximumConnections;
        float Costs;
        int[] Dependencies;
    }
}
