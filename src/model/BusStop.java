package model;

/**
 * Represents a single bus stop / station somewhere in the Kathmandu Valley
 * (Kathmandu, Lalitpur or Bhaktapur).
 */
public class BusStop {

    private int stopId;
    private String name;
    private String area;       // e.g. "Kalanki", "Koteshwor"
    private double latitude;
    private double longitude;

    public BusStop() {
    }

    public BusStop(int stopId, String name, String area, double latitude, double longitude) {
        this.stopId = stopId;
        this.name = name;
        this.area = area;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getStopId() {
        return stopId;
    }

    public void setStopId(int stopId) {
        this.stopId = stopId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return name + " (" + area + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusStop)) return false;
        BusStop busStop = (BusStop) o;
        return stopId == busStop.stopId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(stopId);
    }
}
