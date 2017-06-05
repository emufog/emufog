package export.emufog;

import settings.emufog.Settings;

import java.io.File;
import java.nio.file.Path;

/**
 * Abstract class of a graph exporter. To be extended to export in any file format.
 * Holds the output file and the settings to use.
 */
public abstract class GraphExporter implements IGraphExporter {

    /* file object to export the graph to */
    protected final File file;

    /* settings that applies for the exporter */
    protected final Settings settings;

    /**
     * Creates a new GraphExporter using the given settings.
     * The graph will be exported to the file path provided.
     *
     * @param path     path to the target file
     * @param settings settings that apply for this exporter
     * @throws IllegalArgumentException The path and settings object cannot be null. Also if the file already
     *                                  exists and the overwrite settings are disabled.
     */
    GraphExporter(Path path, Settings settings) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("The given path is null. Please provide a valid path");
        }
        if (settings == null) {
            throw new IllegalArgumentException("No settings given. Please provide valid settings.");
        }

        File f = path.toFile();
        if (!settings.overwriteExperimentFile && f.exists()) {
            throw new IllegalArgumentException("The given file already exist. Please provide a valid path");
        }
        validateFileName(path);

        file = f;
        this.settings = settings;
    }

    /**
     * Validates the file name of the given file according to the respective subclass.
     *
     * @param path path to check the file name
     * @throws IllegalArgumentException if the file name is invalid
     */
    protected abstract void validateFileName(Path path) throws IllegalArgumentException;
}
