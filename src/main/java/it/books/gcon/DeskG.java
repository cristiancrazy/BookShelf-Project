/* ========================================
 * Author: Cristian Capraro
 * September 2022
 * This is controller class of desk.fxml
 * ======================================== */

package it.books.gcon;

import it.books.Main;
import it.books.base.Book;
import it.books.MicrosoftDB;
import it.books.base.BookConverter;
import it.books.base.EvBook;
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
import javafx.stage.StageStyle;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DeskG {

    @FXML
    private void initialize(){
        CloseViewButton.setDisable(true);
    }

    /* Flags - Database loading checkpoint */
    private static int off = 0;
    private static final int limit = 50;
    private static boolean finished = false;

    @FXML
    private BorderPane MainPane;
    @FXML
    private MenuItem AboutButton;
    @FXML
    private MenuItem CloseViewButton;

    @FXML
    private MenuItem OpenButton, CreateButton;

    @FXML
    private MenuItem CheckUpdatesButton;

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
        Label label = new Label("Software realizzato per la Biblioteca di Divignano.\nSoftware libero - con licenza MIT\nVersione "+Main.getVersion()+" - 2022\nProgramma realizzato da:\nCristian Capraro");
        label.idProperty().setValue("About");
        pane.setCenter(label);

        //Scene and stage
        Scene scene = new Scene(pane, 320, 240);
        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("defStyle.css")).toExternalForm());
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

            //Check if there's an .ini config file in the folder.
            File configFile = Path.of(dbFile.getParentFile().getPath(), "config.ini").toFile();
            System.out.println(configFile);
            try{
                Wini config = new Wini(configFile);
                boolean newFormat = false; //Compatibility --> False, Otherwise use a modern Database
                if(configFile.exists()){
                    String databaseType = config.get("BookShelf App", "Database");
                    newFormat = databaseType.equals("NEW_ACCDB");
                    System.out.println(newFormat);
                }

                //Working on new Database model
                if(newFormat){
                    String status = config.get("BookShelf App", "Status");
                    //TODO: NullPointer exception could be thrown
                    if (status.equals("INIT_REQUIRED")) {
                        if(MicrosoftDB.CreateAndInit(dbFile)){ //Init database with spec tables
                            config.remove("BookShelf App", "Status");
                            config.put("BookShelf App", "Status", "READY");
                            config.store(configFile);
                        }

                        try{
                            String dbSource = config.get("BookShelf App", "Source");
                            if(Objects.nonNull(dbSource)){
                                File source = new File(dbSource);
                                if(source.exists()){
                                    List<EvBook> converted = MicrosoftDB.connectAndGet(source, 0, 0).stream().map(i -> BookConverter.ConvertToEvBook(i, -1)).toList();
                                    for(EvBook book : converted){
                                        MicrosoftDB.connectAndUploadANewBook(dbFile, book);
                                    }
                                }else{
                                    new Alert(Alert.AlertType.ERROR, "File sorgente non valido o inesistente!\nIl database viene vuoto.", ButtonType.OK).show();
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                    //Loading and operating with db file (new .accdb database)
                    ArrayList<EvBook> books = MicrosoftDB.connectAndGetNew(dbFile);
                    if(books == null){
                        new Alert(Alert.AlertType.ERROR, "Errore caricamento database!", ButtonType.OK).show();
                        System.gc(); //Run GC memory optimization
                        return;
                    }
                    System.out.println("Numero libri caricati: " + books.size()); //Debug info

                    //Build the table
                    TableView<EvBook> table = new TableView<>();
                    //Create columns
                    ArrayList<TableColumn<EvBook, String>> tableColumns = new ArrayList<>();
                    tableColumns.add(0, new TableColumn<>("ID"));
                    tableColumns.get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
                    tableColumns.add(1, new TableColumn<>("Codice/ISBN"));
                    tableColumns.get(1).setCellValueFactory(new PropertyValueFactory<>("code"));
                    tableColumns.add(2, new TableColumn<>("Titolo"));
                    tableColumns.get(2).setCellValueFactory(new PropertyValueFactory<>("title"));
                    tableColumns.add(3, new TableColumn<>("Genere"));
                    tableColumns.get(3).setCellValueFactory(new PropertyValueFactory<>("genre"));
                    tableColumns.add(4, new TableColumn<>("Anno"));
                    tableColumns.get(4).setCellValueFactory(new PropertyValueFactory<>("year"));
                    tableColumns.add(5, new TableColumn<>("Autore/i"));
                    tableColumns.get(5).setCellValueFactory(new PropertyValueFactory<>("authors"));
                    tableColumns.add(6, new TableColumn<>("Prestito"));
                    tableColumns.get(6).setCellValueFactory(new PropertyValueFactory<>("lease"));
                    //Insert data into the table
                    table.getColumns().addAll(tableColumns);
                    table.getItems().addAll(books);
                    table.setFixedCellSize(50);

                    //Enable close button and disable other buttons
                    OpenButton.setDisable(true);
                    RadioMenuILoadButton.setDisable(true);
                    RadioMenuTLoadButton.setDisable(true);
                    RadioMenuTCViewModeButton.setDisable(true);
                    RadioMenuTViewModeButton.setDisable(true);
                    CloseViewButton.setDisable(false);
                    CheckUpdatesButton.setDisable(true);

                    //Load interface
                    Parent root = FXMLLoader.load(NewDeskDetailG.class.getResource("newDeskDetail.fxml"));
                    BorderPane pane = (BorderPane) root;
                    pane.setCenter(table);

                    Stage MainStage = (Stage) MainPane.getScene().getWindow();

                    MainStage.widthProperty().addListener((obs, ov, nv) -> { //Dynamic resize with window
                        double valueX = MainStage.getWidth();

                        MainStage.heightProperty().addListener((sbo, vo, vn) -> {
                            double valueY = MainStage.getHeight() - 40;
                            pane.setPrefSize(MainStage.getWidth(), valueY);
                        });
                        pane.setPrefSize(valueX, MainStage.getHeight() - 40);
                    });

                    MainPane.setCenter(pane);
                    pane.setPrefSize(MainStage.getWidth(),MainStage.getHeight() - 40); //Static resize
                    NewDeskDetailG.setWorkingDB(dbFile); //Send working database to the other window (information share)
                } else throw new Exception("(Compatibility Mode) - Works on old Database Model");

            }catch (Exception notFound){
                //Debug notFound.printStackTrace();
                //Loading and operating with database files
                ArrayList<Book> book = new ArrayList<>();
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

                    book = MicrosoftDB.connectAndGet(dbFile, 0, 0);

                }

                if(book == null){
                    Alert msg = new Alert(Alert.AlertType.ERROR, "Errore del file. Non sono state trovate tabelle valide nel file selezionato.", ButtonType.OK);
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

                            MainStage.heightProperty().addListener((sbo, vo, vn) -> {
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
    }

    @FXML
    private void CreateNewBtn() throws IOException{
        Parent root = FXMLLoader.load(Objects.requireNonNull(this.getClass().getResource("createNewDesk.fxml")));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("defStyle.css")).toExternalForm());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Crea Database");
        stage.show();
        CreateButton.setDisable(true);
        stage.setOnHiding(i -> {
            CreateButton.setDisable(false);
            System.gc(); //Optimize
        });
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
        CheckUpdatesButton.setDisable(false);
        System.gc(); //Try optimization
        //Reset offset
        off = 0; finished = false;
    }

    public void UpdatesBtn() {
        Main.checkUpdates();
    }
}
