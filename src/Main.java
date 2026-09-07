import gui.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point of the Public Bus Route Finder - Kathmandu Valley desktop app.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        new LoginView(primaryStage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
