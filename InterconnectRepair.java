package minitask_2;

import de.tu_darmstadt.rs.MoveModulesEverywhere;
import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.design.Pin;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.byu.ece.rapidSmith.device.Tile;
import org.w3c.dom.html.HTMLTableColElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Julian Käuser on 14.09.17.
 */
public class InterconnectRepair {
    private static final Logger logger = LoggerFactory.getLogger(MoveModulesEverywhere.class);

    private Design brokenDesign;
    private Device device;
    private WireEnumerator wireEnum;
    private Boolean isFixed = false; //Flag die wir nutzen können um die Reperatur ggf. zu überspringen

    private Collection<Potential> potentials;
    /*
    This class holds the minitask 2 invocation methods etc. Created mainly for testing reasons
     */
    public InterconnectRepair(Design brokenDesign){
        this.brokenDesign = brokenDesign; //das merken wir uns
        this.device = brokenDesign.getDevice(); //das sind nur hilfs variablen
        this.wireEnum = this.device.getWireEnumerator();
        this.isFixed = false;
    }

    public Design fixDesign(){
        for (Net aNet : this.brokenDesign.getNets()){
        //    this.checkIfBroken(aNet);
            for(Pin p : aNet.getPins()) {
                KabelBaum k = new KabelBaum(p, this.brokenDesign);
                logger.info(String.valueOf(k.getMight()));
                logger.info(String.valueOf(k.isComplete()));
            }
        }
        return this.brokenDesign;
    }

    private PIP getPIPfromPin(Pin pin){
        Net net = pin.getNet();
        //WireEnum.
        int pinWire = this.device.getPrimitiveExternalPin(pin);
        for (PIP pip : net.getPIPs()){
            if (pip.getEndWire() == pinWire){ //ist das richtigrum ?
                return pip;
            }
            if (pip.getStartWire()== pinWire){ //ich hoffe das wir auf die weise keinen Fehler machen
                return pip;
            }
        }
        return null;
    }

    /*
    Ermittelt ein PIP ausgehentn von einem anderen PIP. Da es mehere geben kann muss immer das vorige PIP d.h. das nicht zu ermittelde mitgeben werden.
    Außerdem brauchen wir ein net da das Framework das sonst nicht einfach biten kann
     */
    private PIP getPIPfromPIP(Net net, PIP currentPip, PIP previousPip){
        for (PIP aPip : net.getPIPs()){
            if(aPip.getEndWire() == currentPip.getStartWire()){
                if(aPip != previousPip)
                    return aPip;
            }
            if(aPip.getStartWire()== currentPip.getEndWire()){
                if(aPip != previousPip)
                    return aPip;
            }
        }
        return null;
    }

    private Boolean checkIfPIP(int wire){
        return this.wireEnum.isPIPWire(wire);
    }
/*
    private Boolean checkIfPIPon(PIP pip){
        return false;
    }

    private Boolean checkIfWireOkay(int wire){
        if(checkIfPIP(wire)){
            checkIfPIPon(PIP)
        }
        return false;
    }
*/

/*
Start pin -> start wire
start wire -> start pip
A = start pip
End pin -> end wire
end wire -> end pip
B = end pip

Breitensuche prüfen ob pips verbinung haben

Wenn So ist:
    Fertig :D

Wenn NICHT:
    Breitsuche von bieden Richtungen und jedes Pip was möglicherweise mit dem Pip aus der anderen Richtung verbindbar ist verbinden wenn und nur dann wenn die Verbindung NICHT ein anderes pip noch mit integriert.
    Wenn Geht:
        Problem gelöst
    Wenn Nicht:
        Kapput, ist halt so -> route bitte weil schneller
 */
    private Boolean checkIfSourceReachable(Pin sinkPin, Pin sourcePin){
        Net net = sourcePin.getNet();
        Tile tile = sourcePin.getTile();
        int sourceWire = this.device.getPrimitiveExternalPin(sourcePin);
        //Sink to Source weil es nur eine Source gibt
        PIP sinkPip = this.getPIPfromPin(sinkPin);
        PIP prevPip = sinkPip;
        PIP nextPip = this.getPIPfromPIP(net, sinkPip, prevPip);
        if(nextPip == null) { //wenn wir absolut nicht durch kommen weil kein pip mehr
            logger.error("Pip wires broken");
            return false;
        }
        while(!(nextPip.getEndWire() == sourceWire || nextPip.getStartWire() == sourceWire)){ //sollange wir nicht noch irgendwie am Pin sind
            nextPip = this.getPIPfromPIP(net, nextPip, prevPip);
            if(nextPip == null) { //wenn wir absolut nicht durch kommen weil kein pip mehr
                logger.error("Pip wires broken");
                return false;
            }
        }
        logger.info("Nice");

        //Das folgende geht NICHT weil die das nicht implementiert haben d.h. nicht richtig
        //   logger.info(this.device.getNodeFromPin(sourcePin).getSinkPin().toString());
        /*
        if (this.device.getNodeFromPin(sourcePin).getSinkPin().toString() == sinkPin.toString()){
            logger.info("great Success");
        }else {
            logger.info("even greater Failure");
        }
        */
        return true; //wir sind erfolgreich einmal durchgelaufen
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
