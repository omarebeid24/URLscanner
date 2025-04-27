package backend;

import javafx.animation.Animation;
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

public class demoLoadController {

    @FXML
    ImageView loadingBar;

    @FXML
    Text loadingText;


    private int dots;

    public void updateLoadingText() {
        switch(dots)
        {
            case 1 : loadingText.setText("The application is loading with an estimation time of three minutes."); break;
            case 2 : loadingText.setText("The application is loading with an estimation time of three minutes.."); break;
            case 3 : loadingText.setText("The application is loading with an estimation time of three minutes..."); break;
            default: loadingText.setText("The application is loading with an estimation time of three minutes"); dots = 0; break;
        }

        dots++;
    }

    public void startLoadingScreen()
    {
        Main.loadModel();

        RotateTransition animation = new RotateTransition(Duration.millis(1000), loadingBar);
        animation.setOnFinished( (event) -> {
            if(Main.isModelReady)
            {
                animation.stop();
                demo.loadMainScreen();
            }
            else
            {
                animation.play();
                updateLoadingText();
            }
        });
        animation.setByAngle(360);
        animation.play();
//        RotateTransition animation = new RotateTransition(Duration.millis(1000), loadingBar);
//        animation.setOnFinished( (event) -> {
//            animation.play();
//            updateLoadingText();
//        });
//        animation.setByAngle(360);
//
//        Thread loadingThread = new Thread(() ->
//        {
//
//            animation.play();
//
//            while(true)
//            {
//                if(Main.isModelReady)
//                {
//                    Thread.currentThread().interrupt();
//                    animation.stop();
//                    System.out.println("Model is ready!");
//                }
//            }
//        });
//
//        loadingThread.start();
//        Main.main(null);
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
}
