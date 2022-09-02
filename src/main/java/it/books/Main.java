/* ====================================================================
 * Author: Cristian Capraro
 * Date: September 2022
 * JDK Version: Liberica JDK 17.0.4 LTS - FULL
 * Binaries and Sources are released under MIT Licence
 * --------------------------------------------------------------------
 * Software intended only for education, or for providing information.
 * This software is realized to replace a '90s program used by
 * a local librarians to catalogue books.
 * --------------------------------------------------------------------
 * ==================================================================== */



package it.books;

import it.books.gcon.DeskG;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(DeskG.class.getResource("desk.fxml")));
        Scene scene = new Scene(root, 640, 480);
        //scene.getStylesheets().add(DeskG.class.getResource("dark.css").toExternalForm());
        //Setting minimals (Aspect ratio 4:3)
        stage.setMinWidth(640);
        stage.setMinWidth(480);
        //Override exit button
        stage.setOnCloseRequest(i -> {
            i.consume();
            Alert msg = new Alert(Alert.AlertType.CONFIRMATION, "Ricordarsi di salvare. Uscire dal programma?", ButtonType.YES, ButtonType.CANCEL);
            msg.showAndWait().ifPresent(j -> {
                if(j.equals(ButtonType.YES)) System.exit(0); //Exit without any problem
            });
        });
        stage.setScene(scene);
        stage.setTitle("Catalogo");
        stage.getIcons().add(new Image("https://aux.iconspalace.com/uploads/book-icon-256-2103632816.png"));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}