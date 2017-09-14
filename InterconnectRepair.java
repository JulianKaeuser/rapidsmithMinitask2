package minitask_2;

import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Net;

/**
 * Created by Julian Käuser on 14.09.17.
 */
public class InterconnectRepair {

    /*
    This class holds the minitask 2 invocation methods etc. Created mainly for testing reasons
     */
    public InterconnectRepair(){

    }

    public Design fixDesign(Design brokenDesign){
        return brokenDesign;
    }

    //TODO: rückgabetyp zu was sinnvollen ändern wie PIP oder so
    private Boolean checkIfBroken(Net net){
        return true;
    }

}
