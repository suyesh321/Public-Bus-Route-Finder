package dao;

import model.BusStop;

import java.util.List;

public interface BusStopDAO {

    List<BusStop> getAllStops();

    BusStop getStopById(int stopId);

    List<BusStop> searchStopsByName(String keyword);

    boolean addStop(BusStop stop);

    boolean updateStop(BusStop stop);

    boolean deleteStop(int stopId);
}
