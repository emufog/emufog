package emufog.launcher;

import com.beust.jcommander.Parameter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Arguments to read in from the command line.
 */
class Arguments {

    @Parameter(names = {"--settings", "-s"}, description = "settings file to use")
    Path settingsPath;

    @Parameter(names = {"--type", "-t"}, description = "input format to read in")
    String inputType;

    @Parameter(names = {"--output", "-o"}, description = "path to the output file")
    String output;

    @Parameter(names = {"--fogLayout", "-l"}, description = "selector for desired fog layout type")
    String fogLayout;

    @Parameter(names = {"--file", "-f"}, description = "files to read in")
    List<Path> files = new ArrayList<>();
}
