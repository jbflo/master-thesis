//package TestGui;
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.layout.BorderPane;
//import javafx.stage.Stage;
//import com.sun.javafx.application.LauncherImpl;
//import java.io.IOException;
//
//public class TestFX extends Application {
//
//    private Stage primaryStage;
//    private BorderPane rootLayout;
//
////    public static void main(String[] args) {
////        LauncherImpl.launchApplication(TestFX.class, MyPreloader.class, args);
////    }
//    public static void main() {
//       Application.launch();
//    }
//
//    @Override // FX
//    public void start(Stage primaryStage) throws Exception {
//        //super.start();
//        try {
//            // Because we need to init the JavaFX toolkit - which usually Application.launch does
//            // I'm not sure if this way of launching has any effect on anything
//            //  new JFXPanel();
//
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    // Your class that extends Application
//                    initRootLayout();
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        // the wumpus doesn't leave when the last stage is hidden.
//        //Platform.setImplicitExit(false);
//        this.primaryStage = primaryStage;
//        primaryStage.setTitle("Rapp Automation");
//
//        //primaryStage.show();
//      //  initRootLayout();
//        //Platform.exit();
//    }
//
//    public void initRootLayout() { // FX
//        try {
//            // Load root layout from fxml file.
//            Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));
//
//            // Create a Scene for the stage
//            Scene scene = new Scene(root, 1000, 475);
//            // Show the scene containing the root layout.
//            Platform.runLater(() -> {
//                primaryStage.setTitle("FXML Laser Control");
//                primaryStage.setScene(scene);
//                primaryStage.show();
//            });
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
