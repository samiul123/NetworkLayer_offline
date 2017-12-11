/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkLayer;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author samsung
 */
public class Router {
    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddrs;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIds;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state
    public String routingPath;
    public int hop_count;

    public Router() {
        interfaceAddrs = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIds = new ArrayList<>();
        
        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p<=0.80) state = true;
        else state = false;
        
        numberOfInterfaces = 0;
    }
    
    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddrs)
    {
        this.routerId = routerId;
        this.interfaceAddrs = interfaceAddrs;
        this.neighborRouterIds = neighborRouters;
        routingTable = new ArrayList<>();
        routingPath = "";
        hop_count = 0;
        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p<=0.80) state = true;
        else state = false;
        
        numberOfInterfaces = this.interfaceAddrs.size();
    }

    @Override
    public String toString() {
        String temp = "";
        temp+="Router ID: "+routerId+"\n";
        temp+="Intefaces: \n";
        for(int i=0;i<numberOfInterfaces;i++)
        {
            temp+=interfaceAddrs.get(i).getString()+"\t";
        }
        temp+="\n";
        temp+="Neighbors: \n";
        for(int i=0;i<neighborRouterIds.size();i++)
        {
            temp+=neighborRouterIds.get(i)+"\t";
        }
        return temp;
    }
    
    
    
    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable()
    {
        /*routingTable.add(new RoutingTableEntry(routerId, 0, 0));
        for(int i = 0; i < neighborRouterIds.size(); i++){
            routingTable.add(new RoutingTableEntry(neighborRouterIds.get(i), 1, neighborRouterIds.get(i)));
        }*/

        for(Router router: NetworkLayerServer.routers){
            int thisRouterId = router.getRouterId();
            if(thisRouterId == this.routerId){
                routingTable.add(new RoutingTableEntry(routerId, 0, 0));
                continue;
            }
            if(isPresentInNeighbours(thisRouterId) && router.getState()){
                routingTable.add(new RoutingTableEntry(thisRouterId, 1, thisRouterId));
                continue;
            }
            routingTable.add(new RoutingTableEntry(thisRouterId, Constants.INFTY, 0));
        }


        //print every routing table
        System.out.println(printRoutingTable("Initial"));
    }

    public boolean isPresentInNeighbours(int id){
        for(int i = 0; i < neighborRouterIds.size(); i++){
            if(neighborRouterIds.get(i) == id){
                return true;
            }
        }
        return false;
    }

    //print every routing table
    public String printRoutingTable(String initial){
        String routingTableStr = "-----------------\n";
        routingTableStr += initial;
        routingTableStr += " Table for " + routerId + "\n";
        for(int i = 0; i < routingTable.size(); i++){
            routingTableStr += "DEST: " + routingTable.get(i).getRouterId() + " DIST: "
                    + routingTable.get(i).getDistance() +
                    " NEXT: " + routingTable.get(i).getGatewayRouterId() + "\n";
        }
        routingTableStr += "---------------\n";
        return routingTableStr;
    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable()
    {
        routingTable = new ArrayList<>();
    }

    /*public boolean isPresent(int searchableId){
        for(int i = 0; i < routingTable.size(); i++){
            if(routingTable.get(i).getRouterId() == searchableId){
                return true;
            }
        }
        return false;
    }*/

    public double findInitialDistance(int searchableId){
        for (int i = 0; i < routingTable.size(); i++){
            if(routingTable.get(i).getRouterId() == searchableId){
                return routingTable.get(i).getDistance();
            }
        }
        return 0;
    }

    public void updateDistanceAndGateway(int searchableId, double distance, int neighbourToBeNewGateway){
        for(int i = 0; i < routingTable.size(); i++){
            if(routingTable.get(i).getRouterId() == searchableId){
                routingTable.get(i).setDistance(distance);
                routingTable.get(i).setGatewayRouterId(neighbourToBeNewGateway);
            }
        }
    }

    public int gateWayIdinThisRoutingTableForDestinationId(int destinationId){
        for (int i = 0; i < routingTable.size(); i++){
            if(routingTable.get(i).getRouterId() == destinationId){
                return routingTable.get(i).getGatewayRouterId();
            }
        }
        return 0;
    }

    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor 
     */
    public void updateRoutingTable(Router neighbor, int dvrOrSimple) {
        int neighbourId = neighbor.routerId;
        double distanceToNeighbour = 0;
        int gatewayId = 0;
        for(int i = 0; i < routingTable.size(); i++){
            if(routingTable.get(i).getRouterId() == neighbourId){
                distanceToNeighbour = routingTable.get(i).getDistance();
                gatewayId = routingTable.get(i).getGatewayRouterId();
                break;
            }
        }
        for(int i = 0; i < neighbor.routingTable.size(); i++){
            int destinationId = neighbor.routingTable.get(i).getRouterId();
            double distance = neighbor.routingTable.get(i).getDistance();
            int gatewayIdOfNeighbour = neighbor.routingTable.get(i).getGatewayRouterId();
             //check whether initial distance is better or not
            //if not, then replace new distance with initial distance
            if(dvrOrSimple == 0){
                if(gatewayIdOfNeighbour == destinationId ||
                        findInitialDistance(destinationId) > distanceToNeighbour + distance && routerId != gatewayIdOfNeighbour){
                    updateDistanceAndGateway(destinationId, distanceToNeighbour + distance, neighbourId);
                }
                /*if(findInitialDistance(destinationId) > distanceToNeighbour + distance){
                    updateDistanceAndGateway(destinationId, distanceToNeighbour + distance, neighbourId);
                }*/
            }
            else {
                if(findInitialDistance(destinationId) > distanceToNeighbour + distance){
                    updateDistanceAndGateway(destinationId, distanceToNeighbour + distance, neighbourId);
                }
            }
        }
        //System.out.println(printRoutingTable("Updated"));
    }
    
    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState()
    {
        state=!state;
        if(state==true) this.initiateRoutingTable();
        else this.clearRoutingTable();
    }
    
    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddrs() {
        return interfaceAddrs;
    }

    public void setInterfaceAddrs(ArrayList<IPAddress> interfaceAddrs) {
        this.interfaceAddrs = interfaceAddrs;
        numberOfInterfaces = this.interfaceAddrs.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIds() {
        return neighborRouterIds;
    }

    public void setNeighborRouterIds(ArrayList<Integer> neighborRouterIds) {
        this.neighborRouterIds = neighborRouterIds;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }
    
    
}
