package minitask_2;

import de.tu_darmstadt.rs.MoveModulesEverywhere;
import edu.byu.ece.rapidSmith.design.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Julian Käuser on 20.09.17.
 * A DesignPotentials object holds all Potentials which are present in a Design. This is independent of the being routed or not.
 * The DesignPotentials object associates the potentials with their design and links them. Additionally,
 */
public class DesignPotentials {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(MoveModulesEverywhere.class);
    private static int counter = 0;

    // dEsign to work on
    private Design design;
    // all potentials held in this design
    private Set<Potential> allPotentials;
    // whether potentials may be fused
    private boolean fuseFlag;

    /* ######################################
                constructor(s)
     ########################################*/

    /**
     * INitiliazes the object and finds all existing potentials
     * @param design
     */
    public DesignPotentials (Design design){
        this.design = design;
        this.allPotentials = new HashSet<Potential>();
        fuseFlag = false;

        boolean debugSpecific = false;

        for (Net net : design.getNets()){
            debugSpecific = false;
            if (net.getName().equals("module_instance/ethernet_rx_0/reset_restart[0]")&&counter==0) {
                debugSpecific = true;
            }
           if (debugSpecific) logger.info("Net "+net.getName()+" under review");
            Potential sourcePot = new Potential(this, net.getSource());
            allPotentials.add(sourcePot);
           if(debugSpecific) logger.info("source pin potential with ID "+ sourcePot.instanceID);

            int sinkPinCounter = 1;
            for (Pin sinkPin : net.getPins()){
               if(debugSpecific) logger.info("sink pin "+sinkPin.getName()+ " # "+ sinkPinCounter + "/"+net.getPins().size());
                sinkPinCounter++;
                if (this.getPotentialOfPin(sinkPin)==null) {
                    Potential potSink = new Potential(this, sinkPin);
                    if(debugSpecific) {
                        logger.info("sink pin has new potential - obvious task-wise error for sink pin " + sinkPin.getName());
                        logger.info("created new Potential for sinkPin " + sinkPin.getName() + " with ID" + potSink.instanceID);
                    }
                }
                else{
                    if(debugSpecific) logger.info("sink pin has defined potential");
                }
            }
            if(debugSpecific&&counter==0){
                counter++;
            }
        }
    }

    /**
     * ALWAYS use this method to fuse Potentials, do not use the one from the Potential class. The association
     * of design and potential can only be guaranteed if this method is used.
     *
     *
     * Fuses the two given potentials by setting the given pip, and updates the potential set of this object.
     * Returns the resulting Potential object.
     * If the potentials cannot be fused (e.g. the pip is not between them or they are not from the same design, or
     * a equals b
     * null is returned.
     * @param a
     * @param b
     * @param pip
     * @return
     */
    public Potential fusePotentials(Potential a, Potential b, PIP pip){

        if(a.equals(b)){
            return null;
        }
        if (!(allPotentials.contains(a)&&allPotentials.contains(b))){
            // not from same design
            return null;
        }
        if(!(a.getAdjacentPIPs().contains(pip)&&b.getAdjacentPIPs().contains(pip))){
            // pip cannot connect these two potentials to one
            return null;
        }
        /*
        if (a.getNet() !=null && b.getNet() !=null &&(!a.getNet().equals(b.getNet()))){
            return null;
        }
        */
        fuseFlag = true;
        Potential ret = a.fuse(b, pip);
        fuseFlag = false;
        allPotentials.remove(b);
        return a;
    }

    /**
     * Returns the state of the fuse flag
     * @return
     */
    public boolean getFuseFlag(){
        return fuseFlag;
    }

    /**
     * Returns the design of this object.
     * @return
     */
    public Design getDesign(){
        return this.design;
    }

    /**
     * Returns a collection of all potentials in this design
     * @return
     */
    public Collection<Potential> getAllPotentials(){
        return this.allPotentials;
    }


    /**
     * Returns the potential of this wire. If no potential is defined yet, null is returned.
     * @param wire
     * @return the Potential holding this wire
     */
    public Potential getPotentialOfWire( int wire){
        for (Potential pot : allPotentials){
            if (pot.isWireOfPotential(wire)){
                return pot;
            }
        }
        return null;
    }

    /**
     * Return the potential of the given pip. If not defined, null is returned.
     * @return
     */
    public Potential getPotentialOfPIP( PIP pip){
        for (Potential pot : allPotentials){
            if (pot.isPIPOfPotential(pip)){
                return pot;
            }
        }
        return null;
    }

    /**
     * Returns the potential of the gven pin, if yet defined. Otherwise, null is returned.
     * @param pin
     * @return
     */
    public Potential getPotentialOfPin(Pin pin){
        for (Potential pot : allPotentials){
            if (pot.isPinOfPotential(pin)){
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
    public  Collection<Potential> getAdjacentPotentialsOfPIP( PIP pip){
        HashSet<Potential> set = new HashSet<Potential>();
        for (Potential p : allPotentials){
            if (p.getAdjacentPIPs().contains(pip)){
                set.add(p);
            }
            if(!set.contains(p) && p.getAdjacentPIPs().contains(pip)){
                set.add(p);
            }
        }
        return set;
    }


}
