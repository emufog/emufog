package emufog.settings;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import emufog.nodes.DeviceNode;

import java.lang.reflect.Type;

public class DeviceNodeDeserializer implements JsonDeserializer<DeviceNode>{
    @Override
    public DeviceNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}
