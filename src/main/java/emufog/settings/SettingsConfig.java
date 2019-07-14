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
package emufog.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;

/**
 * Top level settings object of the YAML document.
 */
class SettingsConfig {

    @JsonProperty("base-address")
    String baseAddress;

    @JsonProperty("overwrite-experiment-file")
    boolean overWriteOutputFile;

    @JsonProperty("max-fog-nodes")
    int maxFogNodes;

    @JsonProperty("cost-threshold")
    float costThreshold;

    @JsonProperty("host-device-latency")
    float hostDeviceLatency;

    @JsonProperty("host-device-bandwidth")
    float hostDeviceBandwidth;

    @JsonProperty("thread-count")
    int threadCount;

    @JsonProperty("paralleled-fog-building")
    boolean paralleledFogBuilding;

    @JsonProperty("time-measuring")
    boolean timeMeasuring;

    @JsonProperty("device-node-types")
    Collection<DeviceTypeConfig> deviceNodeTypes;

    @JsonProperty("fog-node-types")
    Collection<FogTypeConfig> fogNodeTypes;
}
