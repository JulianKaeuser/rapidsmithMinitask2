package ignore;

import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.PIP;
import minitask_2.Potential;

import java.util.*;

/**
 * Created by theChaoS on 18.09.2017.
 */
public class BreitensucheBetweenPIPs {
    private Design design;
    private DesignPotentials designWrapper;
    private Potential potenialA;
    private Potential potenialB;

    private PriorityQueue<pipNode> leaves;



    public BreitensucheBetweenPIPs(DesignPotentials designWrapper, PIP A, PIP B){
        this.design = designWrapper.getDesign();
        this.designWrapper = designWrapper;
        this.potenialA = designWrapper.getPotentialOfPIP(A);
        this.potenialB = designWrapper.getPotentialOfPIP(B);

        //wir sind NICHT connected, würden das gerne aber.
        //1.) Finde alle Pips am rand, wir arbeiten nur noch auf einer seite, damit ist der Name falsch XD
        for (PIP apip:this.potenialA.getAdjacentPIPs()){
            pipNode pN = new pipNode(apip,null);
            this.leaves.add(pN);
        }
     /*   //B
        for (PIP apip:this.potenialA.getAdjacentPIPs()){
            pipNode pN = new pipNode(apip,null, 'B');
            this.leavesA.add(pN);
        } */
    }
    /*
    Geht rekusiv durch die Liste der PipNodes und verbindet PotinalB langsam mit PotenialA
     */
    public void fixEverthing(pipNode lastPipNode){
        if(this.potenialA.equals(this.potenialB))
            return;
        PIP lastPip = lastPipNode.getPip();
        Potential lastPipPotenial = designWrapper.getPotentialOfPIP(lastPipNode.getPip());
        this.potenialB.fuse(lastPipPotenial, lastPip);
        this.fixEverthing(lastPipNode.parrent);
    }

    public void doBreitensuche(){
        pipNode best = leaves.poll(); //der beste Knoten ist der mit dem kleinsten gewicht und ist immer vorne

        Collection<Potential> potentials = designWrapper.getAdjacentPotentialsOfPIP(best.getPip());
        //prüfen ob wir das Ziel ereicht haben d.h. vor Potenial B stehen mit dem wir verschmelzen können
        for (Potential pot : potentials){
            if(pot.equals(this.potenialB)){ //gefunden
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
                    pipNode pN = new pipNode(apip,best);
                    leaves.add(pN);
                }
            }
        }

    }


    /**
     * Created by theChaoS on 19.09.2017.
     */
    private class pipNode{
        private pipNode parrent = null;
        private int cost = 0;
    //    private char side;
        private PIP pip;
    //    private Potential potenial;

     //   public pipNode(PIP pip, Potential pipPotenial, pipNode predecessor){
        public pipNode(PIP pip, pipNode predecessor){
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

    private class pipNodeComparator implements Comparator<pipNode> {
        @Override
        public int compare(pipNode pn1, pipNode pn2){
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




