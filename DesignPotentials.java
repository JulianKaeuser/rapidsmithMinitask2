package minitask_2;

import edu.byu.ece.rapidSmith.design.Design;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


import edu.byu.ece.rapidSmith.design.Instance;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.design.Pin;

/**
 * Created by Julian Käuser on 20.09.17.
 * A DesignPotentials object holds all Potentials which are present in a Design. This is independent of the being routed or not.
 * The DesignPotentials object associates the potentials with their design and links them. Additionally,
 */
public class DesignPotentials {

    private Design design;
    private Set<Potential> allPotentials;

    public DesignPotentials (Design design){
        this.design = design;
        this.allPotentials = new HashSet<Potential>();

        for (Instance inst : design.getInstances()){
            for(Pin pin : inst.getPins()){
                Potential pot = new Potential(this, pin);
                allPotentials.add(pot);
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
        Potential ret = a.fuse(b, pip);
        allPotentials.remove(b);
        return a;
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




}
