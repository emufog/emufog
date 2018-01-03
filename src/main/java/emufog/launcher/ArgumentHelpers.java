package emufog.launcher;

import emufog.placement.FogLayout;
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
     * @param settings
     * @return
     */
    protected FogLayout getFogLayout(String cliInput, Settings settings){
        FogLayout fogLayout = null;
        //TODO: Implement getFogLayout logic.
        return fogLayout;

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
