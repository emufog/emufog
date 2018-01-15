package emufog.launcher;

import com.beust.jcommander.Parameter;

import java.nio.file.Path;

/**
 * Arguments to read in from the command line.
 */
class Arguments {

    @Parameter(names = {"--settings", "-s"}, description = "settings file to use")
    Path settingsPath;

   /* @Parameter(names = {"--type", "-t"}, description = "input format to read in")
    String inputType;

    @Parameter(names = {"--output", "-o"}, description = "path to the output file")
    String output;

    @Parameter(names = {"--file", "-f"}, description = "files to read in")
    List<Path> files = new ArrayList<>();*/
}
