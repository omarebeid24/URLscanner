package backend;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    Label summaryTable;

    @FXML
    Text testError;

    @FXML
    Label timeStamp;

    @FXML
    Label sslAnalysis;

    @FXML
    Label URLanalysis;

    @FXML
    Label MLanalysis;

    @FXML
    ScrollPane scrollPane;

    @FXML
    ImageView verdictImage;

    @FXML
    Text verdictText;

    @FXML
    ProgressBar confidenceBar;

    @FXML
    ProgressBar riskBar;

    @FXML
    ProgressBar MLBar;

    @FXML
    Text confidencePer;

    @FXML
    Text riskPer;

    @FXML
    Text MLPer;


    private boolean isMenuOpen = true;


    public void CheckURL()
    {
        System.out.println("Tried to load page: 1");
        checkURL.setStyle("-fx-background-color: #161625;");
        aboutURL.setStyle("-fx-background-color: transparent;");

        if(demo.page == 2)
        {
            System.out.println("We are loading page: 1");
            demo.loadMainScreen();
        }
        else
        {
            System.out.println("We are still at the same page");
        }
    }

    public void AboutPage()
    {
        System.out.println("Tried to load page: 2");
        aboutURL.setStyle("-fx-background-color:  #161625;");
        checkURL.setStyle("-fx-background-color: transparent;");

        if(demo.page == 1)
        {
            System.out.println("We are loading page: 2");
            demo.loadAboutScreen();
        }
        else
        {
            System.out.println("We are still at the same page");
        }
    }


    // @TODO Remove the comments
    public void menu ()
    {
        if(isMenuOpen)
        {
            isMenuOpen = false;

            TranslateTransition tt = new TranslateTransition(Duration.millis(400), menuOpener);
            tt.setToX(-100f);
            tt.play();
        }
        else
        {
            isMenuOpen = true;

            TranslateTransition tt = new TranslateTransition(Duration.millis(400), menuOpener);
            tt.setToX(0);
            tt.play();
        }

    }


    public void search()
    {
        scrollPane.setVisible(true);
        testError.setVisible(false);
/*
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
*/
        summaryTable.setText("");

        timeStamp.setText("");
        sslAnalysis.setText("");
        URLanalysis.setText("");
        MLanalysis.setText("");

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
        scrollPane.setVisible(false);
        testError.setVisible(true);
        testError.setText("Error: " + msg);
    }



    public void updateVerdict(boolean isSafe)
    {
        if(isSafe)
        {
            verdictImage.setImage(new Image("download14.png"));
            verdictText.setFill(Color.web("#22ff00"));
        }
        else
        {
            verdictImage.setImage(new Image("download15.png"));
            verdictText.setFill(Color.web("#ff0000"));
        }
    }

    public void updateConfidenceLevel(double level)
    {
        if(level * 100 > 40)
        {
            confidenceBar.setProgress(level);
            confidenceBar.setStyle("-fx-accent: #22ff00;");


            confidencePer.setText(String.format("%.2f" ,(level*100)) + "%");
            confidencePer.setFill(Color.web("#22ff00"));
        }
        else
        {
            confidenceBar.setProgress(level);
            confidenceBar.setStyle("-fx-accent: #ff0000;");

            confidencePer.setText(String.format("%.2f" ,(level*100)) + "%");
            confidencePer.setFill(Color.web("#ff0000"));
        }
    }

    public void updateRiskLevel(double level)
    {
        if(level * 100 > 40)
        {
            riskBar.setProgress(level);
            riskBar.setStyle("-fx-accent: #22ff00;");

            riskPer.setText(String.format("%.2f" ,(level*100)) + "%");
            riskPer.setFill(Color.web("#22ff00"));
        }
        else
        {
            riskBar.setProgress(level);
            riskBar.setStyle("-fx-accent: #ff0000;");

            riskPer.setText(String.format("%.2f" ,(level*100)) + "%");
            riskPer.setFill(Color.web("#ff0000"));
        }
    }

    public void updateMLLevel(double level)
    {
        if(level *100 > 40)
        {
            MLBar.setProgress(level);
            MLBar.setStyle("-fx-accent: #22ff00;");

            MLPer.setText(String.format("%.2f" ,(level*100)) + "%");
            MLPer.setFill(Color.web("#22ff00"));
        }
        else
        {
            MLBar.setProgress(level);
            MLBar.setStyle("-fx-accent: #ff0000;");

            MLPer.setText(String.format("%.2f" ,(level*100)) + "%");
            MLPer.setFill(Color.web("#ff0000"));
        }
    }




    public void updateSummarySection(String assessment , String recommendation) {
        summaryTable.setText("SECURITY ASSESSMENT:\n"+ assessment + "\n\n"+ "RECOMMENDATION:\n" + recommendation);
    }

    public void updateSSLReport(String report)
    {
        sslAnalysis.setText(report);
    }

    public void updateURLAnalysis(String report)
    {
        URLanalysis.setText(report);
    }

    public void updateMLFeature(String report) {
        MLanalysis.setText(report);
    }

    public void updateTimeStamp(String time)
    {
        timeStamp.setText(time);
    }
}