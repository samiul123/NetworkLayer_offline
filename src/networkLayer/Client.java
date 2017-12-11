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
 * Created by samiu on 22-Nov-17.
 */
public class Client {
    static ArrayList<EndDevice> list;
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket;
        ObjectInputStream input;
        ObjectOutputStream output;
        Integer sum_of_hops = 0;
        Integer sum_of_drops = 0;
        /*try {
            socket = new Socket("localhost", 1234);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        socket = new Socket("localhost", 1234);
        input = new ObjectInputStream(socket.getInputStream());
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();

        System.out.println("Connected to server");
        /**
         * Tasks
         */
        /*
        1. Receive EndDevice configuration from server
        2. [Adjustment in NetworkLayerServer.java: Server internally
            handles a list of active clients.]
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the messageto server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
        */

        IPAddress ip = null;
        int i = 0;
        int drop_flag = 0;
        int hop_flag = 0;
        int noOfSuccessfulMsg = 0;
        int noOfDroppedInSrcOrDest = 0;
        int noOfDroppedinMiddle = 0;
        while(true){
            i++;

            Object o = input.readObject();
            EndDevice device = null;
            IPAddress gateWay;
            if(o instanceof EndDevice){
                device = (EndDevice) o;
                ip = device.getIp();
                gateWay = device.getGateway();
                System.out.println("Received endDevice configuration");
                System.out.println("Device : "+device.getIp()+"::::"+device.getGateway());
            }
            else if(o instanceof String){
                System.out.println("FROM SERVER: " + o);
//                if(((String)o).substring(0,16).equals("PACKET DELIVERED")){
//                    hop_flag = 1;
//                    continue;
//                }
//                else if(((String)o).substring(0,7).equals("FAILURE")){
//                    drop_flag = 1;
//                    continue;
//                }
            }
            else if(o instanceof Packet){
                String msg = ((Packet) o).getMessage();
                if(msg.substring(0,6).equals("PACKET")){
                    System.out.println("FROM SERVER: " + msg);
                    sum_of_hops += ((Packet) o).getCount();
                    noOfSuccessfulMsg++;
                }
                else if(msg.substring(0,7).equals("FAILURE")){
                    System.out.println("FROM SERVER: " + msg);
                    sum_of_drops += ((Packet) o).getCount();
                }
                else if(msg.substring(0,3).equals("SRC")){
                    System.out.println("FROM SERVER: " + msg);
                    noOfDroppedInSrcOrDest++;
                }
                else if(msg.substring(0, 6).equals("MIDDLE")){
                    System.out.println("FROM SERVER: " + msg);
                    noOfDroppedinMiddle++;
                }
            }
            else if(o instanceof ArrayList){
                list = (ArrayList<EndDevice>) o;
                continue;
            }
//            else if(o instanceof Integer){
//                if(hop_flag == 1){
//                    sum_of_hops += (Integer) o;
//                    hop_flag = 0;
//                }
//                else if(drop_flag == 1){
//                    sum_of_drops += (Integer)o;
//                    drop_flag = 0;
//                }
//            }
            if(i == 101){
//                double averageHops = (double)sum_of_hops / (double)(i - 1);
//                System.out.println("AVERAGE_HOPS: " + averageHops);
//                double averageDrops = (double)sum_of_drops / (double)(i - 1);
//                System.out.println("AVERAGE_DROPS: " + averageDrops);
//                Packet avg_counts = new Packet(averageHops, averageDrops);
//                output.writeObject(avg_counts);
//                continue;
                break;
            }
            System.out.println("I: " + i);
            String message = "";
            IPAddress receiver = null;
            message = generateRandomMessage();
            //receiver = generateRandomReceiver(device);
            if (i == 20) {
                Packet p = new Packet(message, "SHOW_ROUTE", ip, receiver);
                System.out.println(p.toString());
                output.writeObject(p);
            } else {
                Packet p = new Packet(message, "", ip, receiver);
                System.out.println(p.toString());
                output.writeObject(p);
            }
        }
        double averageHops = (double)sum_of_hops / (double)noOfSuccessfulMsg;
        System.out.println("SUM_HOPS: " + sum_of_hops + " NO OF SUCCESSFUL: " + noOfSuccessfulMsg);
        System.out.println("AVERAGE_HOPS: " + averageHops);
        double averageDrops = (double)noOfDroppedinMiddle / (double)(100 - noOfDroppedInSrcOrDest);
        System.out.println("DROPPED IN MIDDLE: " + noOfDroppedinMiddle + " DROPPED IN SRC_OR_DEST: " + noOfDroppedInSrcOrDest);
        System.out.println("AVERAGE_DROPS: " + averageDrops);
//        Packet avg_counts = new Packet(averageHops, averageDrops);
//        output.writeObject(avg_counts);
        while (true);
    }


    public static String generateRandomMessage(){
        String[] arr = {"Hi", "How are you?", "Nice to meet you", "Are you free today?"};
        Random random = new Random();
        int select = random.nextInt(arr.length);
        return arr[select];
    }
}

