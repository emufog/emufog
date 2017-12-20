package emufog.settings;

import com.google.gson.*;
import emufog.nodes.DeviceNode;

import java.lang.reflect.Type;

public class DeviceNodeDeserializer implements JsonDeserializer<DeviceNode> {
    @Override
    public DeviceNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final DeviceNode deviceNode;

        final JsonObject jsonObject = json.getAsJsonObject();

        final int memoryLimit = jsonObject.get("MemoryLimit").getAsInt();
        final int cpuShare = jsonObject.get("CPUShare").getAsInt();
        final int scalingFactor = jsonObject.get("ScalingFactor").getAsInt();
        final float averageDeviceCount = jsonObject.get("AverageDeviceCount").getAsFloat();

        deviceNode = new DeviceNode(memoryLimit,cpuShare,scalingFactor,averageDeviceCount);

        return deviceNode;
    }
}
