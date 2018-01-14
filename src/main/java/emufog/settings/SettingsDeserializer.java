package emufog.settings;

import com.google.gson.*;
import emufog.application.Application;

import java.lang.reflect.Type;

public class SettingsDeserializer implements JsonDeserializer<Settings> {
    @Override
    public Settings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final JsonObject jsonObject = json.getAsJsonObject();

        final Settings settings = new Settings();

        settings.setBaseAddress(jsonObject.get("BaseAddress").getAsString());
        settings.setOverwriteExperimentFile(jsonObject.get("OverWriteExperimentFile").getAsBoolean());
        settings.setMaxFogNodes(jsonObject.get("MaxFogNodes").getAsInt());
        settings.setCostThreshold(jsonObject.get("CostThreshold").getAsFloat());
        settings.setEdgeDeviceDelay(jsonObject.get("EdgeDeviceDelay").getAsFloat());
        settings.setThreadCount(jsonObject.get("ThreadCount").getAsInt());
        settings.setParallelFogBuilding(jsonObject.get("ParalleledFogBuilding").getAsBoolean());

        //TODO: Repair Device and Fog node deserializers.

        DeviceNode[] deviceNodes = context.deserialize(jsonObject.get("DeviceNodes"), DeviceNode[].class);
        FogNode[] fogNodes = context.deserialize(jsonObject.get("FogNodes"), FogNode[].class);
        Application[] applications = context.deserialize(jsonObject.get("Applications"), Application[].class);

        settings.setDeviceNodeTypes(deviceNodes);
        settings.setFogNodeTypes(fogNodes);
        settings.setApplications(applications);

        return settings;
    }
}
