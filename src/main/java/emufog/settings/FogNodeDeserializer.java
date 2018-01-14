package emufog.settings;

import com.google.gson.*;
import emufog.nodeconfig.FogNodeType;

import java.lang.reflect.Type;

public class FogNodeDeserializer implements JsonDeserializer<FogNodeType>{
    @Override
    public FogNodeType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final FogNodeType fogNode;

        final JsonObject jsonObject = json.getAsJsonObject();

        final int memoryLimit = jsonObject.get("MemoryLimit").getAsInt();
        final int cpuShare = jsonObject.get("CPUShare").getAsInt();
        final int maximumConnections = jsonObject.get("MaximumConnections").getAsInt();
        final double costs = jsonObject.get("Costs").getAsDouble();

        fogNode = new FogNodeType(memoryLimit,cpuShare,maximumConnections,costs);

        return fogNode;
    }
}
