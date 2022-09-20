package it.books.gcon;

import it.books.MicrosoftDB;
import it.books.base.EvBook;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.Optional;

public class NewDeskDetailG {
	//Get DB file path
	private static File workingDB = null;

	/** Method used to share data between classes, it will be used to
	 * get the working database path **/
	public static void setWorkingDB(File dbFile) {
		workingDB = dbFile;
	}

	//On init
	@FXML @SuppressWarnings("unchecked")
	private void initialize(){
		Task<Void> tableSet = new Task<Void>() {
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
	Button AddBookBtn, RemBookBtn, ModBookBtn, DetailBtn, LeaseBtn;
	@FXML
	TextField TitleTextBox, AuthorsTextBox, GenreTextBox, YearTextBox, EditionTextBox, PageTextBox, CodeTextBox;
	@FXML
	TextField TitleSearchTextBox, AuthorSearchTextBox, YearSearchTextBox, CodeSearchTextBox;
	@FXML
	ComboBox<String> GenreComboBox;

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
				MicrosoftDB.DeleteEntry(workingDB, (int) selected.getId(), selected.getCode(), selected.getTitle());
			}else return;
		});
	}

	@FXML
	public void ModifyBookButton(){

	}

	public void AddBookButton() {

	}

	@SuppressWarnings("unchecked")
	public void DetailsButton(){
		TableView<EvBook> table = (TableView<EvBook>) MainPane.getCenter();
		EvBook selected = table.getFocusModel().getFocusedItem();
		if(selected == null) return; //Not selected

		BorderPane pane = new BorderPane();

		GridPane detPane = new GridPane();
		detPane.add(new Label("In possesso dal: "), 0, 0);
		detPane.add(new Label(selected.getOwnDate()), 1, 0);
		detPane.add(new Label("Formato pagine: "), 0, 1);
		detPane.add(new Label(selected.getPagesFormat()), 1, 1);
		detPane.add(new Label("Titolo originale: "), 0, 2);
		detPane.add(new Label(selected.getOriginal()), 1, 2);
		detPane.add(new Label("Nazione: "), 0, 3);
		detPane.add(new Label(selected.getCountry()), 1, 3);
		detPane.add(new Label("Collana: "), 0, 4);
		detPane.add(new Label(selected.getSeries()), 1, 4);
		detPane.add(new Label("Scaffale: "), 0, 5);
		detPane.add(new Label(selected.getShelf()), 1, 5);
		detPane.setAlignment(Pos.TOP_CENTER);
		detPane.setPadding(new Insets(0, 25, 0, 25));
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
		Scene scene = new Scene(pane, 300, 150);
		Stage stage = new Stage();
		stage.setOnShown(i -> DetailBtn.setDisable(true));
		stage.setOnHidden(i -> DetailBtn.setDisable(false));
		stage.setResizable(false);
		stage.setMaximized(false);
		stage.centerOnScreen();
		stage.setScene(scene);
		stage.show();


	}
}
