package controller;

import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import launcher.AppMain;
import model.URLProcessor;

public class AppLoadController {

    @FXML
    ImageView loadingBar;

    @FXML
    Text loadingText;


    private int dots;

    public void updateLoadingText() {
        switch(dots)
        {
            case 1 : loadingText.setText("Estimated loading time is three minutes."); break;
            case 2 : loadingText.setText("Estimated loading time is three minutes.."); break;
            case 3 : loadingText.setText("Estimated loading time is three minutes..."); break;
            default: loadingText.setText("Estimated loading time is three minutes"); dots = 0; break;
        }

        dots++;
    }
    public void startLoadingScreen() {
        URLProcessor.getInstance().loadModel();

        RotateTransition animation = new RotateTransition(Duration.millis(1000), loadingBar);
        animation.setOnFinished( (event) -> {
            if(URLProcessor.getInstance().isModelReady())
            {
                animation.stop();
                AppMain.loadMainPage();
            }
            else
            {
                animation.play();
                updateLoadingText();
            }
        });
        animation.setByAngle(360);
        animation.play();
    }


    public void minimize ()
    {
        AppMain.stage.setIconified(true);
    }
    public void close (){
        System.exit(0);
    }

    public void hover(MouseEvent event) {
        Node source = (Node) event.getSource();
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.web("#86aaf9"));
        source.setEffect(innerShadow);

        if(source.getId() != null)
        {
            AppMain.stage.getScene().setCursor(Cursor.HAND);
        }

    }
    public void unhover(MouseEvent event) {
        Node source = (Node) event.getSource();
        source.setEffect(null);

        if(source.getId() != null)
        {
            AppMain.stage.getScene().setCursor(Cursor.DEFAULT);
        }
    }
}
