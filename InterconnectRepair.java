package minitask_2;

import de.tu_darmstadt.rs.MoveModulesEverywhere;
import edu.byu.ece.rapidSmith.design.*;
import edu.byu.ece.rapidSmith.device.*;
import edu.byu.ece.rapidSmith.router.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

/**
 * Class which is responsible to extract the re-placed design, examine it regarding the error, if possile fix it, and return it.
 * Created by Julian Käuser on 14.09.17.
 */
public class InterconnectRepair {
    private static final Logger log = LoggerFactory.getLogger(MoveModulesEverywhere.class);

    private Design brokenDesign;
    private Device device;
    private WireEnumerator wireEnum;
    private Boolean isBroken = false; //Flag die wir nutzen können um die Reperatur ggf. zu überspringen

    private Collection<Potential> potentials;

    /*
    This class holds the minitask 2 invocation methods etc. Created mainly for testing reasons.
     */
    public InterconnectRepair(Design brokenDesign){
        this.brokenDesign = brokenDesign; //das merken wir uns
        this.device = brokenDesign.getDevice(); //das sind nur hilfs variablen
        this.wireEnum = this.device.getWireEnumerator();
        this.isBroken = checkIfDesignBroken(brokenDesign);
        log.info("This design is "+ (this.isBroken ? "conflicting " : "not conflicting"));
    }

    /**
     * Returns true if broken, false if correct
     * @param design
     * @return
     */
    public Boolean checkIfDesignBroken(Design design){
        boolean isBroken = false;
        for (Net net : design.getNets()){
            AdaptedHandRouter.routeHand(design, net);
            System.exit(0);
            Potential sourcePotential = new Potential(design,net.getSource());
            Collection<Pin> allPins = sourcePotential.getPins();

            Pin sourcePin= net.getSource();
            int sourcePinExternalWire = design.getDevice().getPrimitiveExternalPin(sourcePin);
            /*
            Node sourcePinNode = new Node(sourcePin.getTile(), sourcePinExternalWire, null, 0);
            WireConnection[] startableWires = sourcePin.getTile().getWireConnections(sourcePinNode.getWire());

            //Node sourceSwitchMatrixSink = sourcePinNode.getSwitchBoxSink(design.getDevice()); throws nullpointe rbecause of missing sink for the wire
            */

                /*
                log.info(" ");
                log.info("net " + net);
                log.info(" has " + net.getPins().size() + " pins, " + net.getPIPs().size() + " pips, sourcePin: " + net.getSource());
                log.info(" source pin :                 "+ sourcePin);
                log.info(" source pin wire:             "+ sourcePinExternalWire);
                */


            // iterate all pins in net
            for(Pin pin : net.getPins()) {
                if (pin.equals(sourcePin)){
                    continue;
                }

                int externalWireOfPin =  design.getDevice().getPrimitiveExternalPin(pin);
                /*
                System.out.println("                                               handling pin "+ pin);
                log.info("     externalWire:        "+externalWireOfPin);
                */

                Node pinSinkNode = new Node(pin.getTile(), externalWireOfPin, null, 0 ); // the node for this pin - directly connected
                SinkPin pinSinkPin = pinSinkNode.getSinkPin();
                Node switchMatrixSink;
                if (pinSinkPin!=null) {
                    int pinSwitchMatrixWire = pinSinkPin.switchMatrixSinkWire;
                    log.info("     pinSwitchMatrixWire: " + pinSwitchMatrixWire);
                    boolean isContained = sourcePotential.isWireOfPotential(pinSwitchMatrixWire);
                    log.error("     " + (isContained ? "is " : "is not ") + "contained in potential");
                    switchMatrixSink = pinSinkNode.getSwitchBoxSink(design.getDevice());


                    Potential sinkPinPotential = new Potential(design, pin);
                }


               // log.error(" switchMatrixSinkWire of pin"+ p+ " = "+handRouterTypeWire);
                if (!sourcePotential.isPinOfPotential(pin)) {
                    Collection<Integer> pointers = checkIfWirePointsToPin(design, pin);
                    // basically, it is broken at this point

                    if (!pointers.isEmpty()){
                       // log.info("something points to this pin : "+pin+" " + pointers);
                    }

                    /*
                    comment this in or replace with "return true;" if funtion shall be used. For analysis reasons, this is skipped
                     */
                    isBroken = true;

                    Potential errorPinPotential = new Potential(design, pin);

                    if (!sourcePotential.isPartlyInTile(pin.getTile())) {
                            //log.error(" tile is not even on potential - existing tiles: "+sourcePotential.getTiles());
                    } else {
                        //log.info("tile "+pin.getTile()+"is on potential");


                       // log.info(" wire of the pin: "+ wireEnum.getWireName(externalWireOfPin));   
                        //sourcePotential.forceExpansion();
                        if (sourcePotential.getWires().contains(externalWireOfPin)){
                            //log.info(" is contained in potential");
                        }
                        //log.info(" ");
                    }
                }
            }
        }
        return isBroken;
    }

    public Design fixDesign(){
        for (Net net : this.brokenDesign.getNets()){
            for(Pin p : net.getPins()) {
                Potential pot = new Potential(this.brokenDesign, p);
            }
        }
        return this.brokenDesign;
    }


/*
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


//    Ermittelt ein PIP ausgehentn von einem anderen PIP. Da es mehere geben kann muss immer das vorige PIP d.h. das nicht zu ermittelde mitgeben werden.
//    Außerdem brauchen wir ein net da das Framework das sonst nicht einfach biten kann

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

    private Boolean checkIfSourceReachable(Pin sinkPin, Pin sourcePin){
        Net net = sourcePin.getNet();
        Tile tile = sourcePin.getTile();
        int sourceWire = this.device.getPrimitiveExternalPin(sourcePin);
        //Sink to Source weil es nur eine Source gibt
        PIP sinkPip = this.getPIPfromPin(sinkPin);
        PIP prevPip = sinkPip;
        PIP nextPip = this.getPIPfromPIP(net, sinkPip, prevPip);
        if(nextPip == null) { //wenn wir absolut nicht durch kommen weil kein pip mehr
            log.error("Pip wires broken");
            return false;
        }
        while(!(nextPip.getEndWire() == sourceWire || nextPip.getStartWire() == sourceWire)){ //sollange wir nicht noch irgendwie am Pin sind
            nextPip = this.getPIPfromPIP(net, nextPip, prevPip);
            if(nextPip == null) { //wenn wir absolut nicht durch kommen weil kein pip mehr
                log.error("Pip wires broken");
                return false;
            }
        }
        log.info("Nice");


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
    */


    /**
     * Returns a collection of all wires poiting to this pin.
     * @param design
     * @param pin
     * @return
     */
    public static Collection<Integer> checkIfWirePointsToPin(Design design, Pin pin){
        Collection<Integer> ret = new HashSet<Integer>();
        int pinWire = design.getDevice().getPrimitiveExternalPin(pin);
        Collection<Integer> a = pin.getTile().getWireHashMap().keySet();
        for (Integer key : a){
            WireConnection[] valueWC = pin.getTile().getWireHashMap().get(key);
             //gibt uns alle wires auf die gezeigt wird
            for(WireConnection wc : valueWC){
                if(wc.getWire() == pinWire)
                    ret.add(key);
                //auf uns wird gezeigt :D
            }
        }
        return ret;
    }


    /**
     * Check if there is some wire pointing to this pin
     * @param design
     * @param pin
     * @param tile
     * @return
     */
    public Boolean checkIfWirePointsToPinBool(Design design, Pin pin, Tile tile){
        int pinWire = design.getDevice().getPrimitiveExternalPin(pin);
        if (tile.getWireHashMap()!=null) {
            for (WireConnection[] wc_array : tile.getWireHashMap().values()) { //gibt uns alle wires auf die gezeigt wird
                for (WireConnection wc : wc_array) {
                    if (wc.getWire() == pinWire)
                        return true;
                    //auf uns wird gezeigt :D
                }
            }
        }
        return false;
    }


}
