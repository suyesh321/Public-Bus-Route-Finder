/**
 * Some IDEs (and the plain "java" launcher on newer JDKs) refuse to start a
 * class that directly extends javafx.application.Application unless the
 * JavaFX SDK is on the module path. Launching through this plain class,
 * which just forwards to Main.main(), avoids that restriction.
 *
 * Run this class (right click -> Run) instead of Main.java.
 */
public class MainLauncher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
