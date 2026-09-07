package gui;

import dao.UserDAO;
import dao.UserDAOImpl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;
import model.enums.UserRole;

/**
 * Landing screen: existing users log in, new passengers can register.
 * Admin accounts are seeded directly in the database (see sql/schema.sql).
 */
public class LoginView {

    private final Stage stage;
    private final UserDAO userDAO = new UserDAOImpl();

    public LoginView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        Label title = new Label("Public Bus Route Finder \u2014 Kathmandu Valley");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("Find the fastest, cheapest way across the valley");
        subtitle.getStyleClass().add("subtitle-label");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginBtn = new Button("Log In");
        Button registerBtn = new Button("Create Account");
        Button guestBtn = new Button("Continue as Guest");

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");

        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter both email and password.");
                return;
            }

            User user = userDAO.authenticate(email, password);
            if (user == null) {
                statusLabel.setText("Invalid credentials. Please try again.");
                return;
            }

            if (user.getRole() == UserRole.ADMIN) {
                new AdminDashboardView(stage, user).show();
            } else {
                new PassengerDashboardView(stage, user).show();
            }
        });

        registerBtn.setOnAction(e -> showRegisterDialog(statusLabel));

        guestBtn.setOnAction(e -> {
            User guest = new User(0, "Guest", "guest@local", "", UserRole.PASSENGER);
            new PassengerDashboardView(stage, guest).show();
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setAlignment(Pos.CENTER);
        form.add(new Label("Email:"), 0, 0);
        form.add(emailField, 1, 0);
        form.add(new Label("Password:"), 0, 1);
        form.add(passwordField, 1, 1);

        HBoxButtons buttons = new HBoxButtons(loginBtn, registerBtn, guestBtn);

        VBox root = new VBox(18, title, subtitle, form, buttons.asHBox(), statusLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root-pane");

        Scene scene = new Scene(root, 640, 420);
        scene.getStylesheets().add(getClass().getResource("/gui/style.css") != null
                ? getClass().getResource("/gui/style.css").toExternalForm() : "");

        stage.setTitle("Public Bus Route Finder - Kathmandu Valley");
        stage.setScene(scene);
        stage.show();
    }

    private void showRegisterDialog(Label statusLabel) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create Passenger Account");

        TextField nameField = new TextField();
        nameField.setPromptText("Full name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new User(0, nameField.getText().trim(), emailField.getText().trim(),
                        passwordField.getText(), UserRole.PASSENGER);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newUser -> {
            if (newUser.getFullName().isEmpty() || newUser.getEmail().isEmpty() || newUser.getPassword().isEmpty()) {
                statusLabel.setText("All fields are required to register.");
                return;
            }
            if (userDAO.emailExists(newUser.getEmail())) {
                statusLabel.setText("That email is already registered. Try logging in.");
                return;
            }
            boolean success = userDAO.register(newUser);
            statusLabel.setText(success ? "Account created! You can log in now." : "Registration failed.");
        });
    }

    /** Tiny helper so the button row can be built without importing HBox everywhere. */
    private static class HBoxButtons {
        private final javafx.scene.layout.HBox box;

        HBoxButtons(Button... buttons) {
            box = new javafx.scene.layout.HBox(12, buttons);
            box.setAlignment(Pos.CENTER);
        }

        javafx.scene.layout.HBox asHBox() {
            return box;
        }
    }
}
