/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkLayer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author samsung
 */
public class ServerThread implements Runnable {
    private Thread t;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    EndDevice endDevice;
    String routing_path;
    int hop_count;
    int drop_count;
    int isDroppedInSrcOrDest = 0;
    int isDroppedInMiddle = 0;
    String serverAcknowloedgement = "";
    ArrayList<EndDevice> list;
    double avg_hop_from_client,avg_drop_from_client;

    public ServerThread(Socket socket){
        
        this.socket = socket;
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Server Ready for client no: "+NetworkLayerServer.clientCount);
        NetworkLayerServer.clientCount++;
        
        t=new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        /**
         * Synchronize actions with client.
         */
        /*
        Tasks:
        1. Upon receiving a packet server will assign a recipient.
        [Also modify packet to add destination]
        2. call deliverPacket(packet)
        3. If the packet contains "SHOW_ROUTE" request, then fetch the required information
                and send back to client
        4. Either send acknowledgement with number of hops or send failure message back to client
        */

        endDevice = NetworkLayerServer.devConfig;
        list = NetworkLayerServer.clientLists;
        String route_request = "";
        try {
            //output.writeObject(list);
            output.writeObject(endDevice);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true){
            Object o;
            try {
                System.out.println("Server is waiting to listen");
                o = input.readObject();
                Packet p;
                if(!(o instanceof Packet)){
                    System.out.println("Error reading in packet from client");
                    break;
                }
                else {
                    p = (Packet) o;
                    p.setDestinationIP(generateRandomReceiver(p.getSourceIP()));
//                    if(p.getHop_count() >= 0.0 && p.getDrop_count() >= 0.0){
//                        avg_hop_from_client += p.getHop_count();
//                        avg_drop_from_client += p.getDrop_count();
//                        p.setDestinationIP(null);
//                    }
                    //System.out.println("FROM CLIENT ID " + findRouter(endDevice).getRouterId() + ": "+ p.toString());
                }

                if(p.getDestinationIP() != null){
                    if(deliverPacket(p)){
                        serverAcknowloedgement = "PACKET DELIVERED SUCCESSFULLY\nHOP_COUNT: " + hop_count;
                        try {
//                            output.writeObject(serverAcknowloedgement);
//                            output.writeObject(hop_count);
                            Packet successPacket = new Packet(serverAcknowloedgement, hop_count);
                            output.writeObject(successPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(p.getSpecialMessage().equals("SHOW_ROUTE")){
                            route_request += "FOR I = 20\n";
                            route_request += "ROUTING_PATH: "+ routing_path + "\n";
                            route_request += "HOP_COUNT: " + hop_count + "\n";
                            route_request += "ROUTER'S INFO\n";
                            for(Router router: NetworkLayerServer.routers){
                                route_request += router.printRoutingTable("");
                            }
                            try {
                                output.writeObject(route_request);
//                                Packet routeRequestPacket = new Packet(route_request);
//                                output.writeObject(routeRequestPacket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else{
                        try {
                            if(isDroppedInSrcOrDest == 1){
                                isDroppedInSrcOrDest = 0;
                                serverAcknowloedgement = "SRC OR DEST FAILURE\n";
                            }
                            else if(isDroppedInMiddle == 1){
                                isDroppedInMiddle = 0;
                                serverAcknowloedgement = "MIDDLE FAILURE\n";
                            }
//                            output.writeObject(serverAcknowloedgement);
//                            output.writeObject(drop_count);
                            Packet dropPacket = new Packet(serverAcknowloedgement,drop_count);
                            output.writeObject(dropPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            if(list.size() % 4 == 0){
//                double final_avg_hop = (double) avg_hop_from_client / (double)list.size();
//                double final_avg_drop = (double)avg_drop_from_client / (double)list.size();
//                System.out.println("<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>\n");
//                System.out.println("FINAL AVG HOP: " + final_avg_hop);
//                System.out.println("FINAL AVG DROP: " + final_avg_drop);
//                System.out.println("<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>\n");
//            }
        }
        //System.out.println("Size: " + NetworkLayerServer.routers.size());
    }

    private IPAddress generateRandomReceiver(IPAddress sourceIP) {
        Random random = new Random();
        System.out.println(list.size());
        int r =Math.abs(random.nextInt(list.size()));
        System.out.println("value of r: " + r);
        IPAddress ip = null;
//        int index = list.indexOf(ownDevice);
//        while (r == index)r =Math.abs(random.nextInt(list.size()));
//        ip = list.get(r).getIp();
        int index = 0;
        for(EndDevice device: list){
            if(device.getIp().equals(sourceIP)){
                index = list.indexOf(device);
                break;
            }
        }
        while (r == index)r =Math.abs(random.nextInt(list.size()));
        ip = list.get(r).getIp();
        System.out.println("DEST FOR SOURCE IP: " + sourceIP + " is " + ip);
        return ip;
    }

    public Router findRouter(EndDevice device){
        Router sameInterfaceWithSrcDevice = null;
        boolean isFound = false;
        for(int i = 0; i < NetworkLayerServer.routers.size(); i++){
            Router router = NetworkLayerServer.routers.get(i);
            //System.out.println(router.getRouterId()+"\n");
            ArrayList<IPAddress> routerInterfaces = router.getInterfaceAddrs();
            for(int j = 0; j < routerInterfaces.size(); j++){
                //System.out.println("interfaces: " + routerInterfaces.get(j));
                if(routerInterfaces.get(j).toString().equals(device.getGateway().toString())){
                    sameInterfaceWithSrcDevice = router;
                    isFound = true;
                    //System.out.println("1");
                    break;
                }
            }
            if(isFound)break;
        }
        return sameInterfaceWithSrcDevice;
    }

    public Router findRouterSimilarWithDest(IPAddress ip){
        Router sameInterfaceWithDest = null;
        boolean isFound = false;
        IPAddress ipp2 = null;
        //System.out.println("in find dest" + NetworkLayerServer.routers.size());
        for(int k = 0; k < list.size(); k++){
            EndDevice device2 = list.get(k);
            if(device2.getIp().toString().equals(ip.toString())){
                ipp2 = device2.getGateway();
                break;
            }
        }
        for(int i = 0; i < NetworkLayerServer.routers.size(); i++){
            Router router = NetworkLayerServer.routers.get(i);
            ArrayList<IPAddress> routerInterfaces = router.getInterfaceAddrs();
            for(int j = 0; j < routerInterfaces.size(); j++){
                if(routerInterfaces.get(j).toString().equals(ipp2.toString())){
                    sameInterfaceWithDest = router;
                    isFound = true;
                    break;
                }
            }
            if(isFound)break;
        }
        return sameInterfaceWithDest;
    }

    public Router findNextRouterWithNextHopId(int nextHopId){
        Router nextRouter = null;
        for(Router router: NetworkLayerServer.routers){
            if(router.getRouterId() == nextHopId){
                nextRouter = router;
                break;
            }
        }
        return nextRouter;
    }

    /**
     * Returns true if successfully delivered
     * Returns false if packet is dropped
     * @param p
     * @return 
     */
    public Boolean deliverPacket(Packet p){
        /*
        1. Find the router s which has an interface
                such that the interface and source end device have same network address.
        2. Find the router d which has an interface
                such that the interface and destination end device have same network address.
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination, 
                and eventually the packet reaches to destination router d.
                
            3(a) If, while forwarding, any gateway x, found from routingTable of router x is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t
                            
            3(b) If, while forwarding, a router x receives the packet from router y, 
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t
                            
        4. If 3(a) occurs at any stage, packet will be dropped, 
            otherwise successfully sent to the destination router
        */
        Router src = findRouter(endDevice);
        Router originalSrc = findRouter(endDevice);
        //System.out.println(originalSrc.printRoutingTable(""));
        Router dest = findRouterSimilarWithDest(p.getDestinationIP());
        System.out.println("dest: " + p.getDestinationIP());
        System.out.println("src ip: " + src.getRouterId());
        System.out.println("dest ip: " + dest.getRouterId());
        //implementing forwarding
        int success = 0;
        int nextHopId = 0;
        hop_count = 0;
        routing_path = "";
//        routing_path = ""+originalSrc.getRouterId();
        if(!src.getState()){
            isDroppedInSrcOrDest = 1;
            return false;
        }
        while (nextHopId != dest.getRouterId()){
            ArrayList<RoutingTableEntry> routingTableOfSrc = src.getRoutingTable();
//            originalSrc.routingPath += src.getRouterId();
            routing_path += src.getRouterId();
            for(RoutingTableEntry entry: routingTableOfSrc){
                if(entry.getRouterId() == dest.getRouterId()){
                    nextHopId = entry.getGatewayRouterId();
//                    routing_path += nextHopId;
                    System.out.println("Next hop id: " + nextHopId);
                    if(nextHopId == 0){
                        success = 0;
                        serverAcknowloedgement += "FAILURE OCCURRED WHILE SENDING TO " + entry.getRouterId() + " DUE TO NO AVAILABLE GATEWAY\n";
//                        break;
                        drop_count++;
                        isDroppedInMiddle = 1;
                        return false;
                    }
                    else{
                        src = findNextRouterWithNextHopId(nextHopId);//next would-be src router with the nextHopId
                        System.out.println(src.printRoutingTable(""));
                        if(!src.getState()){//if state of this router is false
                            p = null; //dropping packet p
                            entry.setDistance(Constants.INFTY); //updating distance of entry to infinity
                            NetworkLayerServer.stateChanger.t.suspend(); //blocking stateChanger thread
                            //NetworkLayerServer.DVR(src.getRouterId()); //DVR from the faulty router id
                            NetworkLayerServer.simpleDVR(src.getRouterId());
                            NetworkLayerServer.stateChanger.t.resume(); //resuming stateChanger thread
                            success = 0;
                            serverAcknowloedgement += "FAILURE OCCURRED WHILE SENDING TO " + src.getRouterId() + " DUE TO DOWN STATE\n";
//                            break;
                            drop_count++;
                            isDroppedInMiddle = 1;
                            return false;
                        }
                        else{
                            if(entry.getDistance() == Constants.INFTY){
                                entry.setDistance(1);
                                hop_count++;
                                routing_path += " -> " + src.getRouterId();
                                //routing_path = originalSrc.routingPath;
                                NetworkLayerServer.stateChanger.t.suspend();
                                //NetworkLayerServer.DVR(src.getRouterId()); //DVR from the faulty router id
                                NetworkLayerServer.simpleDVR(src.getRouterId());
                                NetworkLayerServer.stateChanger.t.resume(); //resuming stateChanger thread
                                success = 1;
                                //break;
                            }
                            else{
                                hop_count += entry.getDistance();
                                routing_path += " -> " + src.getRouterId();
                            }
                        }
                    }
                    //break;
                }
            }
//            if(success == 0)break;
        }
//        if(success == 0)return false;
//        else return true;
        if(!dest.getState()){
            isDroppedInSrcOrDest = 1;
            return false;
        }
        return true;
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }

}
