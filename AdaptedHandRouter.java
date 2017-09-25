package minitask_2;

import edu.byu.ece.rapidSmith.design.Design;
import edu.byu.ece.rapidSmith.design.Net;
import edu.byu.ece.rapidSmith.design.PIP;
import edu.byu.ece.rapidSmith.design.Pin;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.WireConnection;
import edu.byu.ece.rapidSmith.device.WireEnumerator;
import edu.byu.ece.rapidSmith.router.Node;
import edu.byu.ece.rapidSmith.util.FileConverter;
import edu.byu.ece.rapidSmith.util.MessageGenerator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Basically a copy of the handrouter with some adjustments for our debugging needs
 * Created by Julian Käuser on 23.09.17.
 */
public class AdaptedHandRouter {




        /** This is the current device of the design that was loaded */
        private Device dev;
        /** This is the corresponding wire enumerator for the device */
        private WireEnumerator we;
        /** This is the current design we are loading */
        private Design design;
        /** Standard Input */
        private BufferedReader br;

        // the net to route
        private Net net;

        /**
         * Initialize the AdaptedHandRouter with the design
         */
        public AdaptedHandRouter(Design design, Net net){
            this.design = design;

            dev = design.getDevice();
            we = design.getWireEnumerator();
            this.net = net;

        }

        /**
         * Prompt the user with options to perform routing of the given net.

         */
        private void HandRoute(){

            int choice;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            ArrayList<PIP> path =  new ArrayList<PIP>();
            ArrayList<PIP> pipList = new ArrayList<PIP>();

            Node currNode = null;
            WireConnection currWire;
            WireConnection[] wiresList = null;
            ArrayList<Node> choices;

            // This keeps track of all the possible starting points (or sources) that
            // we can use to route this net.
            ArrayList<Node> sources = new ArrayList<Node>();

            // Add the original source from the net
            sources.add(new Node(net.getSourceTile(), // Add the tile of the source
                    dev.getPrimitiveExternalPin(net.getSource()), // wire on the source
                    null, // This is used for retracing the route once it is completed
                    0)); // Number of switches needed to reach this point in the route

            // In this loop we'll route each sink pin of the net separately
            for(Pin sinkPin : net.getPins()){
                if(sinkPin.isOutPin()) continue; // Don't try to route the source to the source
                boolean start = true;
                boolean finishedRoute = false;

                // Here is where we create the current sink that we intend to target in this
                //routing iteration.
                Node sink = new Node(sinkPin.getTile(), // A node is a specific tile and wire pair
                        dev.getPrimitiveExternalPin(sinkPin), // we need the external pin
                        // name of the primitive pin
                        // (ex: F1_PINWIRE0 vs. F1)
                        null,								  // This is a parent node
                        0);									  // This value is used to keep track
                // length in hops of the route.

                MessageGenerator.printHeader("Current Sink: " + sink.getTile() +
                        " " + we.getWireName(sink.getWire()));

                // Almost all routes must pass through a particular switch matrix and wire to arrive
                // at the particular sink.  Here we obtain that information to help us target the routing.
                Node switchMatrixSink = sink.getSwitchBoxSink(dev);
                System.out.println("** Sink must pass through switch matrix: " + switchMatrixSink.getTile() +
                        ", wire: " + we.getWireName(switchMatrixSink.getWire())+ " **");

                while(!finishedRoute){

                    // Here we prompt the user to choose a source to start the route from.  If this
                    // is the first tile we are routing in this net there will only be one choice.
                    if(start){
                        start = false;
                        System.out.println("Sources:");
                        for(int i=0; i < sources.size(); i++){
                            Node src = sources.get(i);
                            System.out.println("  " + i+". " + src.getTile() + " " + src.getWire());
                        }
                        System.out.print("Choose a source from the list above: ");
                        try {
                            choice = Integer.parseInt(br.readLine());
                        } catch (Exception e){
                            System.out.println("Error, could not get choice, defaulting to 0");
                            choice = 0;
                        }

                        // Once we get the user's choice, we can determine what wires the source
                        // can connect to by calling Tile.getWireConnections(int wire)

                        currNode = sources.get(choice);
                        wiresList = currNode.getTile().getWireConnections(currNode.getWire());
                        if(wiresList == null || wiresList.length == 0){
                            // We'll have to choose something else, this source had no other connections.
                            System.out.println("Wire had no connections");
                            continue;
                        }
                    }

                    // Print out some information about the sink we are targeting
                    if(sink.getTile().getSinks().get(sink.getWire()).switchMatrixSinkWire == -1){
                        System.out.println("\n\nSINK: "
                                + sink.getTile().getName()
                                + " "
                                + sink.getWire());
                    }
                    else{
                        System.out.println("\n\nSINK: "
                                + sink.getTile().getName()
                                + " "
                                + sink.getWire()
                                + " thru("
                                + switchMatrixSink.getTile() + " "
                                + sink.getTile().getSinks().get(sink.getWire()).switchMatrixSinkWire + ")");
                    }

                    // Print out a part of the corresponding PIP that we have chosen
                    System.out.println("  pip " + currNode.getTile().getName() + " "
                            + currNode.getWire() + " -> ");

                    // Check if we have reached the sink node
                    if (sink.getTile().equals(currNode.getTile())
                            && sink.getWire() == currNode.getWire()){
                        System.out.println("You completed the route!");
                        // If we have, let's print out all the PIPs we used
                        for (PIP pip : path){
                            System.out.print(pip.toString(we));
                            pipList.add(pip);
                            finishedRoute = true;
                        }
                    }
                    if(!finishedRoute){
                        // We didn't find the sink yet, let's print out the set of
                        // choices we can follow given our current wire
                        choices = new ArrayList<Node>();
                        if(wiresList == null || wiresList.length == 0){
                            // We'll have to choose something else, this source had no other connections.
                            System.out.println("Wire had no connections");
                            return;
                        }
                        for (int i = 0; i < wiresList.length; i++) {
                            currWire = wiresList[i];
                            choices.add(new Node(currWire.getTile(currNode.getTile()),
                                    currWire.getWire(), currNode, currNode.getLevel() + 1));

                            System.out.println("    " + i + ". "
                                    + currWire.getTile(currNode.getTile()).getName()
                                    + " " + currWire.getWire() + " "
                                    + choices.get(i).getCost() + " " + choices.get(i).getLevel());
                        }

                        choice = -1;
                        while(choice<0 || choice >wiresList.length) {
                            System.out.print("\nChoose a route (s to start over): ");
                            try {
                                String cmd = br.readLine();
                                if (cmd.equals("s")) {
                                    start = true;
                                    continue;
                                }
                                choice = Integer.parseInt(cmd);

                            } catch (Exception e) {
                                System.out.println("Error reading response, try again.");
                                continue;
                            }
                        }
                        if(wiresList[choice].isPIP()){
                            path.add(new PIP(currNode.getTile(), currNode.getWire(), wiresList[choice].getWire()));
                        }

                        currNode = choices.get(choice);
                        wiresList = currNode.getTile().getWireConnections(currNode.getWire());

                        System.out.println("PIPs so far: ");
                        for (PIP p : path){
                            System.out.print("  " + p.toString(we));
                        }
                    }
                }
            }
            // Apply the PIPs we have choosen to the net
            net.setPIPs(pipList);
        }

        /**
         * Saves the design to a file.
         * @param outputFileName
         */
        private void saveDesign(String outputFileName){
            if(outputFileName.toLowerCase().endsWith("ncd")){
                String xdlFileName = outputFileName+"temp.xdl";
                design.saveXDLFile(xdlFileName, true, true);
                FileConverter.convertXDL2NCD(xdlFileName, outputFileName);

                // Delete the temporary XDL file, if needed
                //FileTools.deleteFile(xdlFileName);
            }
            else{
                design.saveXDLFile(outputFileName, true, true);
            }
        }

    public static void routeHand(Design design, Net net){
        AdaptedHandRouter hr = new AdaptedHandRouter(design, net);

        String nl = System.getProperty("line.separator");
        MessageGenerator.printHeader("OurHand Router Example");


        boolean continueRouting = true;
        hr.br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(">> ");
        System.out.println("Routing net: " + net.getName());
        hr.HandRoute();

    }
}






