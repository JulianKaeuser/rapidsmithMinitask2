package minitask_2;

import de.tu_darmstadt.rs.MoveModulesEverywhere;
import edu.byu.ece.rapidSmith.design.*;
import edu.byu.ece.rapidSmith.device.PrimitiveSite;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Julian Käuser on 18.09.17.
 *
 * A Potential is a set of wires and pips which form an isoelectric in the fpga fabric. If a pip in included, both ends
 * of the pip are part of the potential. if a pip is adjacent to the potential, one end is connected to the potential,
 * while the other is part of another potential or undefined (floating). Setting this pip integrates the pip into the
 * adjacent potentials and thus fuses them.
 */
public class Potential {

    private static final Logger logger = LoggerFactory.getLogger(MoveModulesEverywhere.class);

    private final int instanceID; //XD bitte immer private lassen und geter nutzen.

    private static int runningID = 0;


        /* #####################################################
                       constructor(s)
        ######################################################## */


    /**
     * Constructor based on pin
     * @param design
     * @param startPin
     */
    public Potential(Design design, Pin startPin){
        instanceID = assignRunningID();
        wires = new HashSet<Integer>();
        pips = new HashSet<PIP>();
        adjacentPIPs = new HashSet<PIP>();
        pins = new HashSet<Pin>();
        tiles = new HashSet<Tile>();

        //TODO find out how to get from pin to wire
        wires.add(startPin.getInstance().getPrimitiveSite().getExternalPinWireEnum(startPin.getName()));
        pins.add(startPin);
        tiles.add(startPin.getTile());
        net=startPin.getNet();
        this.design = design;
        this.expandAll();
    }

    public int getInstanceID(){
        return this.instanceID;
    }

    /*
    /**
     * Defines the first point of the Potential and therefore the first "wire tree"
     * @param startWire the first piece of metal, as wire
     *

    public Potential(DesignPotentials designWrapper, int startWire){
        wires = new HashSet<Integer>();
        pips = new HashSet<PIP>();
        adjacentPIPs = new HashSet<PIP>();
        pins = new HashSet<Pin>();

        wires.add(startWire);
        //TODO find out how to get from wire to net etc
        net=null;

        this.designWrapper = designWrapper;
        this.design = designWrapper.getDesign();
        this.expandAll();

    }
    */


    /**
     * Constructor with pip as the first point of the potential
     * @param designWrapper
     * @param startPip
     *
    public Potential(DesignPotentials designWrapper, PIP startPip){
        wires = new HashSet<Integer>();
        pips = new HashSet<PIP>();
        adjacentPIPs = new HashSet<PIP>();
        pins = new HashSet<Pin>();

        wires.add(startPip.getStartWire());
        wires.add(startPip.getEndWire());
        //TODO find out how to get from wire to net etc
        net=null;
        pips.add(startPip);

        this.designWrapper = designWrapper;
        this.design = designWrapper.getDesign();
        this.expandAll();

    }
    */


    // ###############################################
        //             attributes
        // ###############################################

        // The DEsignPotentials pobject which holds this potential
//        private DesignPotentials designWrapper;
        // all wires with this potential
        private Collection<Integer> wires;
        // the PIPs of this potential line
        private Collection<PIP> pips;
        // all pips which can connect this potential to another potential (=fuse)
        private Collection<PIP> adjacentPIPs;

        private Collection<Pin> pins;

        // The design where this potential is embedded
        private Design design;

        // the net of this potential
        private Net net;


        // List of all Tiles we have wires, pins, pips ... in
        private Set<Tile> tiles;


        /* ###############################################
                       methods
         *################################################
        */



    /**
     * Returns the wires
     * @return
     */
    public Collection<Integer> getWires(){
            return wires;
        }

    /**
     * Sets the wires
     * @param wires
     */
    public void setWires(Collection<Integer> wires){
            this.wires = wires;
        }

    /**
     * Sets the pips
     * @param pips
     */
    public void setPips(Collection<PIP> pips){
            this.pips = pips;
        }

    /**
     * Returns the pips
     * @return
     */
    public Collection<PIP> getPIPs(){
            return this.pips;
        }

    /**
     * Every
     * Returns all PIPs which can connect this potential to another potential.
     * @return
     */
    public Collection<PIP> getAdjacentPIPs() {
            return adjacentPIPs;
        }

    /**
     * Returns the net which this potential belongs to, if this is known.
     * @return
     */
    public Net getNet() {
        return net;
    }

    /**
     * Sets the net of this potential
     * @param net
     */
    public void setNet(Net net){
        this.net = net;
    }

    /**
     * Returns the wrapper object of type DesignPotentials where this Potential is part of
     * @return
     */
/*    public DesignPotentials getDesignPotentials(){
        return this.designWrapper;
    } */

    /**
     * Returns the included pins
     * @return
     */
    public Collection<Pin> getPins(){
        return this.pins;
    }

    /**
     * Sets the pins.
     * @param newPins
     */
    public void setPins(Collection<Pin> newPins){
        this.pins = newPins;
    }


    /**
     * Checks if the given pin is on the potential.
     * @param pin
     * @return true if so, false if not
     */
    public boolean isPinOfPotential(Pin pin){
       //if (wires.contains(pin.getInstance().getPrimitiveSite().getExternalPinWireEnum(pin.getPrimitiveSitePinName()))) return true;
        if(pins.contains(pin)) return true; //diese Prüfung ist richtig
        return false;
    }
    /**
     * Checks if this wire is on this potential
     * @param wire
     * @return
     */
    public boolean isWireOfPotential(Integer wire){
        if (wires.contains(wire)) return true;
        return false;
    }

    /**
     * Checks if this PIP is yet integrated in this potential (electrically: are both sides of the pip on the same potential?)
     * @param pip
     * @return
     */
    public boolean isPIPOfPotential(PIP pip){
        if(pips.contains(pip)){
            return true;
        }
        return false;
    }

    /**
     * Reveals whether the given PIP is adjacent (can connect this potential with another) to this potential.
     * @param pip
     * @return
     */
    public boolean isPIPAdjacent(PIP pip){
        if (adjacentPIPs.contains(pip)) return true;
        return false;
    }

    /**
     * Literally spoken: connects the two potentials electrically; this means, they are then the same potential.
     * If the potential should be fused with itself (incorrect bevhaviour), null is returned
     * @param other the removed Potential
     */
    private Potential fuse(Potential other){

        if (other.equals(this)) return null;
            for (Integer otherWire : other.getWires()){
                wires.add(otherWire);
            }
            for(PIP otherPip : other.getPIPs()){
                pips.add(otherPip);
            }
            for (PIP otherPIP : other.getAdjacentPIPs()){
                this.adjacentPIPs.add(otherPIP);
            }
            other.clear();
            return other;


        }

    /**
     * NEVER actively use this method! It is only there to be called from the method DesignPotentials.fuse. We could not
     * make up how to solve this properly (public, protected, private, no modifier are not the right solutions)
     *
     * connects this potential with the given potential, by setting/including the given pip.
     * This operation fails if the fuse flag of the associated DEisgnPotentials wrapper is not set, indicating that the method
     * has not been called from the designwrapper.fuse() method. This secures that no wrong data structures are kept.
     * In this case, null is returned.
     * @param other
     * @param pip
     * @assert this.net != other.net
     * @return the activated pip (was the parameter)
     */
 /*   public Potential fuse(Potential other, PIP pip){
        if(!designWrapper.getFuseFlag()){
            return null;
        }

        other.getAdjacentPIPs().remove(pip);
        this.fuse(other);
        // pip is now integrated;
        this.adjacentPIPs.remove(pip);
        this.pips.add(pip);
        return this;
    }
*/
    /**
     * Removes all elements from this potential
     */
    public void clear(){
            wires.clear();
            pips.clear();
            adjacentPIPs.clear();
    //        designWrapper = null;
            this.design = null;

        //TODO check if all dependencies are removed
        }

    /**
     * This method includes all wires which are now connected to this potential in this object, and re-adjusts
     * the "borders" (i.e. pips)
     */
    private void expandAll(){
        Collection<Integer> wiresToAdd = new HashSet<Integer>();
        wiresToAdd.add(-1);

        Collection<Tile> tilesToAdd = new HashSet<Tile>();
        int counter = 0;
        this.checkForNewPins();
        //logger.info("expandAll() on potential #"+instanceID);
        while(!wiresToAdd.isEmpty() || !tilesToAdd.isEmpty()){
            //logger.info("iteration nr. "+counter);
            wiresToAdd.clear();
            tilesToAdd.clear();

            Pair<Collection<Integer>, Collection<Tile>> p = this.expandOne();

            wiresToAdd = p.getA();
            wires.addAll(wiresToAdd);

            tilesToAdd = p.getB();
            tiles.addAll(tilesToAdd);

            this.checkForNewPins(wiresToAdd);
            counter++;
        }
    }

    /**
     * This method generates a Collection of wires which have been detected as actual part of the potential and which
     * hence must be integrated into the potential.
     * The method uses side effects to manipulate the pip, pin and adjacentPip fields of the object
     * @return
     */
    private Pair<Collection<Integer>, Collection<Tile>> expandOne(){
        //TODO implement this method based on wires/tiles/whatever offers the best methods
        int counter = 0;



        // add all wires connected to all known pins

        for (Pin pin : pins){
            int thisPinsConnectedWire =  pin.getInstance().getPrimitiveSite().getExternalPinWireEnum(pin.getName());
            wires.add(thisPinsConnectedWire);
            tiles.add(pin.getTile());
        }

        // add all wires which are connected to pips which are on this potential (both connectors) (and not only one connector = adjacent)

        for (PIP pip : pips){
            wires.add(pip.getStartWire());
            wires.add(pip.getEndWire());
            tiles.add(pip.getTile());
        }

        // TODO check beforehand which pips are activated on this net...

        // look at all connections from all wires
        Set<Integer> wiresToAdd = new HashSet<Integer>();
        Set<Tile> tilesToAdd = new HashSet<Tile>();
        for (int existingWire : wires){
            Collection<PIP> netPIPsWithThisWire = new HashSet<PIP>(); // holds all pips with this wire as start or end point
            for (PIP pip : net.getPIPs()){
                if (pip.getStartWire()==existingWire || pip.getEndWire()==existingWire){
                    //The net has got a pip set which has the current wire as start or end point
                    netPIPsWithThisWire.add(pip);
                   // tiles.add(pip.getTile()); // not sure if this is necessary
                }
            }
            // then look outgoing from every pin the potential kno, how far it reaches

            for (Tile tile : tiles){
            //for (Pin existingPin : pins) {
                // all coennctions which can be reched from this pin/this pin's wire
               // WireConnection[] existingConnectionsForExistingWire = existingPin.getInstance().getTile().getWireConnections(existingWire);
                WireConnection[] existingConnectionsForExistingWire = tile.getWireConnections(existingWire);
                if (existingConnectionsForExistingWire!= null){
                    for (WireConnection wc : existingConnectionsForExistingWire) {
                        // wire can be reached directly
                        if (!wc.isPIP()) {
                            // add to potential
                            wiresToAdd.add(wc.getWire());
                            tilesToAdd.add(wc.getTile(tile));
                            tilesToAdd.add(tile);
                        }
                        if (wc.isPIP()) {
                            // wire would have to be set to be reached. check if it is set in next steps
                            boolean isAdjacent = true;
                            for (PIP pip : netPIPsWithThisWire) {
                                // for every pip which is set in this net with the current wire at at least one end,
                                // see how it connects and add it to this potential

                                if (pip.getStartWire() == wc.getWire()) {
                                    wiresToAdd.add(pip.getStartWire());
                                    wiresToAdd.add(pip.getEndWire());
                                    tilesToAdd.add(wc.getTile(tile));
                                    tilesToAdd.add(tile);
                                    isAdjacent = false;

                                } else if (pip.getEndWire() == wc.getWire()) {
                                    wiresToAdd.add(pip.getEndWire());
                                    wiresToAdd.add(pip.getStartWire());
                                    tilesToAdd.add(wc.getTile(tile));
                                    tilesToAdd.add(tile);
                                    isAdjacent = false;
                                }
                            }
                            // if the flag is not resetted, this means that there is no pip in the net connecting the wc pip
                            // which has one end wire in the potential then, but not the other. it is adjacent.
                            if (isAdjacent) {
                               // if (adjacentPIPs.add(new PIP(existingPin.getTile(), existingWire, wc.getWire()))) counter++;
                               adjacentPIPs.add(new PIP(tile, existingWire, wc.getWire()));
                            }
                        }
                    }
                }
            }
        }
        wiresToAdd.removeAll(wires);
        tilesToAdd.removeAll(tiles);

        Pair<Collection<Integer>, Collection<Tile>> returnPair = new Pair<Collection<Integer>, Collection<Tile>>(wiresToAdd, tilesToAdd);
        return returnPair;
    }

    /**
     * Crawls the wires for pin connections which are not yet stored in the pin set.
     * @param newWires the recently added wires - eases the search and shortens the execution.
     */
    private void checkForNewPins(Collection<Integer> newWires){
        for (Integer currentWire : newWires){
            if (pins.addAll(this.getAllConnectedPins(currentWire))){
               // logger.info("new pins discovered - " +pins);
            }
        }
    }

    /**
     * Crawls the wires for pin connections which are not yet stored in the pin set.
     */
    private void checkForNewPins(){
        for (Integer wire : wires){
            if(pins.addAll(this.getAllConnectedPins(wire))){
               // logger.info("new pins discovered (wide search) - " +pins);
            }
        }
    }

    /**
     * Returns a collection of pins connected to that wire (on the device)
     * @param wire
     * @return
     */
    private Collection<Pin> getAllConnectedPins(Integer wire){
        Collection<Pin> newPins = new HashSet<Pin>();
        for (Instance inst : design.getInstances()){
            for (Pin pin : inst.getPins()){
                int pinWireEnumFromName= pin.getInstance().getPrimitiveSite().getExternalPinWireEnum(pin.getName());
               // int pinWireEnumFromSiteName = pin.getInstance().getPrimitiveSite().getExternalPinWireEnum(pin.getPrimitiveSitePinName());
                String pinName = pin.getName();
                String pinSiteName = pin.getPrimitiveSitePinName();
                if (pinWireEnumFromName==wire){
                    if (newPins.add(pin)) {
                        //logger.info("new pin discovered - " + pin + " ,tile:"+ inst.getTile()+" netSource: " + net.getSource() + " potential " + instanceID);
                    }
                }
            }
        }
        return newPins;
    }

    private Collection<Pin> getAllConnectedPinsAlternative(Integer wire){
        return null;
    }

    /**
     * Is unsignificant if no pips are included (only "floating" wire). If at least one pip is inside, wires are
     * connected.
     * @return
     */
    public boolean isSignificant(){
        if(pips.isEmpty()) return false;
        return true;
    }

    public int getWireCount(){
        return wires.size();
    }

    public int getPinCount(){
        return pins.size();
    }

    public int getPipCount(){
        return pips.size();
    }

    public int getCardinality(){
        return getWireCount()+getPinCount()+getPipCount();
    }

    public Set<Tile> getTiles(){
        return this.tiles;
    }

    public Boolean isPartlyInTile(Tile t){
        if (this.tiles.contains(t))
            return true;
        return false;
    }

    /**
     * Forces the expandAll method. For debugging reasons
     */
    public void forceExpansion(){
        this.expandAll();
    }

    public Boolean checkIfWirePointsToPin(Pin pin){
        pin.getTile();
        int pinWire = this.design.getDevice().getPrimitiveExternalPin(pin);
        for(WireConnection[] wc_array : pin.getTile().getWireHashMap().values()){ //gibt uns alle wires auf die gezeigt wird
            for(WireConnection wc : wc_array){
                if(wc.getWire() == pinWire)
                    return true;
                //auf uns wird gezeigt :D
            }
        }
        return false;
    }

    public Collection<Integer> getWireOfWire(Pin pin){
        Collection<Integer> wires = new HashSet<Integer>();
     //   for(Tile t : this.design.getDevice().getTiles()){
        pin.getTile();
        int pinWire = this.design.getDevice().getPrimitiveExternalPin(pin);
        for(WireConnection[] wc_array : pin.getTile().getWireHashMap().values()){ //gibt uns alle wires auf die gezeigt wird
            for(WireConnection wc : wc_array){
                if(wc.getWire() == pinWire)
                    return true;
                //auf uns wird gezeigt :D
            }

        }


        return wires;
    }

    /* ########################################
                  static methods
     ###########################################*/

    /**
     * Handles the classes ID and assigns a new ID if requested.
     * @return
     */
    private static int assignRunningID(){
        runningID++;
        return runningID;

    }

    private class Pair<A, B> {
        private A a;
        private B b;

        public A getA(){
            return a;
        }

        public B getB(){
            return b;
        }

        public void setA(A a){
            this.a = a;
        }

        public void setB (B b){
            this.b = b;
        }

        public Pair(A a, B b){
            setA(a);
            setB(b);
        }
    }
}
