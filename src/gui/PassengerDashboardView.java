package gui;

import dao.BusRouteDAO;
import dao.BusRouteDAOImpl;
import dao.BusStopDAO;
import dao.BusStopDAOImpl;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.BusStop;
import model.RouteSegment;
import model.User;
import service.FareCalculatorService;
import service.RouteFinderService;
import util.GraphBuilder;

import java.util.List;
import java.util.Map;

/**
 * Main screen for passengers (and guests): pick a starting stop and a
 * destination stop, choose whether to optimize for cheapest fare or
 * shortest distance, and see the recommended journey step by step.
 */
public class PassengerDashboardView {

    private final Stage stage;
    private final User user;

    private final BusStopDAO stopDAO = new BusStopDAOImpl();
    private final BusRouteDAO routeDAO = new BusRouteDAOImpl();
    private final FareCalculatorService fareCalculator = new FareCalculatorService();

    private ListView<String> resultsList;
    private Label summaryLabel;

    public PassengerDashboardView(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public void show() {
        Label heading = new Label("Hi " + user.getFullName() + ", where are you headed?");
        heading.getStyleClass().add("title-label");

        List<BusStop> allStops = stopDAO.getAllStops();
        ObservableStopChoice fromChoice = new ObservableStopChoice(allStops);
        ObservableStopChoice toChoice = new ObservableStopChoice(allStops);

        ToggleGroup optimizeGroup = new ToggleGroup();
        RadioButton cheapestBtn = new RadioButton("Cheapest fare");
        RadioButton shortestBtn = new RadioButton("Shortest distance");
        cheapestBtn.setToggleGroup(optimizeGroup);
        shortestBtn.setToggleGroup(optimizeGroup);
        cheapestBtn.setSelected(true);

        Button findBtn = new Button("Find Best Route");
        Button swapBtn = new Button("\u21c4 Swap");
        Button logoutBtn = new Button("Log Out");

        summaryLabel = new Label();
        summaryLabel.getStyleClass().add("status-label");

        resultsList = new ListView<>();
        resultsList.setPrefHeight(260);

        swapBtn.setOnAction(e -> {
            BusStop from = fromChoice.getBox().getValue();
            BusStop to = toChoice.getBox().getValue();
            fromChoice.getBox().setValue(to);
            toChoice.getBox().setValue(from);
        });

        findBtn.setOnAction(e -> {
            BusStop from = fromChoice.getBox().getValue();
            BusStop to = toChoice.getBox().getValue();

            if (from == null || to == null) {
                summaryLabel.setText("Please choose both a starting stop and a destination.");
                resultsList.getItems().clear();
                return;
            }
            if (from.getStopId() == to.getStopId()) {
                summaryLabel.setText("You are already there!");
                resultsList.getItems().clear();
                return;
            }

            RouteFinderService.OptimizeBy optimizeBy = cheapestBtn.isSelected()
                    ? RouteFinderService.OptimizeBy.FARE
                    : RouteFinderService.OptimizeBy.DISTANCE;

            runSearch(from, to, optimizeBy);
        });

        logoutBtn.setOnAction(e -> new LoginView(stage).show());

        HBox searchRow = new HBox(10,
                new Label("From:"), fromChoice.getBox(),
                swapBtn,
                new Label("To:"), toChoice.getBox());
        searchRow.setAlignment(Pos.CENTER_LEFT);

        HBox optionsRow = new HBox(20, cheapestBtn, shortestBtn, findBtn);
        optionsRow.setAlignment(Pos.CENTER_LEFT);

        HBox topBar = new HBox(heading);
        HBox.setHgrow(heading, javafx.scene.layout.Priority.ALWAYS);
        HBox topBarWithLogout = new HBox(20, heading, logoutBtn);
        topBarWithLogout.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(16, topBarWithLogout, searchRow, optionsRow, summaryLabel, resultsList);
        root.setPadding(new Insets(24));

        Scene scene = new Scene(root, 760, 560);
        stage.setTitle("Public Bus Route Finder - Kathmandu Valley - Passenger");
        stage.setScene(scene);
        stage.show();
    }

    private void runSearch(BusStop from, BusStop to, RouteFinderService.OptimizeBy optimizeBy) {
        List<RouteSegment> allSegments = routeDAO.getAllSegments();
        Map<Integer, List<RouteSegment>> graph = GraphBuilder.buildAdjacencyList(allSegments);
        RouteFinderService finder = new RouteFinderService(graph);

        RouteFinderService.JourneyResult result = finder.findBestJourney(from.getStopId(), to.getStopId(), optimizeBy);

        resultsList.getItems().clear();

        if (result == null) {
            summaryLabel.setText("No route found between " + from.getName() + " and " + to.getName()
                    + ". Try different stops.");
            return;
        }

        int busChanges = finder.countBusChanges(result.getSegments());
        summaryLabel.setText(String.format("Total fare: %s   |   Total distance: %s   |   Buses to board: %d",
                fareCalculator.formatFare(result.getTotalFare()),
                fareCalculator.formatDistance(result.getTotalDistanceKm()),
                busChanges));

        String currentRouteName = null;
        for (RouteSegment segment : result.getSegments()) {
            BusStop fromStop = stopDAO.getStopById(segment.getFromStopId());
            BusStop toStop = stopDAO.getStopById(segment.getToStopId());

            if (!segment.getRouteName().equals(currentRouteName)) {
                resultsList.getItems().add("\u25B6 Board \"" + segment.getRouteName() + "\" at "
                        + (fromStop != null ? fromStop.getName() : segment.getFromStopId()));
                currentRouteName = segment.getRouteName();
            }

            resultsList.getItems().add(String.format("   %s \u2192 %s  (%s, %s)",
                    fromStop != null ? fromStop.getName() : segment.getFromStopId(),
                    toStop != null ? toStop.getName() : segment.getToStopId(),
                    fareCalculator.formatDistance(segment.getDistanceKm()),
                    fareCalculator.formatFare(segment.getFareNpr())));
        }
        resultsList.getItems().add("\u2691 Alight at " + to.getName());
    }

    /** Small wrapper around a ComboBox<BusStop> with a searchable stop list. */
    private static class ObservableStopChoice {
        private final ComboBox<BusStop> box;

        ObservableStopChoice(List<BusStop> stops) {
            box = new ComboBox<>(FXCollections.observableArrayList(stops));
            box.setEditable(false);
            box.setPrefWidth(220);
        }

        ComboBox<BusStop> getBox() {
            return box;
        }
    }
}
