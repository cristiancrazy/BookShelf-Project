/* ========================================
 * Author: Cristian Capraro
 * September 2022
 * This is controller class of desk.fxml
 * ======================================== */



package it.books.gcon;

import it.books.base.Book;
import it.books.MicrosoftDB;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DeskG {

    @FXML
    private void initialize(){
        CloseViewButton.setDisable(true);
    }

    /* Flags - Database loading checkpoint */
    private static int off = 0, limit = 50;
    private static boolean finished = false;

    @FXML
    private BorderPane MainPane;
    @FXML
    private MenuItem AboutButton;
    @FXML
    private MenuItem CloseViewButton;

    @FXML
    private MenuItem OpenButton;

    @FXML
    private void CloseBtn() {
        Alert msg = new Alert(Alert.AlertType.CONFIRMATION, "Ricordarsi di salvare. Uscire dal programma?", ButtonType.YES, ButtonType.CANCEL);
        msg.showAndWait().ifPresent(j -> {
            if(j.equals(ButtonType.YES)) System.exit(0); //Exit without any problem
        });
    }
    @FXML
    private void AboutBtn() {
        //Generate a new window from code
        BorderPane pane = new BorderPane();
        Label label = new Label("Software realizzato per la Biblioteca di Divignano.\nSoftware libero - con licenza MIT\nVersione 1.0 - 2022\n~ Tecnico: Cristian C.");
        pane.setCenter(label);

        //Scene and stage
        Scene scene = new Scene(pane, 320, 240);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setMinWidth(320);
        stage.setMinWidth(240);
        stage.setResizable(false);
        stage.setTitle("Informazioni Catalogo");
        stage.getIcons().add(new Image("https://www.ngmadv.it/wp-content/uploads/2017/06/User-yellow-icon.png"));
        //Actions (disable multiple instances)
        stage.setOnHiding(i -> AboutButton.setDisable(false));
        stage.setOnShowing(i -> AboutButton.setDisable(true));

        stage.showAndWait();
    }

    @FXML
    ToggleGroup view;
    @FXML
    RadioMenuItem RadioMenuTLoadButton, RadioMenuILoadButton;
    @FXML
    RadioMenuItem RadioMenuTViewModeButton, RadioMenuTCViewModeButton;

    @FXML @SuppressWarnings("ConstantConditions")
    private void OpenBtn() throws IOException {
        //Selecting files
        FileChooser dChooser = new FileChooser();
        dChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database Microsoft", "*.mdb", "*.accdb"));
        //dChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database Microsoft (new)", "*.accdb"));
        File dbFile = dChooser.showOpenDialog(MainPane.getScene().getWindow());
        if(dbFile == null){
            Alert msg = new Alert(Alert.AlertType.WARNING, "Non \u00E8 stato selezionato alcun file", ButtonType.OK);
            msg.show();
        }else{
            //Loading and operating with database files
            ArrayList<Book> book;
            //Share info
            OldDeskDetailG.getDBFile(dbFile);

            if(view.getToggles().get(0).isSelected()){ //Incremental load
                RadioMenuILoadButton.setDisable(true);
                RadioMenuTLoadButton.setDisable(true);
                RadioMenuTCViewModeButton.setDisable(true);
                RadioMenuTViewModeButton.setDisable(true);

                book = MicrosoftDB.connectAndGet(dbFile, off, limit);
                //Update offset
                off += limit;
            }
            else{ //Full load
                RadioMenuILoadButton.setDisable(true);
                RadioMenuTLoadButton.setDisable(true);
                RadioMenuTCViewModeButton.setDisable(true);
                RadioMenuTViewModeButton.setDisable(true);

                book = MicrosoftDB.connectAndGet(dbFile);

            }

            if(book == null){
                Alert msg = new Alert(Alert.AlertType.ERROR, "Errore del file. Non \u00E8 stata trovata una tabella valida nel file selezionato.", ButtonType.OK);
                msg.show();
            }else{
                TableView<Book> table = new TableView<>();
                List<TableColumn<Book, String>> columns = new ArrayList<>();
                //Working with reflection API
                Field[] bookFields = Book.class.getDeclaredFields();

                int indexLocale = 0;
                for(Field f : bookFields){
                    TableColumn<Book, String> column = new TableColumn<>(Book.getLocale().get(indexLocale++));
                    column.setCellValueFactory(new PropertyValueFactory<>(f.getName()));
                    columns.add(column);
                }

                table.getColumns().addAll(columns);
                table.getItems().addAll(book);
                table.setFixedCellSize(70);

                //Detailed View
                if(RadioMenuTCViewModeButton.isSelected()){
                    //Load Win
                    Parent root = FXMLLoader.load(OldDeskDetailG.class.getResource("oldDeskDetails.fxml"));
                    BorderPane pane = (BorderPane) root;

                    Stage MainStage = (Stage) MainPane.getScene().getWindow();

                    MainStage.widthProperty().addListener((obs, ov, nv) -> {
                        double valueX = MainStage.getWidth();

                        MainStage.heightProperty().addListener((bso, vo, vn) -> {
                            double valueY = MainStage.getHeight() - 40;
                            pane.setPrefSize(MainStage.getWidth(), valueY);
                        });
                        pane.setPrefSize(valueX, MainStage.getHeight() - 40);
                    });


                    //pane.setPrefSize(MainPane.getWidth(), MainPane.getHeight());
                    pane.setCenter(table);
                    pane.setPrefSize(MainStage.getWidth(),MainStage.getHeight() - 40);
                    MainPane.setCenter(pane);
                }else{
                    MainPane.setCenter(table);
                }

                CloseViewButton.setDisable(false);
                OpenButton.setDisable(true);

                //Define scrollbar action delayed on incremental load
                if(view.getToggles().get(0).isSelected()){
                    Task<Void> scrollAction = new Task<>() {
                        @Override
                        @SuppressWarnings("unchecked")
                        protected Void call() throws Exception {
                            Thread.sleep(2000);
                            ScrollBar scroll = (ScrollBar) MainPane.getCenter().lookup(".scroll-bar:vertical");
                            scroll.valueProperty().addListener((obsV, oldV, newV) -> {
                                if((double) newV == 1.0){ //Reaches the bottom
                                    if(!finished){
                                        List<Book> otherBooks = MicrosoftDB.connectAndGet(dbFile, off, limit);
                                        finished = otherBooks == null;
                                        if(!finished){
                                            off+=limit;
                                            ((TableView<Book>) MainPane.getCenter()).getItems().addAll(otherBooks);
                                        }
                                    }
                                }
                            });
                            return null;
                        }
                    };

                    new Thread(scrollAction).start();
                }
            }
        }
    }

    @FXML
    private void CreateNewBtn(){

    }

    @FXML
    private void CloseViewBtn(){
        MainPane.setCenter(null);
        CloseViewButton.setDisable(true);
        RadioMenuILoadButton.setDisable(false);
        RadioMenuTLoadButton.setDisable(false);
        OpenButton.setDisable(false);
        RadioMenuTCViewModeButton.setDisable(false);
        RadioMenuTViewModeButton.setDisable(false);
        System.gc(); //Try optimization
        //Reset offset
        off = 0; finished = false;
    }
}
