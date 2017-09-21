package minitask_2;

import de.tu_darmstadt.rs.MoveModulesEverywhere;
import edu.byu.ece.rapidSmith.design.*;
import edu.byu.ece.rapidSmith.device.PrimitiveSite;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.WireConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Konkurenzklasse zu Potenial
 * Created by Flo on 21.09.2017.
 */
public class KabelBaum {

    private static final Logger log = LoggerFactory.getLogger(MoveModulesEverywhere.class);

    private static int runningID = -1;

    /**
     * Handles the classes ID and assigns a new ID if requested.
     * @return
     */
    private static int assignRunningID(){
        runningID++;
        return runningID;
    }

    private int instanceID;
    private Design design;
    private Collection<Integer> wires;
    private Net myNet;
    private Boolean complete = false; //sollange falsch wie man das erweiteren kann. Kappute Nets bleiben falsch.

    private Collection<PIP> PIPsToCheck;

    public KabelBaum(Pin startPin, Design design){
        this.instanceID = assignRunningID();
        this.design = design;
        this.wires = new HashSet<Integer>();
    //    wires.add(startPin.getInstance().getPrimitiveSite().getExternalPinWireEnum(startPin.getName()));
        wires.add(design.getDevice().getPrimitiveExternalPin(startPin));
        this.myNet=startPin.getNet();

        //kümmere dich drum den Baum zu erweiteren
        this.PIPsToCheck = new ArrayList<PIP>();
        for (PIP p : this.myNet.getPIPs()) {
            this.PIPsToCheck.add(p);
        }
        this.complete = this.expandKabelBaum();
    }

    //Viele Einträge ist heißt hier mächtiger Baum
    public int getMight(){
        return this.wires.size();
    }

    public Boolean isComplete(){
        return this.complete;
    }

    public Boolean isSignificant(){
        if(this.wires.size()>1)
            return true;
        return false;
    }

    private Boolean expandKabelBaum(){
        while(this.PIPsToCheck.size()>0){
            Boolean at_least_one_pip_used = false;
            Collection<PIP> removeList = new ArrayList<PIP>();
            for(PIP p : this.PIPsToCheck){
                if(wires.contains(p.getStartWire()) || wires.contains(p.getEndWire())) {
                //    this.PIPsToCheck.remove(p); //pop
                    removeList.add(p);
                    at_least_one_pip_used = true; //flag setzen das wir noch was machen
                    if (wires.contains(p.getStartWire()))
                        wires.add(p.getEndWire());
                    if (wires.contains(p.getEndWire()))
                        wires.add(p.getStartWire());
                }
            }
            this.PIPsToCheck.removeAll(removeList);
            if(at_least_one_pip_used == false){
                return false;
            }
        }
        return true;
    }

    private Boolean wireInBaum(int w){
        return wires.contains(w);
    }

}
