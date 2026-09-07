package dao;

import model.BusRoute;
import model.RouteSegment;

import java.util.List;

public interface BusRouteDAO {

    List<BusRoute> getAllRoutes();

    BusRoute getRouteById(int routeId);

    /**
     * Returns every route segment (edge) in the system, used to build the
     * routing graph for RouteFinderService.
     */
    List<RouteSegment> getAllSegments();

    boolean addRoute(BusRoute route);

    boolean addSegment(RouteSegment segment);

    boolean deleteRoute(int routeId);
}
