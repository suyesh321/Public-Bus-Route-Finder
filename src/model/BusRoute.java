package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a full bus route (e.g. "Ratnapark - Koteshwor - Airport")
 * made up of an ordered list of stops.
 */
public class BusRoute {

    private int routeId;
    private String routeName;      // e.g. "Route 5: Ratnapark - Koteshwor"
    private String operatorName;   // e.g. "Sajha Yatayat"
    private double farePerKm;      // NPR per km, used for fare calculation
    private List<BusStop> stops = new ArrayList<>();

    public BusRoute() {
    }

    public BusRoute(int routeId, String routeName, String operatorName, double farePerKm) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.operatorName = operatorName;
        this.farePerKm = farePerKm;
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

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public double getFarePerKm() {
        return farePerKm;
    }

    public void setFarePerKm(double farePerKm) {
        this.farePerKm = farePerKm;
    }

    public List<BusStop> getStops() {
        return stops;
    }

    public void setStops(List<BusStop> stops) {
        this.stops = stops;
    }

    public void addStop(BusStop stop) {
        this.stops.add(stop);
    }

    @Override
    public String toString() {
        return routeName + " (" + operatorName + ")";
    }
}
