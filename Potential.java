package minitask_2;

import edu.byu.ece.rapidSmith.design.*;
import edu.byu.ece.rapidSmith.device.PrimitiveSite;
import edu.byu.ece.rapidSmith.device.Tile;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Julian Käuser on 18.09.17.
 *
 * A Potential is a set of wires and pips which form an isoelectric in the fpga fabric. If a pip in included, both ends
 * of the pip are part of the potential. if a pip is adjacent to the potential, one end is connected to the potential,
 * while the other is part of another potential or undefined (floating). Setting this pip integrates the pip into the
 * adjacent potentials and thus fuses them.
 */
public class Potential {


        /* #####################################################
                       constructor(s)
        ######################################################## */


    /**
     * Constructor based on pin
     * @param designWrapper
     * @param startPin
     */
    public Potential(DesignPotentials designWrapper, Pin startPin){
        wires = new HashSet<Integer>();
        pips = new HashSet<PIP>();
        adjacentPIPs = new HashSet<PIP>();
        pins = new HashSet<Pin>();

        //TODO find out how to get from pin to wire
        wires.add(null);
        pins.add(startPin);
        net=startPin.getNet();
        this.designWrapper = designWrapper;
        this.design = designWrapper.getDesign();
        this.expandAll();
    }

    /**
     * Defines the first point of the Potential and therefore the first "wire tree"
     * @param startWire the first piece of metal, as wire
     */
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



        // ###############################################
        //          non-static attributes
        // ###############################################

        // The DEsignPotentials pobject which holds this potential
        private DesignPotentials designWrapper;
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
         * adds the wire to this potential
         * @param wire
         * @return
         */
    private boolean addWire(Integer wire){
            return (wires.add(wire));
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
    public DesignPotentials getDesignPotentials(){
        return this.designWrapper;
    }

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
        if(pins.contains(pin)) return true;
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
     * @param other
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
     * connects this potential with the given potential, by setting/including the given pip.
     * This operation fails if not this and the other potential to be connected are only separated by at most one pip
     * (else, a more routing-like connection would have to be made). In this case, null is returned.
     * Null is also returned if the nets are actually different (if one net is null, this is ok), or if the potential
     * other equals this potential (cannot fuse itself)
     * @param other
     * @param pip
     * @assert this.net != other.net
     * @return the activated pip (was the parameter)
     */
    package Potential fuse(Potential other, PIP pip){

        other.getAdjacentPIPs().remove(pip);
        this.fuse(other);
        // pip is now integrated;
        this.adjacentPIPs.remove(pip);
        this.pips.add(pip);
        return this;
    }

    /**
     * Removes all elements from this potential
     */
    public void clear(){
            wires.clear();
            pips.clear();
            adjacentPIPs.clear();
            designWrapper = null;
            this.design = null;

        //TODO check if all dependencies are removed
        }

    /**
     * This method includes all wires which are now connected to this potential in this object, and re-adjusts
     * the "borders" (i.e. pips)
     */
    private void expandAll(){
        //TODO implement this method based on wires/tiles/whatever offers the best methods
        Pin pin;
        //pin.
        pin.getNet().

    }

    /**
     * Is Unsignificant if no pips are included (only "floating" wire). If at least one pip is inside, wires are
     * connected.
     * @return
     */
    public boolean isSignificant(){
        if(pips.isEmpty()) return false;
        return true;
    }



    /* ########################################
         static methods
     ###########################################*/
    /**
     * Returns the potential of this wire. If no potential is defined yet, null is returned.
     * @param wire
     * @return the Potential holding this wire
     */
     public static Potential getPotentialOfWire(DesignPotentials designWrapper, int wire){
        for (Potential pot : designWrapper.getAllPotentials()){
            if (pot.isWireOfPotential(wire)){
                return pot;
            }
        }
        return null;
    }

    /**
     * Return the potential of the given pip
     * @param designWrapper
     * @return
     */
    public static Potential getPotentialOfPIP(DesignPotentials designWrapper, PIP pip){
        for (Potential pot : designWrapper.getAllPotentials()){
            if (pot.isPIPOfPotential(pip)){
                return pot;
            }
        }
        return null;
    }

    /**
     * Returns the potential of this wire. If no potential is defined yet, null is returned.
     * @param wire
     * @return the Potential holding this wire
     */
    public static Potential getPotential(DesignPotentials designWrapper, int wire){
        for (Potential pot : designWrapper.getAllPotentials()){
            if (pot.isWireOfPotential(wire)){
                return pot;
            }
        }
        return null;
    }


    /**
     * Returns a collection of potentials (might only be of cardinality 2) which are adjacent to the given
     * pip (=which would be fused if the pip is set).
     * @param pip
     * @return
     */
    public static Collection<Potential> getAdjacentPotentialsOfPIP(DesignPotentials designWrapper, PIP pip){
        HashSet<Potential> set = new HashSet<Potential>();
        for (Potential p : designWrapper.getAllPotentials()){
            if (p.getAdjacentPIPs().contains(pip)){
                set.add(p);
            }
            if(!set.contains(p) && p.getAdjacentPIPs().contains(pip)){
                set.add(p);
            }
        }
        return set;
    }




    /* #############################
        Interface whcih collects Pin, Pip, wire interface
     ################################*/
    public static class ElectricalObject{

        Integer wire;
        PrimitiveSite site;
        Pin pin;
        PIP pip;

        public boolean isWire;
        public boolean isPIP;
        public boolean isPin;

        public boolean isPrimitiveSite;

        public ElectricalObject(Integer wire){
            this.wire = wire;
            isWire = true;
            isPIP = false;
            isPin = false;

            isPrimitiveSite = false;
        }


        public ElectricalObject(PrimitiveSite site){
            this.site = site;
            isWire = false;
            isPIP = false;
            isPin = false;
            isPrimitiveSite = true;
        }

        public ElectricalObject(Pin pin){
            this.pin = pin;
            isWire = false;
            isPIP = false;
            isPin = true;
            isPrimitiveSite = false;
        }

        public ElectricalObject(PIP pip ){
            this.pip = pip;
            isWire = false;
            isPIP = true;
            isPin = false;
            isPrimitiveSite = false;
        }

        public ElectricalObject(Net net){
            this.pin = net.getSource();
            isWire = false;
            isPIP = false;
            isPin = true;
            isPrimitiveSite = false;
        }

        /**
         * A unified method to get the anchor point as wire, independent of the input type
         * @return
         */
        public Integer getElectricalObjectAsWire(){
        // TODO implement so that for each type, the input is converted to the output type

            if (isWire){
                return this.wire;
            }
            if(isPIP){
                return this.pip.getStartWire();
            }
            if(isPin){
            }
            if(isPrimitiveSite){

            }
            return -1;
        }

        /**
         * Returns the anchor point's net, deducted from the actual type
         * @return
         */
        public Net getElectricalObjectAsNet(){
            if (isPin) return pin.getNet();

                    //TODO implement correctly for all types
            return null;
        }

        public PIP getConnectedPIPs (Pin pin){

        }




    }
}
