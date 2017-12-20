package emufog.settings;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class BaseSettingsDeserializer implements JsonDeserializer<MultiTierSettings.BaseSettings>{

    @Override
    public MultiTierSettings.BaseSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}
