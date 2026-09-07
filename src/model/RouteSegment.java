package model;

/**
 * A directed edge between two consecutive stops that belong to the same
 * bus route. This is the unit the pathfinding graph is built from.
 */
public class RouteSegment {

    private int routeId;
    private String routeName;
    private int fromStopId;
    private int toStopId;
    private double distanceKm;
    private double fareNpr;
    private int sequenceOrder;

    public RouteSegment() {
    }

    public RouteSegment(int routeId, String routeName, int fromStopId, int toStopId,
                         double distanceKm, double fareNpr, int sequenceOrder) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.fromStopId = fromStopId;
        this.toStopId = toStopId;
        this.distanceKm = distanceKm;
        this.fareNpr = fareNpr;
        this.sequenceOrder = sequenceOrder;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public int getFromStopId() {
        return fromStopId;
    }

    public void setFromStopId(int fromStopId) {
        this.fromStopId = fromStopId;
    }

    public int getToStopId() {
        return toStopId;
    }

    public void setToStopId(int toStopId) {
        this.toStopId = toStopId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getFareNpr() {
        return fareNpr;
    }

    public void setFareNpr(double fareNpr) {
        this.fareNpr = fareNpr;
    }

    public int getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(int sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }
}
