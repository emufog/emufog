package emufog.settings;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import emufog.application.Application;

import java.lang.reflect.Type;

public class ApplicationDeserializer implements JsonDeserializer<Application>{
    @Override
    public Application deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}