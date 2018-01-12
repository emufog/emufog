package emufog.launcher;

import emufog.placement.EdgeIdentifier;
import emufog.placement.IFogLayout;
import emufog.reader.BriteFormatReader;
import emufog.reader.CaidaFormatReader;
import emufog.reader.GraphReader;
import emufog.settings.Settings;

/**
 * Small helper class providing serveral funktions mainly to retrieve information from input parameters.
 */

public class ArgumentHelpers {

    /**
     * Function to retrieve desired Fog Layout strategy from input parameter.
     * @param cliInput
     * @return
     */
    protected static IFogLayout getFogLayoutStrategy(String cliInput){
        //TODO: Implement getFogLayout logic.
        return null;
    }

    /**
     * Function to retrieve desired EdgeIdentifier strategy from input parameter.
     * @param cliInput
     * @return
     */

    protected static EdgeIdentifier getEdgeIdentifierStrategy(String cliInput){
        //TODO: Implement getEdgeIdentifier logic.
        //TODO: Add parameter to Arguments class.
        return null;
    }

    /**
     * Function to retrieve desired EdgeIdentifier from settings.
     * @param settings
     * @return
     */
    protected static EdgeIdentifier getEdgeIdentifierStrategy(Settings settings){
        //TODO: Implement settings alternative for getEdgeIdentifier.
        return null;
    }



    /**
     * Function to retrieve reader matching the input parameter.
     * @param cliInput
     * @param settings
     * @return
     */

    protected static GraphReader getReader(String cliInput, Settings settings){

        GraphReader reader = null;

        switch (cliInput.toLowerCase()){
            case "brite":
                reader = new BriteFormatReader(settings);
                break;
            case "caida":
                reader = new CaidaFormatReader(settings);
                break;

        }

        return reader;

    }

}
