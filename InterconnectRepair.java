package minitask_2;

import de.tu_darmstadt.rs.MoveModulesEverywhere;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.design.Pin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Julian Käuser on 14.09.17.
 */
public class InterconnectRepair {
    private static final Logger logger = LoggerFactory.getLogger(MoveModulesEverywhere.class);
    /*
    This class holds the minitask 2 invocation methods etc. Created mainly for testing reasons
     */
    public InterconnectRepair(){

    }

    public Design fixDesign(Design brokenDesign){
        logger.info("Testz");
        brokenDesign.getNetMap();
        return brokenDesign;
    }

    private void PinToPin (Pin aPin){
      //  PIP aPip;
      //  aPin.getConnectedForward();

     //   aPip.getEndWireName();
    }

    //TODO: rückgabetyp zu was sinnvollen ändern wie PIP oder so
    private Boolean checkIfBroken(Net net){
        return true;
    }

}
