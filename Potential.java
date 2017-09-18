package minitask_2;

import edu.byu.ece.rapidSmith.design.Design;
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
                       constructor
        ######################################################## */

        public Potential(Design design) {
            wires = new HashSet<Integer>;
            pips = new HashSet<PIP>();
            if (allPotentials.get(design) == null) {
                allPotentials.put(design, new HashSet<Potential>());

            }
                allPotentials.get(design).add(this);
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
    public boolean addWire(Integer wire){
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
     * Literally spoken: connects the two potentials electrically; this means, they are then the same potential
     * @param other
     */
    public Potential fuse(Potential other){
            for (Integer otherWire : other.getWires()){
                wires.add(otherWire);
            }
            for(PIP otherPip : other.getPIPs()){
                pips.add(otherPip);
            }
            other.clear();



        }

    /**
     * Removes all elements from this potential
     */
    public void clear(){
            wires.clear();
            pips.clear();
            adjacentPIPs.clear();

        }


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
        Potential a, b = null;
        for (Potential p : allPotentials.get(design)){
            if (p.getAdjacentPIPs().contains(pip)){
                a = p;
                set.add(a);
            }
            if(a!=null && !p.equals(a) && p.getAdjacentPIPs().contains(pip)){
                b=p;
                set.add(b);
            }
        }
    }
}
