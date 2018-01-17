package emufog.launcher;

import com.beust.jcommander.JCommander;
import emufog.settings.Settings;
import emufog.topology.Topology;
import emufog.util.Logger;

import java.io.FileNotFoundException;

/**
 * The EmuFog main launcher class. Starts a new instance of the application with the given parameters
 * by the command line interface.
 */
public class Emufog {

    //TODO: Take some time to implement clean and helpful logging.

    /**
     * Main function call to start EmuFog.
     *
     * @param args arguments of the command line
     */
    public static void main(String[] args) {

        Logger logger = Logger.getInstance();
        logger.logSeparator();
        logger.log("Welcome to EmuFog - MultiTierApplication");
        logger.logSeparator();

        Arguments arguments = new Arguments();

        try {
            //parse the command line arguments
            JCommander.newBuilder().addObject(arguments).build().parse(args);

            //initialize settings
            Settings.read(arguments.settingsPath);

            // build topology
            Topology topology = new Topology.TopologyBuilder().build();

            topology.export();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
