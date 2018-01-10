package emufog.settings;

import com.google.gson.*;
import emufog.nodeconfig.DeviceNodeType;

import java.lang.reflect.Type;

public class DeviceNodeDeserializer implements JsonDeserializer<DeviceNodeType> {
    @Override
    public DeviceNodeType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final DeviceNodeType deviceNode;

        final JsonObject jsonObject = json.getAsJsonObject();

        final int memoryLimit = jsonObject.get("MemoryLimit").getAsInt();
        final int cpuShare = jsonObject.get("CPUShare").getAsInt();
        final int scalingFactor = jsonObject.get("ScalingFactor").getAsInt();
        final float averageDeviceCount = jsonObject.get("AverageDeviceCount").getAsFloat();

        deviceNode = new DeviceNodeType(memoryLimit,cpuShare,scalingFactor,averageDeviceCount);

        return deviceNode;
    }
}
