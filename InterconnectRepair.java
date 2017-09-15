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
        for (Net aNet : brokenDesign.getNets()){
            this.checkIfBroken(aNet);
        }
        return brokenDesign;
    }

    private Boolean checkIfSourceReachable(Pin sinkPin, Pin sourcePin){
        //sinkPin.
        return false;
    }

    private Boolean checkIfBroken(Net net){
        Pin sourcePin = net.getSource();
        for (Pin aPin : net.getPins()) {
            if (!aPin.isOutPin()) { //wenn keine Quelle, d.h. es gibt einen outpin und N inpins. Achtung, das ist verwierend notiert!
                if (!aPin.isDeadEnd()) { //und kein bewusster dead-end, was eigentlich NICHT vorkommen darf das ein Ausgang ein deadend ist aber es könnte sonderfälle geben
                    if( this.checkIfSourceReachable(aPin, sourcePin) == false)
                        return true; //kapput
                }
            }
        }
        return false; // nicht kapput
    }
}
