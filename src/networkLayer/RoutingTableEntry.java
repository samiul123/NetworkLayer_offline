/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkLayer;

/**
 *
 * @author samsung
 */
public class RoutingTableEntry {
    private int routerId;
    private double distance;
    private int gatewayRouterId;

    public RoutingTableEntry(int routerId, double distance, int gatewayRouterId) {
        this.routerId = routerId;
        this.distance = distance;
        this.gatewayRouterId = gatewayRouterId;
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getGatewayRouterId() {
        return gatewayRouterId;
    }

    public void setGatewayRouterId(int gatewayRouterId) {
        this.gatewayRouterId = gatewayRouterId;
    }

    @Override
    public int hashCode() {
        return routerId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        RoutingTableEntry rt = (RoutingTableEntry) obj;
        if(this.routerId == rt.routerId && this.distance == rt.distance &&
                this.gatewayRouterId == rt.gatewayRouterId)return true;
        return false;
    }
}
