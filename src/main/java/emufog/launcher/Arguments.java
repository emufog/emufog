package emufog.launcher;

import com.beust.jcommander.Parameter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Arguments to read in from the command line.
 */
class Arguments {

    @Parameter(names = {"-Settings", "-s"}, description = "settings file to use")
    Path settingsPath;

    @Parameter(names = {"-Type", "-t"}, description = "input format to read in")
    String inputType;

    @Parameter(names = {"-Output", "-o"}, description = "path to the output file")
    String output;

    @Parameter(names = {"-File", "-f"}, description = "files to read in")
    List<Path> files = new ArrayList<>();
}
