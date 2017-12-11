/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package networkLayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author CSE_BUET
 */
public class NetworkLayerServer {
    static int clientCount = 1;
    static EndDevice devConfig;
    static ArrayList<Router> routers = new ArrayList<>();
    static RouterStateChanger stateChanger = null;
    static ArrayList<EndDevice> clientLists = new ArrayList<>();
    /**
     * Each map entry represents number of client end devices connected to the interface
     */
    static Map<IPAddress,Integer> clientInterfaces = new HashMap<>();
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /**
         * Task: Maintain an active client list
         */
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(1234);
        } catch (IOException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Server Ready: "+serverSocket.getInetAddress().getHostAddress());
        
        System.out.println("Creating router topology");
        
        readTopology();
        printRouters();
        
        /**
         * Initialize routing tables for all routers
         */
        initRoutingTables();
        
        /**
         * Update routing table using distance vector routing until convergence
         */
        //DVR(1);
        simpleDVR(1);
        /**
         * Starts a new thread which turns on/off routers randomly depending on parameter Constants.LAMBDA
         */
        stateChanger = new RouterStateChanger();
        
        while(true){
            try {
                Socket clientSock = serverSocket.accept();
                System.out.println("Client attempted to connect");
                devConfig = getClientDeviceSetup();
                if(!clientLists.contains(devConfig)){
                    clientLists.add(devConfig);
                    new ServerThread(clientSock);
                }


            } catch (IOException ex) {
                Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public static void initRoutingTables()
    {
        for(int i=0;i<routers.size();i++)
        {
            routers.get(i).initiateRoutingTable();
        }
    }


    private static boolean compareArrayList(ArrayList<RoutingTableEntry> before,
                                            ArrayList<RoutingTableEntry> after){
        if(before.size() != after.size())return false;
        for(RoutingTableEntry routingTableEntry: before){
            if(!after.contains(routingTableEntry))return false;
        }
        return true;
    }

    private static Router findInRouters(int neighbourId){
        Router router = null;
        for(int i = 0; i < routers.size(); i++){
            if(routers.get(i).getRouterId() == neighbourId){
                router = routers.get(i);
                break;
            }
        }
        return router;
    }

    private static ArrayList<Router> findActiveRouters(Router router){
        ArrayList<Integer> neighbourIds = router.getNeighborRouterIds();
        ArrayList<Router> activeNeighbours = new ArrayList<>();
        for(int i = 0; i < neighbourIds.size(); i++){
            Router currentRouter = findInRouters(neighbourIds.get(i));
            if(currentRouter.getState()){
                activeNeighbours.add(currentRouter);
            }
        }
        return activeNeighbours;
    }

    private static boolean checkToCheck(ArrayList<Integer> toCheck){
        for(Integer i: toCheck){
            if(i == 1){
                return false;
            }
        }
        return true;
    }

    /**
     * Task: Implement Distance Vector Routing with Split Horizon and Forced Update
     */
    public static void DVR(int startingRouterId)
    {
        /*
        while(convergence)
        {
            //convergence means no change in any routingTable before and after executing the following for loop
            for each router r <starting from the router with routerId = startingRouterId, in any order>
            {
                1. T <- getRoutingTable of the router r
                2. N <- find routers which are the active neighbors of the current router r
                3. Update routingTable of each router t in N using the 
                   routing table of r [Hint: Use t.updateRoutingTable(r)]
            }
        }
        */
        //retrieving initial routing table
        ArrayList<ArrayList<RoutingTableEntry>> beforeForLoop = new ArrayList<>();
        ArrayList<ArrayList<RoutingTableEntry>> afterForLoop = new ArrayList<>();
        ArrayList<Router> activeNeighbours;
        ArrayList<Router> forLoopingOver = new ArrayList<>();
        ArrayList<Integer> toCheck = new ArrayList<>();
        //1st element of forLoopingOver is the router with startingRouterId
        for(int i = 0; i < routers.size(); i++){
            if(routers.get(i).getRouterId() == startingRouterId){
                forLoopingOver.add(routers.get(i));
                break;
            }
        }

        //then inserting other routers followed by the router with startingRouterId
        for(int i = 0; i < routers.size(); i++){
            if(routers.get(i).getRouterId() != startingRouterId){
                forLoopingOver.add(routers.get(i));
            }
        }

        while (true){
            //need to run DVR
            //before update
            for(int i = 0; i < forLoopingOver.size(); i++){
                Router router = forLoopingOver.get(i);
                beforeForLoop.add(router.getRoutingTable());
            }

            for(int i = 0; i < forLoopingOver.size(); i++){
                Router r = forLoopingOver.get(i);
                activeNeighbours = findActiveRouters(r);
                for(int j = 0; j < activeNeighbours.size(); j++){
                    Router router = activeNeighbours.get(j);
                    router.updateRoutingTable(r, 0);
                    for(int k = 0; k < forLoopingOver.size(); k++){
                        if(router.getRouterId() == forLoopingOver.get(k).getRouterId()){
                            forLoopingOver.set(k,router);
                        }
                    }
                }
            }
            //after update
            for(int i = 0; i < forLoopingOver.size(); i++){
                Router router = forLoopingOver.get(i);
                afterForLoop.add(router.getRoutingTable());
            }

            for(int i = 0; i < forLoopingOver.size(); i++){
                ArrayList<RoutingTableEntry> before = beforeForLoop.get(i);
                ArrayList<RoutingTableEntry> after = afterForLoop.get(i);
                boolean isEqual = compareArrayList(before, after);
                if (isEqual) {
                    toCheck.add(1);
                }
                else toCheck.add(0);
            }

            boolean continueDvr = checkToCheck(toCheck);
            if(!continueDvr){
                System.out.println("matched routing tables");
                break;
            }
        }

    }
    
    /**
     * Task: Implement Distance Vector Routing without Split Horizon and Forced Update
     */
    public static void simpleDVR(int startingRouterId)
    {
        ArrayList<ArrayList<RoutingTableEntry>> beforeForLoop = new ArrayList<>();
        ArrayList<ArrayList<RoutingTableEntry>> afterForLoop = new ArrayList<>();
        ArrayList<Router> activeNeighbours;
        ArrayList<Router> forLoopingOver = new ArrayList<>();
        ArrayList<Integer> toCheck = new ArrayList<>();
        //1st element of forLoopingOver is the router with startingRouterId
        for(int i = 0; i < routers.size(); i++){
            if(routers.get(i).getRouterId() == startingRouterId){
                forLoopingOver.add(routers.get(i));
                break;
            }
        }

        //then inserting other routers followed by the router with startingRouterId
        for(int i = 0; i < routers.size(); i++){
            if(routers.get(i).getRouterId() != startingRouterId){
                forLoopingOver.add(routers.get(i));
            }
        }

        while (true){
            //need to run DVR
            //before update
            for(int i = 0; i < forLoopingOver.size(); i++){
                Router router = forLoopingOver.get(i);
                beforeForLoop.add(router.getRoutingTable());
            }

            for(int i = 0; i < forLoopingOver.size(); i++){
                Router r = forLoopingOver.get(i);
                activeNeighbours = findActiveRouters(r);
                for(int j = 0; j < activeNeighbours.size(); j++){
                    Router router = activeNeighbours.get(j);
                    router.updateRoutingTable(r, 1);
                    for(int k = 0; k < forLoopingOver.size(); k++){
                        if(router.getRouterId() == forLoopingOver.get(k).getRouterId()){
                            forLoopingOver.set(k,router);
                        }
                    }
                }
            }
            //after update
            for(int i = 0; i < forLoopingOver.size(); i++){
                Router router = forLoopingOver.get(i);
                afterForLoop.add(router.getRoutingTable());
            }

            for(int i = 0; i < forLoopingOver.size(); i++){
                ArrayList<RoutingTableEntry> before = beforeForLoop.get(i);
                ArrayList<RoutingTableEntry> after = afterForLoop.get(i);
                boolean isEqual = compareArrayList(before, after);
                if (isEqual) {
                    toCheck.add(1);
                }
                else toCheck.add(0);
            }

            boolean continueDvr = checkToCheck(toCheck);
            if(!continueDvr){
                System.out.println("matched routing tables");
                break;
            }
        }

    }
    
    
    public static EndDevice getClientDeviceSetup()
    {
        Random random = new Random();
        int r =Math.abs(random.nextInt(clientInterfaces.size()));
        
        System.out.println("Size: "+clientInterfaces.size()+"\n"+r);
        
        IPAddress ip=null;
        IPAddress gateway=null;
        
        int i=0;
        for (Map.Entry<IPAddress, Integer> entry : clientInterfaces.entrySet()) {
            IPAddress key = entry.getKey();
            Integer value = entry.getValue();
            if(i==r)
            {
                gateway = key;
                ip = new IPAddress(gateway.getBytes()[0]+"."+gateway.getBytes()[1]+"."+gateway.getBytes()[2]+"."+(value+2));
                value++;
                clientInterfaces.put(key, value);
                break;
            }
            i++;
        }
        
        EndDevice device = new EndDevice(ip, gateway);
        System.out.println("Device : "+ip+"::::"+gateway);
        return device;
    }
    
    public static void printRouters()
    {
        for(int i=0;i<routers.size();i++)
        {
            System.out.println("------------------\n"+routers.get(i));
        }
    }
    
    public static void readTopology()
    {
        Scanner inputFile = null;
        try {
            inputFile = new Scanner(new File("topologyNew.txt"));
            //skip first 27 lines
            int skipLines = 27;
            for(int i=0;i<skipLines;i++)
            {
                inputFile.nextLine();
            }
            
            //start reading contents
            while(inputFile.hasNext())
            {
                inputFile.nextLine();
                int routerId;
                ArrayList<Integer> neighborRouters = new ArrayList<>();
                ArrayList<IPAddress> interfaceAddrs = new ArrayList<>();
                
                routerId = inputFile.nextInt();
                
                int count = inputFile.nextInt();
                for(int i=0;i<count;i++)
                {
                    neighborRouters.add(inputFile.nextInt());
                }
                count = inputFile.nextInt();
                inputFile.nextLine();
                
                for(int i=0;i<count;i++)
                {
                    String s = inputFile.nextLine();
                    //System.out.println(s);
                    IPAddress ip = new IPAddress(s);
                    interfaceAddrs.add(ip);
                    
                    /**
                     * First interface is always client interface
                     */
                    if(i==0)
                    {
                        //client interface is not connected to any end device yet
                        clientInterfaces.put(ip, 0);
                    }
                }
                Router router = new Router(routerId, neighborRouters, interfaceAddrs);
                routers.add(router);
            }
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
