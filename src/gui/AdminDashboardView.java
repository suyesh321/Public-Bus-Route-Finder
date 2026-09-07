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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.BusRoute;
import model.BusStop;
import model.RouteSegment;
import model.User;

import java.util.List;

/**
 * Admin console: add new bus stops, create routes, and stitch stops
 * together into ordered route segments (the edges the passenger-facing
 * route finder searches over).
 */
public class AdminDashboardView {

    private final Stage stage;
    private final User admin;

    private final BusStopDAO stopDAO = new BusStopDAOImpl();
    private final BusRouteDAO routeDAO = new BusRouteDAOImpl();

    private TableView<BusStop> stopTable;
    private TableView<BusRoute> routeTable;
    private Label statusLabel;

    public AdminDashboardView(Stage stage, User admin) {
        this.stage = stage;
        this.admin = admin;
    }

    public void show() {
        Label heading = new Label("Admin Console \u2014 " + admin.getFullName());
        heading.getStyleClass().add("title-label");

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setOnAction(e -> new LoginView(stage).show());

        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");

        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Bus Stops", buildStopsTab()));
        tabs.getTabs().add(new Tab("Bus Routes", buildRoutesTab()));
        tabs.getTabs().add(new Tab("Route Segments", buildSegmentsTab()));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        HBox topBar = new HBox(20, heading, logoutBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(14, topBar, statusLabel, tabs);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 860, 620);
        stage.setTitle("Public Bus Route Finder - Kathmandu Valley - Admin");
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildStopsTab() {
        stopTable = new TableView<>();
        TableColumn<BusStop, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getStopId()));
        TableColumn<BusStop, String> nameCol = new TableColumn<>("Stop Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<BusStop, String> areaCol = new TableColumn<>("Area");
        areaCol.setCellValueFactory(new PropertyValueFactory<>("area"));
        stopTable.getColumns().addAll(idCol, nameCol, areaCol);
        refreshStops();

        TextField nameField = new TextField();
        nameField.setPromptText("Stop name, e.g. Koteshwor Chowk");
        TextField areaField = new TextField();
        areaField.setPromptText("Area, e.g. Koteshwor");
        TextField latField = new TextField();
        latField.setPromptText("Latitude (optional)");
        TextField lonField = new TextField();
        lonField.setPromptText("Longitude (optional)");

        Button addBtn = new Button("Add Stop");
        addBtn.setOnAction(e -> {
            if (nameField.getText().isBlank() || areaField.getText().isBlank()) {
                statusLabel.setText("Stop name and area are required.");
                return;
            }
            double lat = parseOrZero(latField.getText());
            double lon = parseOrZero(lonField.getText());
            BusStop stop = new BusStop(0, nameField.getText().trim(), areaField.getText().trim(), lat, lon);
            boolean ok = stopDAO.addStop(stop);
            statusLabel.setText(ok ? "Stop added." : "Failed to add stop.");
            if (ok) {
                nameField.clear();
                areaField.clear();
                latField.clear();
                lonField.clear();
                refreshStops();
            }
        });

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setOnAction(e -> {
            BusStop selected = stopTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLabel.setText("Select a stop to delete.");
                return;
            }
            boolean ok = stopDAO.deleteStop(selected.getStopId());
            statusLabel.setText(ok ? "Stop deleted." : "Could not delete stop (it may be used by a route).");
            refreshStops();
        });

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.addRow(0, new Label("Name:"), nameField, new Label("Area:"), areaField);
        form.addRow(1, new Label("Lat:"), latField, new Label("Lon:"), lonField);

        HBox buttons = new HBox(10, addBtn, deleteBtn);

        VBox box = new VBox(12, stopTable, form, buttons);
        box.setPadding(new Insets(12));
        return box;
    }

    private VBox buildRoutesTab() {
        routeTable = new TableView<>();
        TableColumn<BusRoute, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getRouteId()));
        TableColumn<BusRoute, String> nameCol = new TableColumn<>("Route Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("routeName"));
        TableColumn<BusRoute, String> operatorCol = new TableColumn<>("Operator");
        operatorCol.setCellValueFactory(new PropertyValueFactory<>("operatorName"));
        TableColumn<BusRoute, Number> fareCol = new TableColumn<>("NPR/km");
        fareCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getFarePerKm()));
        routeTable.getColumns().addAll(idCol, nameCol, operatorCol, fareCol);
        refreshRoutes();

        TextField nameField = new TextField();
        nameField.setPromptText("Route name, e.g. Route 5: Ratnapark - Airport");
        TextField operatorField = new TextField();
        operatorField.setPromptText("Operator, e.g. Sajha Yatayat");
        TextField rateField = new TextField();
        rateField.setPromptText("Fare per km (NPR)");

        Button addBtn = new Button("Add Route");
        addBtn.setOnAction(e -> {
            if (nameField.getText().isBlank() || operatorField.getText().isBlank()) {
                statusLabel.setText("Route name and operator are required.");
                return;
            }
            double rate = parseOrZero(rateField.getText());
            BusRoute route = new BusRoute(0, nameField.getText().trim(), operatorField.getText().trim(), rate);
            boolean ok = routeDAO.addRoute(route);
            statusLabel.setText(ok ? "Route added. Now add its segments in the 'Route Segments' tab."
                    : "Failed to add route.");
            if (ok) {
                nameField.clear();
                operatorField.clear();
                rateField.clear();
                refreshRoutes();
            }
        });

        VBox box = new VBox(12, routeTable, new GridPane() {{
            setHgap(8);
            setVgap(8);
            addRow(0, new Label("Name:"), nameField);
            addRow(1, new Label("Operator:"), operatorField, new Label("Rate:"), rateField);
        }}, addBtn);
        box.setPadding(new Insets(12));
        return box;
    }

    private VBox buildSegmentsTab() {
        List<BusRoute> routes = routeDAO.getAllRoutes();
        List<BusStop> stops = stopDAO.getAllStops();

        ComboBox<BusRoute> routeBox = new ComboBox<>(FXCollections.observableArrayList(routes));
        ComboBox<BusStop> fromBox = new ComboBox<>(FXCollections.observableArrayList(stops));
        ComboBox<BusStop> toBox = new ComboBox<>(FXCollections.observableArrayList(stops));
        TextField distanceField = new TextField();
        distanceField.setPromptText("Distance (km)");
        TextField fareField = new TextField();
        fareField.setPromptText("Fare (NPR)");
        TextField orderField = new TextField();
        orderField.setPromptText("Sequence order (1, 2, 3 ...)");

        Button addSegmentBtn = new Button("Add Segment to Route");
        addSegmentBtn.setOnAction(e -> {
            BusRoute route = routeBox.getValue();
            BusStop from = fromBox.getValue();
            BusStop to = toBox.getValue();

            if (route == null || from == null || to == null) {
                statusLabel.setText("Choose a route, a from-stop and a to-stop.");
                return;
            }
            double distance = parseOrZero(distanceField.getText());
            double fare = parseOrZero(fareField.getText());
            int order = (int) parseOrZero(orderField.getText());

            RouteSegment segment = new RouteSegment(route.getRouteId(), route.getRouteName(),
                    from.getStopId(), to.getStopId(), distance, fare, order);
            boolean ok = routeDAO.addSegment(segment);
            statusLabel.setText(ok ? "Segment added to " + route.getRouteName() + "." : "Failed to add segment.");
            if (ok) {
                distanceField.clear();
                fareField.clear();
                orderField.clear();
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Route:"), routeBox);
        form.addRow(1, new Label("From stop:"), fromBox, new Label("To stop:"), toBox);
        form.addRow(2, new Label("Distance:"), distanceField, new Label("Fare:"), fareField);
        form.addRow(3, new Label("Order:"), orderField);

        Label hint = new Label("Tip: add stops in the order a passenger actually rides them (1, 2, 3, ...). "
                + "The route finder automatically treats each segment as usable in both directions.");
        hint.setWrapText(true);

        VBox box = new VBox(14, form, addSegmentBtn, hint);
        box.setPadding(new Insets(12));
        return box;
    }

    private void refreshStops() {
        stopTable.setItems(FXCollections.observableArrayList(stopDAO.getAllStops()));
    }

    private void refreshRoutes() {
        routeTable.setItems(FXCollections.observableArrayList(routeDAO.getAllRoutes()));
    }

    private double parseOrZero(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
