package backend;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class demoController {

    @FXML
    VBox menuOpener;

    @FXML
    HBox checkURL;

    @FXML
    HBox batchURL;


    @FXML
    HBox aboutURL;

    @FXML
    TextField searchBar;

    @FXML
    ProgressIndicator confidencePro;

    @FXML
    ProgressIndicator riskPro;

    @FXML
    Text confidenceText;

    @FXML
    Text riskText;

    @FXML
    Label summaryTable;

    @FXML
    Text testError;

    private boolean isMenuOpen = true;


    public void CheckURL()
    {
        System.out.println("Check!");
        checkURL.setStyle("-fx-background-color: #161625;");
        batchURL.setStyle("-fx-background-color: transparent;");
        aboutURL.setStyle("-fx-background-color: transparent;");
    }

    public void BatchLinks()
    {
        System.out.println("Batch");
        batchURL.setStyle("-fx-background-color:  #161625;");
        checkURL.setStyle("-fx-background-color: transparent;");
        aboutURL.setStyle("-fx-background-color: transparent;");
    }

    public void AboutPage()
    {
        System.out.println("About!");
        aboutURL.setStyle("-fx-background-color:  #161625;");
        checkURL.setStyle("-fx-background-color: transparent;");
        batchURL.setStyle("-fx-background-color: transparent;");
    }


    // @TODO Remove the comments
    public void menu ()
    {
        System.out.println("menu!");

        if(isMenuOpen)
        {
            isMenuOpen = false;

            System.out.println("Closing...");
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), menuOpener);
            tt.setToX(-100f);
            tt.play();
        }
        else
        {
            isMenuOpen = true;

            System.out.println("Opening...");
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), menuOpener);
            tt.setToX(0);
            tt.play();
        }

    }


    public void search()
    {
        testError.setVisible(false);

        riskPro.setProgress(0.01);
        riskPro.setStyle( "-fx-base: black;" +
                          "-fx-fill: white;" +
                          "-fx-accent: white;" +
                          "-fx-box-border: white;");
        riskText.setFill(Color.WHITE);

        confidencePro.setProgress(0.01);
        confidencePro.setStyle( "-fx-base: black;" +
                                "-fx-fill: white;" +
                                "-fx-accent: white;" +
                                "-fx-box-border: white;");
        confidenceText.setFill(Color.WHITE);

        summaryTable.setText("");

        Main.runDemo(searchBar.getText());
    }




    // @TODO fix this 5ara code
    public void minimize ()
    {
        System.out.println("minimize screen!");
        demo.stage.setIconified(true);
    }

    // @TODO Remove the comments
    public void close (){
        System.out.println("Close App!");
        System.exit(0);
    }

    @FXML
    public void hover(MouseEvent event)
    {
        Node source = (Node) event.getSource();
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.web("#86aaf9"));
        source.setEffect(innerShadow);

        if(source.getId() != null)
        {
            demo.stage.getScene().setCursor(Cursor.HAND);
        }

    }

    @FXML
    public void unhover(MouseEvent event)
    {
        Node source = (Node) event.getSource();
        source.setEffect(null);

        if(source.getId() != null)
        {
            demo.stage.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    public void sendError(String msg)
    {
        testError.setText("Error: " + msg);
        testError.setVisible(true);
    }

    public void updateRiskProgressLevel(double level, boolean  isSafe)
    {
        if(isSafe)
        {
            riskPro.setProgress(level);
            riskPro.setStyle("-fx-base: black;" +
                    "-fx-fill: white;" +
                    "-fx-accent: green;" +
                    "-fx-box-border: green;");
            riskText.setFill(Color.GREEN);
        }
        else
        {
            riskPro.setProgress(level);
            riskPro.setStyle("-fx-base: black;" +
                    "-fx-fill: white;" +
                    "-fx-accent: red;" +
                    "-fx-box-border: red;");
            riskText.setFill(Color.RED);
        }
    }

    public void updateConfidenceProgressLevel(double level)
    {
        if(level *100 < 40)
        {
            confidencePro.setProgress(level);
            confidencePro.setStyle( "-fx-base: black;" +
                                    "-fx-fill: white;" +
                                    "-fx-accent: yellow;" +
                                    "-fx-box-border: yellow;");
            confidenceText.setFill(Color.YELLOW);
        }
        else
        {
            confidencePro.setProgress(level);
            confidencePro.setStyle( "-fx-base: black;" +
                                    "-fx-fill: white;" +
                                    "-fx-accent: green;" +
                                    "-fx-box-border: green;");
            confidenceText.setFill(Color.GREEN);
        }
    }


    public void updateSummarySection(String assessment , String recommendation)
    {
        summaryTable.setText("SECURITY ASSESSMENT:\n"+ assessment + "\n\n"+ "RECOMMENDATION:\n" + recommendation);
    }
}