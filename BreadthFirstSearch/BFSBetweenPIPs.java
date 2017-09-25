package BreadthFirstSearch;

import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.design.Pin;
import minitask_2.Potential;

import java.util.*;

/**
 * Created by theChaoS on 18.09.2017.
 */
public class BFSBetweenPIPs {
    // the design
    private Design design;
    // potential of first pin
    private Potential potentialA;
    //potential of second pin
    private Potential potentialB;
    // a map holding all known potentials
    private HashMap<PIP, Potential> pipPotentialHashMap;

    // priority queue
    private PriorityQueue<PipNode> leaves;


    /**
     * Constructor which fills the priorityQueue and creates Potentials
     * @param design
     * @param a the first anchor pin   - either source or sink
     * @param b the second anchor pin  - either source or sink
     */
    public BFSBetweenPIPs(Design design, Pin a, Pin b){
        this.pipPotentialHashMap = new HashMap<PIP, Potential>();
        this.leaves = new PriorityQueue<>(new pipNodeComparator());

        Set<Potential> tempPotentials = new HashSet<Potential>();
        for(Net net : design.getNets()){
            for (Pin pin : net.getPins()){
                Potential pinPot = new Potential(design, pin);
                tempPotentials.add(pinPot);
                if(a.getNet().equals(net) && pinPot.isPinOfPotential(a)){
                    this.potentialA = pinPot;
                }
                if(b.getNet().equals(net) && pinPot.isPinOfPotential(b)){
                    this.potentialB = pinPot;
                }
            }
            for (Potential pot : tempPotentials){
                for (PIP pip : net.getPIPs()){
                    if(pot.isPIPOfPotential(pip)){
                        pipPotentialHashMap.put(pip, pot);
                    }
                }
            }

        }

        this.design = design;
        if (this.potentialA==null) this.potentialA = new Potential(design, a);
        if(potentialA.isPinOfPotential(b)){
            // everything should be fine then, better return
            // nothing to do, if you ask me..
        }
        if(this.potentialB==null) this.potentialB = new Potential(design, b);



        //wir sind NICHT connected, würden das gerne aber.
        //1.) Finde alle Pips am rand, wir arbeiten nur noch auf einer seite, damit ist der Name falsch XD
        for (PIP apip:this.potentialA.getAdjacentPIPs()){
            PipNode pN = new PipNode(apip,null);
            this.leaves.add(pN);
        }
    }


    /**
     * Geht rekursiv durch die Liste der PipNodes und verbindet PotentialB langsam mit PotentialA
     * @param lastPipNode the previous pip node
     */
    public void fixEverthing(PipNode lastPipNode){
        if(this.potentialA.equals(this.potentialB))
            return;
        PIP lastPip = lastPipNode.getPip();
        Potential lastPipPotenial = pipPotentialHashMap.get(lastPipNode.getPip());
        if(lastPipPotenial==null)
        this.potentialB.fuse(lastPipPotenial, lastPip);
        this.fixEverthing(lastPipNode.parrent);
    }

    public void doBreadthFirstSearch(){
        PipNode best = leaves.poll(); //der beste Knoten ist der mit dem kleinsten gewicht und ist immer vorne

        Collection<Potential> potentials = getAdjacentPotentialsOfPIP(best.getPip());
        //prüfen ob wir das Ziel ereicht haben d.h. vor Potenial B stehen mit dem wir verschmelzen können
        for (Potential pot : potentials){
            if(pot.equals(this.potentialB)){ //gefunden
                //baue das ganze ding um
                this.fixEverthing(best); //schmlitz mit B und dann den parrents bis zu A
                return;
            }
        }
        //wir haben das Ziel noch nicht erreicht also müssen wir die blätter erweitern
        for (Potential pot : potentials){
            //wir können nur dann und exakt dann erweitern wenn das neue potenial selbst nicht komplex ist d.h. nicht mehr als ein wire enthällt
            if(!pot.isSignificant()){
                Collection<PIP> pips = pot.getAdjacentPIPs();
                for(PIP apip : pips){
                    PipNode pN = new PipNode(apip,best);
                    leaves.add(pN);
                }
            }
        }

    }


    /**
     * Created by theChaoS on 19.09.2017.
     */
    private class PipNode {
        private PipNode parrent = null;
        private int cost = 0;
    //    private char side;
        private PIP pip;
    //    private Potential potenial;

     //   public PipNode(PIP pip, Potential pipPotenial, PipNode predecessor){
        public PipNode(PIP pip, PipNode predecessor){
            this.pip = pip;
            this.parrent = predecessor;
    //        this.potenial = pipPotenial;
            if(predecessor!=null)
                this.cost = predecessor.getCost()+1;
            else
                this.cost = 1;
    //        this.side = side; //A oder B erstmal
        }

        public int getCost() {
            return cost;
        }

        public PIP getPip() {
            return pip;
        }

    //    public char getSide() {
    //        return side;
    //    }

        public Object getPredecessor() {
            return parrent;
        }

    //    public Potential getPotenial() {
    //        return potenial;
    //    }
    }

    /**
     * Returns a collection of potentials (might only be of cardinality 2) which are adjacent to the given
     * pip (=which would be fused if the pip is set).
     * @param pip
     * @return
     */
    public  Collection<Potential> getAdjacentPotentialsOfPIP( PIP pip){
        HashSet<Potential> set = new HashSet<Potential>();
        for (Potential p : pipPotentialHashMap.values()){
            if (p.getAdjacentPIPs().contains(pip)){
                set.add(p);
            }
            if(!set.contains(p) && p.getAdjacentPIPs().contains(pip)){
                set.add(p);
            }
        }
        return set;
    }

    private class pipNodeComparator implements Comparator<PipNode> {
        @Override
        public int compare(PipNode pn1, PipNode pn2){
            int c1 = pn1.getCost();
            int c2 = pn2.getCost();
            if (c1>c2)
                return 1;
            if (c2>c1)
                return -1;
            return 0;
        }
    }

}




