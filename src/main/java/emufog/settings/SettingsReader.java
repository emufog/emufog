package emufog.settings;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
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
     */
    public static Settings read(Path path) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("The given file path is not initialized.");
        }

        Settings settings = null;
        try {
            Gson gson = new Gson();
            JSONSettings json = gson.fromJson(new FileReader(path.toFile()), JSONSettings.class);

            if (json != null) {
                settings = new Settings(json);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return settings;
    }

    class JSONSettings {
        String BaseAddress;
        boolean OverWriteOutputFile;
        int MaxFogNodes;
        float CostThreshold;
        float HostDeviceLatency;
        float HostDeviceBandwidth;
        int ThreadCount;
        boolean ParalleledFogBuilding;
        Collection<DeviceType> DeviceNodeTypes;
        Collection<FogType> FogNodeTypes;
    }

    abstract class DockerType {
        DockerName DockerImage;
        int MemoryLimit;
        float CPUShare;
    }

    class DeviceType extends DockerType {
        int ScalingFactor;
        int AverageDeviceCount;
    }

    class DockerName {
        String Name;
        String Version;

        @Override
        public String toString() {
            return Name + ':' + Version;
        }
    }

    class FogType extends DockerType {
        int MaximumConnections;
        float Costs;
    }
}
