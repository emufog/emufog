package emufog.launcher;

import com.beust.jcommander.Parameter;

import java.nio.file.Path;

/**
 * Arguments to read in from the command line.
 */
class Arguments {

    @Parameter(names = {"--settings", "-s"}, description = "settings file to use")
    Path settingsPath;

}
