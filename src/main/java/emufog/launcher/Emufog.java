package emufog.launcher;

import com.beust.jcommander.JCommander;
import emufog.settings.Settings;
import emufog.topology.Topology;
import emufog.util.Logger;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The EmuFog main launcher class. Starts a new instance of the application with the given parameters
 * by the command line interface.
 */
public class Emufog {

    /**
     * Main function call to start EmuFog.
     *
     * @param args arguments of the command line
     */
    public static void main(String[] args) {

        Logger logger = Logger.getInstance();
        logger.logSeparator();
        logger.log("\n" +
                "$$$$$$$$\\                         $$$$$$$$\\                  \n" +
                "$$  _____|                        $$  _____|                 \n" +
                "$$ |      $$$$$$\\$$$$\\  $$\\   $$\\ $$ |    $$$$$$\\   $$$$$$\\  \n" +
                "$$$$$\\    $$  _$$  _$$\\ $$ |  $$ |$$$$$\\ $$  __$$\\ $$  __$$\\ \n" +
                "$$  __|   $$ / $$ / $$ |$$ |  $$ |$$  __|$$ /  $$ |$$ /  $$ |\n" +
                "$$ |      $$ | $$ | $$ |$$ |  $$ |$$ |   $$ |  $$ |$$ |  $$ |\n" +
                "$$$$$$$$\\ $$ | $$ | $$ |\\$$$$$$  |$$ |   \\$$$$$$  |\\$$$$$$$ |\n" +
                "\\________|\\__| \\__| \\__| \\______/ \\__|    \\______/  \\____$$ |\n" +
                "                                                   $$\\   $$ |\n" +
                "                                                   \\$$$$$$  |\n" +
                "                                                    \\______/ \n" +
                " \n");
        logger.logSeparator();

        Arguments arguments = new Arguments();

        try {
            //parse the command line arguments
            JCommander.newBuilder().addObject(arguments).build().parse(args);

            //initialize settings
            Path path = Paths.get("/home/renderfehler/IdeaProjects/emufog/settings.yaml");
            Settings.read(path);


            // build topology
            long start = System.nanoTime();
            Topology topology = new Topology.TopologyBuilder().build();
            long end = System.nanoTime();
            logger.log("It took " + Logger.convertToMs(start, end) + " to build the complete topology.");

            start = System.nanoTime();
            topology.export();
            end = System.nanoTime();
            logger.log("It took " + Logger.convertToMs(start,end) + " to export the topology");
            logger.logSeparator();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
