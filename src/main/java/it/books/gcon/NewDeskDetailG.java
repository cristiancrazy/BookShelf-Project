/* =================================================
 * Author: Cristian Capraro
 * Personal Project under MIT Licence
 * This class is the graphic controller to work on
 * new Microsoft database specific format.
 * ================================================= */

package it.books.gcon;

import it.books.MicrosoftDB;
import it.books.base.EvBook;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class NewDeskDetailG {
	//Get DB file path
	private static File workingDB = null;

	/** Method used to share data between classes, it will be used to
	 * get the working database path **/
	public static void setWorkingDB(File dbFile) {
		workingDB = dbFile;
		AddBookFormG.setWorkingDB(workingDB);
	}

	//On init
	@FXML @SuppressWarnings("unchecked")
	private void initialize(){
		Task<Void> tableSet = new Task<>() {
			@Override
			protected Void call() throws Exception {
				Thread.sleep(512); //Delay
				TableView<EvBook> table = (TableView<EvBook>) MainPane.getCenter();
				table.setOnMouseClicked(i -> {
					EvBook selected;
					if((selected = table.getFocusModel().getFocusedItem())!= null){
						//Set data
						TitleTextBox.setText(selected.getTitle());
						AuthorsTextBox.setText(selected.getAuthors());
						GenreTextBox.setText(selected.getGenre());
						YearTextBox.setText(selected.getYear());
						EditionTextBox.setText(selected.getEdition());
						PageTextBox.setText(""+selected.getPages());
						CodeTextBox.setText(selected.getCode());
					}
				});
				return null;
			}
		};

		new Thread(tableSet).start();
	}

	/* FXML ELEMENTS */
	@FXML
	BorderPane MainPane;
	@FXML
	Button AddBookBtn, RemBookBtn, DetailBtn, LeaseBtn, FilterBookBtn;
	@FXML
	TextField TitleTextBox, AuthorsTextBox, GenreTextBox, YearTextBox, EditionTextBox, PageTextBox, CodeTextBox;
	@FXML
	TextField TitleSearchTextBox, AuthorSearchTextBox, YearSearchTextBox, CodeSearchTextBox;

	//FXML ACTION

	/** Delete a selected book from the database **/
	@FXML @SuppressWarnings("unchecked")
	public void RemoveBookButton(){
		TableView<EvBook> table = (TableView<EvBook>) MainPane.getCenter();
		EvBook selected = table.getFocusModel().getFocusedItem();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Procedere alla cancellazione del seguente libro:\n"+selected.getTitle()+" - "+selected.getAuthors(), ButtonType.YES, ButtonType.NO);
		alert.showAndWait().ifPresent(btn -> {
			if(btn.equals(ButtonType.YES)){
				table.getItems().remove(selected);
				MicrosoftDB.DeleteEntry(workingDB, selected.getCode(), selected.getTitle());
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void AddBookButton() {
		AddBookFormG.setParent((TableView<EvBook>) MainPane.getCenter());
		try{
			Parent addBk = FXMLLoader.load(Objects.requireNonNull(AddBookFormG.class.getResource("addBookForm.fxml")));
			Scene scene = new Scene(addBk);
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setResizable(false);
			stage.setIconified(false);
			stage.initStyle(StageStyle.UNDECORATED);
			stage.show();
			stage.setOnHiding(ref -> {
				((TableView<EvBook>) MainPane.getCenter()).getItems().clear();
				((TableView<EvBook>) MainPane.getCenter()).getItems().addAll(MicrosoftDB.connectAndGetNew(workingDB)); //Reload db
			});
			stage.setOnShowing(ref -> AddBookBtn.setDisable(true));
		}catch (IOException ignored){
			AddBookBtn.setDisable(false);
		}

	}

	@SuppressWarnings("unchecked")
	public void DetailsButton(){
		TableView<EvBook> table = (TableView<EvBook>) MainPane.getCenter();
		EvBook selected = table.getFocusModel().getFocusedItem();
		if(selected == null) return; //Not selected

		BorderPane pane = new BorderPane();

		GridPane detPane = new GridPane();
		detPane.setVgap(20);
		detPane.setAlignment(Pos.CENTER);
		detPane.add(new Label("In possesso dal: "), 0, 0);
		detPane.add(new Label(selected.getOwnDate() == null? "(indefinito)" : selected.getOwnDate()), 1, 0);
		detPane.add(new Label("Formato pagine: "), 0, 1);
		detPane.add(new Label(selected.getPagesFormat() == null? "(indefinito)" : selected.getPagesFormat()), 1, 1);
		detPane.add(new Label("Titolo originale: "), 0, 2);
		detPane.add(new Label(selected.getOriginal() == null? "(indefinito)" : selected.getOriginal()), 1, 2);
		detPane.add(new Label("Nazione: "), 0, 3);
		detPane.add(new Label(selected.getCountry() == null? "(indefinito)" : selected.getCountry()), 1, 3);
		detPane.add(new Label("Collana: "), 0, 4);
		detPane.add(new Label(selected.getSeries() == null? "(indefinito)" : selected.getSeries()), 1, 4);
		detPane.add(new Label("Scaffale: "), 0, 5);
		detPane.add(new Label(selected.getShelf() == null? "(indefinito)" : selected.getShelf()), 1, 5);
		detPane.add(new Label("Commento: "), 0, 6);
		try{
			detPane.add(new Label(selected.getComments().isEmpty()? "(indefinito)" : selected.getComments()), 1, 6);
		}catch (NullPointerException npe){
			detPane.add(new Label("(indefinito)"), 1, 6);
		}
		detPane.setPadding(new Insets(10, 25, 10, 25));
		int n = 0;
		for(Node i : detPane.getChildren()){
			if(i.getClass() == Label.class){
				if(n%2 == 0){
					((Label)i).setTextFill(Paint.valueOf("black")); //Even
					((Label)i).fontProperty().set(new Font(14));
				}else{
 					((Label)i).setTextFill(Paint.valueOf("green")); //Odd
					((Label)i).fontProperty().set(new Font(14));
				}
				++n;
			}
		}
		pane.setCenter(detPane);
		Scene scene = new Scene(pane, 640, 480);
		Stage stage = new Stage();
		stage.setOnShown(i -> DetailBtn.setDisable(true));
		stage.setOnHidden(i -> DetailBtn.setDisable(false));
		stage.setResizable(false);
		stage.setMaximized(false);
		stage.centerOnScreen();
		stage.setAlwaysOnTop(true);
		stage.setScene(scene);
		stage.show();
	}


	//Flag
	private static boolean alreadyFiltered = false;
	@FXML @SuppressWarnings("unchecked")
	public void FilterBook(){
		TableView<EvBook> table = (TableView<EvBook>) MainPane.getCenter();
		if(!alreadyFiltered){
			try{
				ArrayList<EvBook> books = MicrosoftDB.connectAndSearch(CodeSearchTextBox.getText(), TitleSearchTextBox.getText(), YearSearchTextBox.getText(), AuthorSearchTextBox.getText(), workingDB);
				if(books != null) { //Prevent throwing unuseful exception
					if(!books.isEmpty()){
						table.getItems().clear(); //Clear the table
						alreadyFiltered = true; //Set flag and lock interface
						FilterBookBtn.setText("Reset");
						CodeSearchTextBox.setDisable(true);
						TitleSearchTextBox.setDisable(true);
						AuthorSearchTextBox.setDisable(true);
						YearSearchTextBox.setDisable(true);
						table.getItems().addAll(books);
					}else new Alert(Alert.AlertType.ERROR, "La ricerca non ha prodotto risultati validi.", ButtonType.CLOSE).showAndWait();
				}
			}catch (Exception ignored){
				new Alert(Alert.AlertType.ERROR, "Parametri di ricerca non corretti", ButtonType.CLOSE).showAndWait();
			}

		}else{
			alreadyFiltered = false; //Reset flag and unlock interface
			FilterBookBtn.setText("Applica Filtro");
			CodeSearchTextBox.setDisable(false);
			TitleSearchTextBox.setDisable(false);
			AuthorSearchTextBox.setDisable(false);
			YearSearchTextBox.setDisable(false);
			((TableView<EvBook>) MainPane.getCenter()).getItems().addAll(MicrosoftDB.connectAndGetNew(workingDB)); //Reload db
		}

	}

	@SuppressWarnings("unchecked")
	public void LeaseButton(){
		LeasingDetailedG.setDbFile(workingDB);
		TableView<EvBook> table = (TableView<EvBook>) MainPane.getCenter();
		if(table.getFocusModel().getFocusedItem() != null){
			LeasingDetailedG.setSelected(table.getFocusModel().getFocusedItem());
			try{
				Parent lease = FXMLLoader.load(Objects.requireNonNull(LeasingDetailedG.class.getResource("leasingDetailed.fxml")));
				Scene scene = new Scene(lease);
				Stage stage = new Stage();
				stage.setScene(scene);
				stage.setResizable(false);
				stage.centerOnScreen();
				stage.initStyle(StageStyle.UNDECORATED);
				stage.show();
				stage.setOnHiding(i -> {
					((TableView<EvBook>) MainPane.getCenter()).getItems().clear();
					((TableView<EvBook>) MainPane.getCenter()).getItems().addAll(MicrosoftDB.connectAndGetNew(workingDB)); //Reload db
				});
			}catch (IOException ignored){ }
		}
	}
}
