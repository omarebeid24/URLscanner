package backend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class demo extends Application {

    // @FIXME proper way to getting this
    public static Stage stage;
    public static demoController controller;
    public static int page = 0;

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            System.out.println("We are loading page: " + demo.page);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("demo1_load.fxml"));
            primaryStage.setScene(new Scene(loader.load()));

            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setResizable(false);
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.show();
            stage = primaryStage;

            ((demoLoadController) loader.getController()).startLoadingScreen();
            System.out.println("We loaded page: " + demo.page);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String[] args)
    {
        launch(args);
    }

    public static void loadMainScreen()
    {
        try
        {
            page = 1;
            FXMLLoader loader = new FXMLLoader(demo.class.getResource("demo1.fxml"));
            stage.setScene(new Scene(loader.load()));
            controller = loader.getController();
            System.out.println("We loaded page: " + demo.page);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void loadAboutScreen()
    {
        try
        {
            page = 2;
            FXMLLoader loader = new FXMLLoader(demo.class.getResource("demo_about.fxml"));
            stage.setScene(new Scene(loader.load()));
            System.out.println("We loaded page: " + demo.page);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static demoController getMainScreenController()
    {
        return controller;
    }
}