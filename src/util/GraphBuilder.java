package util;

import model.RouteSegment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns the flat list of RouteSegments (edges pulled from the database)
 * into an adjacency list keyed by stop id, ready to be consumed by
 * service.RouteFinderService (Dijkstra's algorithm).
 *
 * Because a single road between two stops can be served by more than one
 * bus route, a stop pair may have several parallel edges - each one kept
 * separately so the route finder can tell the user which bus to board.
 */
public class GraphBuilder {

    public static Map<Integer, List<RouteSegment>> buildAdjacencyList(List<RouteSegment> segments) {
        Map<Integer, List<RouteSegment>> graph = new HashMap<>();

        for (RouteSegment segment : segments) {
            graph.computeIfAbsent(segment.getFromStopId(), k -> new ArrayList<>()).add(segment);

            // Most municipal bus routes in the valley run both directions on the
            // same road, so we also add the reverse edge with the same weight.
            RouteSegment reverse = new RouteSegment(
                    segment.getRouteId(),
                    segment.getRouteName(),
                    segment.getToStopId(),
                    segment.getFromStopId(),
                    segment.getDistanceKm(),
                    segment.getFareNpr(),
                    segment.getSequenceOrder()
            );
            graph.computeIfAbsent(reverse.getFromStopId(), k -> new ArrayList<>()).add(reverse);
        }

        return graph;
    }
}
