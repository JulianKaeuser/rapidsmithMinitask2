package minitask_2;

import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.Tile;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Julian Käuser on 18.09.17.
 */
public class Potential {

        /*
        A Map which holds all potentials of all designs known in this runtime. Make sure that everytime a
        Design is finished with the processing, the function clearPotentials is called so that unnecessary
        memory allocation is avoided, if the potentials are not used again.
         */
        static HashMap<Design, Collection<Potential>> allPotentials;


        /* #####################################################
                       constructor(s)
        ######################################################## */

    /**
     * Constructor with design only; potential is undefined and therefore only internal use
     * @param design
     */
        private Potential(Design design) {
            wires = new HashSet<Integer>();
            pips = new HashSet<PIP>();
            adjacentPIPs = new HashSet<PIP>();
            this.design = design;
            this.underlyingPIPTiles = new HashSet<Tile>();
            design = design;
            if (allPotentials.get(design) == null) {
                allPotentials.put(design, new HashSet<Potential>());

            }
                allPotentials.get(design).add(this);
        }

    /**
     * Defines the first point of the Potential and therefore the first "wire tree"
     * @param design
     * @param wireAnchorPoint
     */
    public Potential(Design design, Integer wireAnchorPoint){
        wires = new HashSet<Integer>();
        pips = new HashSet<PIP>();
        adjacentPIPs = new HashSet<PIP>();
        this.underlyingPIPTiles = new HashSet<Tile>();
        this.design = design;
        if (allPotentials.get(design) == null) {
            allPotentials.put(design, new HashSet<Potential>());

        }
        allPotentials.get(design).add(this);
        wires.add(wireAnchorPoint);
        net=null;
        this.expandAllWires();

    }



        // ###############################################
        //          non-static attributes
        // ###############################################
        // all wires with this potential
        private Collection<Integer> wires;
        // the PIPs of this potential line
        private Collection<PIP> pips;
        // all pips which can connect this potential to another potential (=fuse)
        private Collection<PIP> adjacentPIPs;

        /*
           Should work
         */
        private Collection<Tile> underlyingPIPTiles;

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
     * Returns all PIPs which can connect this potential to another potential
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
     * Returns the underlying tile mesh of pips - device dependent
     * @return
     */
    public Collection<Tile> getUnderlyingPIPTiles() {
        return underlyingPIPTiles;
    }

    /**
     * Sets the underlying tile mesh
     * @param underlyingPIPTiles
     */
    public void setUnderlyingPIPTiles(Collection<Tile> underlyingPIPTiles) {
        this.underlyingPIPTiles = underlyingPIPTiles;
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
     * Literally spoken: connects the two potentials electrically; this means, they are then the same potential
     * @param other
     */
    private Potential fuse(Potential other){
            for (Integer otherWire : other.getWires()){
                wires.add(otherWire);
            }
            for(PIP otherPip : other.getPIPs()){
                pips.add(otherPip);
            }
            for (PIP otherPIP : other.getAdjacentPIPs()){
                this.adjacentPIPs.add(otherPIP);
            }
            for (Tile otherTile : other.getUnderlyingPIPTiles()){
                this.underlyingPIPTiles.add(otherTile);
            }
            other.clear();
            allPotentials.get(this.design).remove(other);
            return other;


        }

    /**
     * connects this potential with the given potential, by setting/including the given pip.
     * This operation fails if not this and the other potential to be connected are only separated by at most one pip
     * (else, a more routing-like connection would have to be made). In this case, null is returned.
     * Null is also returned if the nets are actually different (if one net is null, this is ok)
     * @param other
     * @param pip
     * @assert this.net != other.net
     * @return the activated pip (was the parameter)
     */
    public PIP fuse(Potential other, PIP pip){
        if (!this.adjacentPIPs.contains(pip) || !other.getAdjacentPIPs().contains(pip)){
            // pip is not in both connectable pip sets, i.e. common of both potentials
            return null;
        }
        if (this.net !=null && other.net !=null &&(!this.net.equals(other.net))){
            return null;
        }
        other.getAdjacentPIPs().remove(pip);
        this.fuse(other);
        // pip is now integrated;
        this.adjacentPIPs.remove(pip);
        this.pips.add(pip);
        return pip;
    }

    /**
     * Removes all elements from this potential
     */
    public void clear(){
            wires.clear();
            pips.clear();
            adjacentPIPs.clear();
            underlyingPIPTiles.clear();


        }

    /**
     * This method includes all wires which are now connected to this potential in this object, and re-adjusts
     * the "borders" (i.e. pips)
     */
    private void expandAllWires(){
        design.getDevice().
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
     public static Potential getPotentialOfWire(Design design, int wire){
        if (allPotentials.get(design)==null){
            return null;
        }
        for (Potential pot : allPotentials.get(design)){
            if (pot.isWireOfPotential(wire)){
                return pot;
            }
        }
        return null;
    }

    /**
     * Return the potential of the given pip
     * @param design
     * @param pip
     * @return
     */
    public static Potential getPotentialOfPIP(Design design, PIP pip){
        if (allPotentials.get(design)==null){
            return null;
        }
        for (Potential pot : allPotentials.get(design)){
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
    public static Potential getPotential(Design design, int wire){
        if (allPotentials.get(design)==null){
            return null;
        }
        for (Potential pot : allPotentials.get(design)){
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
    public static Collection<Potential> getAdjacentPotentialsOfPIP(Design design, PIP pip){
        if (allPotentials.get(design)==null){
            return null;
        }
        HashSet<Potential> set = new HashSet<Potential>();
        for (Potential p : allPotentials.get(design)){
            if (p.getAdjacentPIPs().contains(pip)){
                set.add(p);
            }
            if(!set.contains(p) && p.getAdjacentPIPs().contains(pip)){
                set.add(p);
            }
        }
        return set;
    }


    /**
     * Determines if the given Potential is significant or not
     * @param other
     * @return
     */
    public static boolean isSignificantPotential(Potential other){
        return other.isSignificant();
    }


    /**
     * Reveals if the two Pips are on the same potential
     * @param design
     * @param a
     * @param b
     * @return
     */
    public static boolean isSamePotential(Design design, PIP a, PIP b){
        for (Potential p : allPotentials.get(design)){
            if (p.getPIPs().contains(a) && p.getPIPs().contains(b)) return true;
            return false;
        }
    }

}
