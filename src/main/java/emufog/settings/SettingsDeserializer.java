package emufog.settings;

import com.google.gson.*;
import emufog.application.Application;
import emufog.nodes.DeviceNode;
import emufog.nodes.FogNode;

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
        settings.setFogGraphParallel(jsonObject.get("ParalleledFogBuilding").getAsBoolean());


        DeviceNode[] deviceNodes = context.deserialize(jsonObject.get("DeviceNodes"), DeviceNode.class);
        FogNode[] fogNodes = context.deserialize(jsonObject.get("FogNodes"), FogNode.class);
        Application[] applications = context.deserialize(jsonObject.get("Applications"), Application.class);

        settings.setDeviceNodes(deviceNodes);
        settings.setFogNodes(fogNodes);
        settings.setApplications(applications);

        return settings;
    }
}
