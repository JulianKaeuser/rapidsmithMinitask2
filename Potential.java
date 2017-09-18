package minitask_2;

import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.device.Tile;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Julian Käuser on 18.09.17.
 */
public class Potential {


    private class Potential {

        public Potential(){
            wires = new HashSet<Integer>;
            pips = new HashSet<PIP>();
        };

        // all wires with this potential
        private Collection<Integer> wires;
        // the PIPs of this potential line
        private Collection<PIP> pips;

        /*
        Should work
         */
        private Collection<Tile> underlyingPIPTiles;

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

        public boolean addPip(PIP pip){
            return pips.add(pip);
        }





        /*
        Checks if this wire is on this potential
         */
        public boolean isWireOfPotential(Integer wire){
            if (wires.contains(wire)) return true;
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
         * Removes all wires and pips
         */
        public void clear(){
            wires.clear();
            pips.clear();
        }



    }

    /**
     * Returns the potential of this wire. If no potential is defined yet, null is returned.
     * @param wire
     * @return the Potential holding this wire
     */
    public Potential getPotential(int wire){
        for (Potential pot : potentials){
            if (pot.isWireOfPotential(wire)){
                return pot;
            }
        }
        return null;
    }
}
