package emufog.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import emufog.application.Application;
import emufog.nodes.DeviceNode;
import emufog.nodes.FogNode;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;

public class MultiTierSettings {

    public final List<FogNode> fogNodes;

    public final List<DeviceNode> deviceNodes;

    public final List<Application> applications;


    class BaseSettings {

        public final String baseAdress;

        public final boolean overwriteExperimentFile;

        public final int maxFogNodes;

        public final float costThreshold;

        public final float edgeDeviceDelay;

        public final float edgeDeviceBandwitdh;

        public final int threadCount;

        public final boolean fogGraphParallel;

    }


    MultiTierSettings(){

    }

    public MultiTierSettings read(Path settingsPath, Path applicationsPath) throws FileNotFoundException{

        MultiTierSettings settings = null;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BaseSettings.class, new BaseSettingsDeserializer());
        gsonBuilder.registerTypeAdapter(FogNode.class, new FogNodeDeserializer());
        gsonBuilder.registerTypeAdapter(DeviceNode.class, new DeviceNodeDeserializer());
        gsonBuilder.registerTypeAdapter(Application.class, new ApplicationDeserializer());
        Gson gson = gsonBuilder.create();

        final BaseSettings baseSettings = gson.fromJson()













        return settings;
    }



}
