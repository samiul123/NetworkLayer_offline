/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkLayer;

import java.io.Serializable;

/**
 *
 * @author samsung
 */
public class Packet implements Serializable{
    private String message;
    private String specialMessage;  //ex: "SHOW_ROUTE" request
    private IPAddress destinationIP;
    private IPAddress sourceIP;
    private int count;
    private double drop_count;
    private double hop_count;
    public Packet(String message){
        this.message = message;
    }

    public Packet(String message, int count){
        this.message = message;
        this.count = count;
        this.drop_count = -1;
        this.hop_count = -1;
    }

    public Packet(String message, String specialMessage, IPAddress sourceIP, IPAddress destinationIP) {
        this.message = message;
        this.specialMessage = specialMessage;
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
        this.hop_count = -1;
        this.drop_count = -1;
    }

    public Packet(double hop_count, double drop_count){
        this.hop_count = hop_count;
        this.drop_count = drop_count;
    }

    public double getDrop_count() {
        return drop_count;
    }

    public void setDrop_count(double drop_count) {
        this.drop_count = drop_count;
    }

    public double getHop_count() {
        return hop_count;
    }

    public void setHop_count(double hop_count) {
        this.hop_count = hop_count;
    }

//    public boolean hasDrop_Hop_count(){
//        if(drop_count >= 0 && hop_count >= 0)return true;
//        else return false;
//    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public IPAddress getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(IPAddress sourceIP) {
        this.sourceIP = sourceIP;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSpecialMessage() {
        return specialMessage;
    }

    public void setSpecialMessage(String specialMessage) {
        this.specialMessage = specialMessage;
    }

    public IPAddress getDestinationIP() {
        return destinationIP;
    }

    public void setDestinationIP(IPAddress destinationIP) {
        this.destinationIP = destinationIP;
    }

    @Override
    public String toString() {
        return "MESSAGE: " + message + " SPECIAL_MSG: " + specialMessage + " SOURCE_IP: " + sourceIP + " DEST_IP: " + destinationIP;
    }
}
