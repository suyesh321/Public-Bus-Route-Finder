package service;

import model.RouteSegment;

import java.util.*;

/**
 * Core pathfinding logic of the application.
 *
 * Given the full set of route segments (edges between consecutive stops on
 * every bus route in the valley), this service runs Dijkstra's algorithm to
 * find the cheapest (or shortest-distance) way to get from one stop to
 * another, potentially transferring between several different bus routes.
 */
public class RouteFinderService {

    public enum OptimizeBy {
        FARE,
        DISTANCE
    }

    private final Map<Integer, List<RouteSegment>> adjacencyList;

    public RouteFinderService(Map<Integer, List<RouteSegment>> adjacencyList) {
        this.adjacencyList = adjacencyList;
    }

    /**
     * Finds the best journey between two stops.
     *
     * @param startStopId    id of the boarding stop
     * @param destinationStopId id of the destination stop
     * @param optimizeBy     whether to minimize total fare or total distance
     * @return a JourneyResult describing the path, or null if no route exists
     */
    public JourneyResult findBestJourney(int startStopId, int destinationStopId, OptimizeBy optimizeBy) {
        if (startStopId == destinationStopId) {
            return new JourneyResult(Collections.singletonList(startStopId), Collections.emptyList(), 0, 0);
        }

        Map<Integer, Double> bestCost = new HashMap<>();
        Map<Integer, RouteSegment> cameFromEdge = new HashMap<>();
        Map<Integer, Integer> cameFromStop = new HashMap<>();

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));
        bestCost.put(startStopId, 0.0);
        pq.add(new Node(startStopId, 0.0));

        Set<Integer> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (visited.contains(current.stopId)) {
                continue;
            }
            visited.add(current.stopId);

            if (current.stopId == destinationStopId) {
                break;
            }

            List<RouteSegment> edges = adjacencyList.getOrDefault(current.stopId, Collections.emptyList());
            for (RouteSegment edge : edges) {
                int neighbor = edge.getToStopId();
                if (visited.contains(neighbor)) {
                    continue;
                }
                double edgeWeight = (optimizeBy == OptimizeBy.FARE) ? edge.getFareNpr() : edge.getDistanceKm();
                double newCost = current.cost + edgeWeight;

                if (newCost < bestCost.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    bestCost.put(neighbor, newCost);
                    cameFromEdge.put(neighbor, edge);
                    cameFromStop.put(neighbor, current.stopId);
                    pq.add(new Node(neighbor, newCost));
                }
            }
        }

        if (!bestCost.containsKey(destinationStopId)) {
            return null; // no path found
        }

        // Reconstruct the path from destination back to start.
        LinkedList<Integer> stopPath = new LinkedList<>();
        LinkedList<RouteSegment> edgePath = new LinkedList<>();
        int cursor = destinationStopId;
        stopPath.addFirst(cursor);

        while (cursor != startStopId) {
            RouteSegment edge = cameFromEdge.get(cursor);
            edgePath.addFirst(edge);
            cursor = cameFromStop.get(cursor);
            stopPath.addFirst(cursor);
        }

        double totalFare = edgePath.stream().mapToDouble(RouteSegment::getFareNpr).sum();
        double totalDistance = edgePath.stream().mapToDouble(RouteSegment::getDistanceKm).sum();

        return new JourneyResult(stopPath, edgePath, totalFare, totalDistance);
    }

    /**
     * Counts how many distinct bus routes must be boarded to complete the
     * journey (i.e. number of transfers + 1).
     */
    public int countBusChanges(List<RouteSegment> edgePath) {
        Set<Integer> distinctRoutes = new HashSet<>();
        for (RouteSegment edge : edgePath) {
            distinctRoutes.add(edge.getRouteId());
        }
        return distinctRoutes.size();
    }

    private static class Node {
        final int stopId;
        final double cost;

        Node(int stopId, double cost) {
            this.stopId = stopId;
            this.cost = cost;
        }
    }

    /**
     * Result of a pathfinding query: the ordered list of stop ids visited,
     * the edges (bus segments) taken between them, and totals.
     */
    public static class JourneyResult {
        private final List<Integer> stopPath;
        private final List<RouteSegment> segments;
        private final double totalFare;
        private final double totalDistanceKm;

        public JourneyResult(List<Integer> stopPath, List<RouteSegment> segments,
                              double totalFare, double totalDistanceKm) {
            this.stopPath = stopPath;
            this.segments = segments;
            this.totalFare = totalFare;
            this.totalDistanceKm = totalDistanceKm;
        }

        public List<Integer> getStopPath() {
            return stopPath;
        }

        public List<RouteSegment> getSegments() {
            return segments;
        }

        public double getTotalFare() {
            return totalFare;
        }

        public double getTotalDistanceKm() {
            return totalDistanceKm;
        }
    }
}
