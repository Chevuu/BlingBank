package sirs.com;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStage;

    public static String currentUser = "-1";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/screens/login-screen.fxml"));
            Scene scene = new Scene(login);
            primaryStage.setTitle("Insurance & Banking: BlingBank");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.out.println("Wrong FXML path.");
            e.printStackTrace();
        }
    }

    public static void setRoot(String fxml) throws IOException {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource(fxml));
            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            System.out.println("Wrong FXML path.");
        }

    }
}
