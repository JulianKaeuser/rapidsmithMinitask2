package minitask_2;

import de.tu_darmstadt.rs.MoveModulesEverywhere;
import edu.byu.ece.rapidSmith.design.*;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.WireConnection;
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
    private static final Logger log = LoggerFactory.getLogger(MoveModulesEverywhere.class);

    private Design brokenDesign;
    private Device device;
    private WireEnumerator wireEnum;
    private Boolean isBroken = false; //Flag die wir nutzen können um die Reperatur ggf. zu überspringen

    private Collection<Potential> potentials;

    /*
    This class holds the minitask 2 invocation methods etc. Created mainly for testing reasons. Also to be lazy with the git
     */
    public InterconnectRepair(Design brokenDesign){
        this.brokenDesign = brokenDesign; //das merken wir uns
        this.device = brokenDesign.getDevice(); //das sind nur hilfs variablen
        this.wireEnum = this.device.getWireEnumerator();
        this.isBroken = checkIfDesignBroken(brokenDesign);
        log.info("This design is "+ (this.isBroken ? "broken " : "not broken"));
    }

    //geht

    /**
     * Returns true if broken, false if correct
     * @param aDesign
     * @return
     */
    public Boolean checkIfDesignBroken(Design aDesign){
        boolean isBroken = false;
        int netCounter = 0;
        for (Net aNet : aDesign.getNets()){

            Potential soll_pot = new Potential(aDesign,aNet.getSource());
            Collection<Pin> allPins = soll_pot.getPins();
            Pin s_p = aNet.getSource();
            int sourceWire = aDesign.getDevice().getPrimitiveExternalPin(s_p);
            if (netCounter<15) log.info(" ");
            if (netCounter<15) log.info("net "+ aNet.getName());
            if (netCounter<15) log.info(" has "+aNet.getPins().size()+" pins, "+aNet.getPIPs().size()+ " pips, sourcePin: "+aNet.getSource());
            for(Pin p : aNet.getPins()) {
            //    int wire = aDesign.getDevice().getNodeFromPin(p).getWire(); //kann auch wireConnections sein !
                //log.error(p+"");
                int wireOfPin =  p.getInstance().getPrimitiveSite().getExternalPinWireEnum(p.getName());
                if (!soll_pot.isPinOfPotential(p)) {
                    isBroken = true;
                    Potential errorPinPot = new Potential(aDesign, p);
                    if (netCounter<15) {
                        log.error(p + " ,tile " + p.getTile() + " is not on potential");

                        if (!soll_pot.isPartlyInTile(p.getTile())) {
                            log.error(" tile is not even on potential - existing tiles: "+soll_pot.getTiles());
                        } else {
                            log.info("tile "+p.getTile()+"is on potential");


                            log.info(" wire of the pin: "+ wireEnum.getWireName(wireOfPin));
                            //soll_pot.forceExpansion();
                            if (soll_pot.getWires().contains(wireOfPin)){
                                log.info(" is contained in potential");
                            }
                            log.info(" ");

                        }
                    }

                }

            }
            netCounter++;
            /*
            //ACHTUNG: pin source ist nicht kompatible mit net.getPins()! ich verwende getPins()
        //    Potential soll_pot = new Potential(aDesign,aNet.getPins().get(0));
            Potential soll_pot = new Potential(aDesign,aNet.getSource());
            for(PIP aPIP : aNet.getPIPs()){
                log.error(""+soll_pot.isPIPOfPotential(aPIP));
                if (!soll_pot.isPIPOfPotential(aPIP)) {
                    return true;
                }
            }
            */
            /*
            Potential soll_pot = new Potential(aDesign,aNet.getSource());
            Collection<Pin> allPins = soll_pot.getPins();
            int a = 54;
            Pin np = aNet.getSource();
            log.error(aDesign.getDevice().getNodeFromPin(np).getWire()+"");
            for(Pin p : aNet.getPins()) {
                int wire = aDesign.getDevice().getNodeFromPin(p).getWire(); //kann auch wireConnections sein !
                log.error(wire+"");
                if (!soll_pot.isWireOfPotential(wire)) {
                    return true;
                }
            }
            */
        }
        return isBroken;
    }

    public Design fixDesign(){
        for (Net aNet : this.brokenDesign.getNets()){
            for(Pin p : aNet.getPins()) {
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


}
