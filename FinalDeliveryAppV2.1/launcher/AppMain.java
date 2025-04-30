package launcher;

import controller.AppController;
import controller.AppLoadController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * MAIN -
 * */
public class AppMain extends Application{

    public static AppController controller;
    public static Stage stage;
    public static int currentPage = 0;

    @Override
    public void start(Stage primaryStage) {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/pages/app_load.fxml"));
            primaryStage.setScene(new Scene(loader.load()));

            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setResizable(false);
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.getIcons().add(new Image("images/download11.png"));
            primaryStage.show();
            stage = primaryStage;

            ((AppLoadController) loader.getController()).startLoadingScreen();
        }
        catch(Exception error)
        {
            System.err.println(error.getMessage());
        }
    }
    public static void launchApp(String[] args)
    {
        launch(args);
    }

    public static void loadMainPage() {
        try
        {
            currentPage = 1;
            FXMLLoader loader = new FXMLLoader(AppMain.class.getResource("../view/pages/url_check.fxml"));
            stage.setScene(new Scene(loader.load()));
            controller = loader.getController();
        }
        catch(Exception error)
        {
            System.err.println(error.getMessage());
        }
    }
    public static void loadAboutPage() {
        try
        {
            currentPage = 2;
            FXMLLoader loader = new FXMLLoader(AppMain.class.getResource("../view/pages/about.fxml"));
            stage.setScene(new Scene(loader.load()));
        }
        catch(Exception error)
        {
            System.err.println(error.getMessage());
        }
    }
}