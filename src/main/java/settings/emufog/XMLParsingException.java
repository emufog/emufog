package settings.emufog;

import java.io.IOException;

/**
 * This exception should notify in case of occurring errors while reading XML files.
 */
public class XMLParsingException extends IOException {

    /**
     * Creates a new exception representing an error while reading an XML file.
     *
     * @param msg error message
     */
    public XMLParsingException(String msg) {
        super(msg);
    }
}
