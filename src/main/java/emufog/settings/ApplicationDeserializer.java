/*
package emufog.settings;

import com.google.gson.*;
import emufog.application.Application;

import java.lang.reflect.Type;

public class ApplicationDeserializer implements JsonDeserializer<Application>{
    @Override
    public Application deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final JsonObject jsonObject = json.getAsJsonObject();

        final Application application = new Application();

        application.resources(jsonObject.get("MemoryLimit").getAsInt(),jsonObject.get("CpuShare").getAsFloat());
        application.image(jsonObject.get("Image").getAsString(), jsonObject.get("ImageVersion").getAsString());
        application.setName(jsonObject.get("Name").getAsString());
        application.type(jsonObject.get("ApplicationType").getAsString());

        return application;
    }
}
*/
