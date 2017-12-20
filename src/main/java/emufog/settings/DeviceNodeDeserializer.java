package emufog.settings;

import com.google.gson.*;
import emufog.nodes.DeviceNode;

import java.lang.reflect.Type;

public class DeviceNodeDeserializer implements JsonDeserializer<DeviceNode>{
    @Override
    public DeviceNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final JsonObject jsonObject = json.getAsJsonObject();

        final

        final DeviceNode deviceNode = new DeviceNode(

        )


        return deviceNode;
    }
}
