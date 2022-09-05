/* ========================================
 * Author: Cristian Capraro
 * September 2022
 * This is controller class of
 * oldDeskDetails.fxml
 * ======================================== */

package it.books.gcon;

import it.books.MicrosoftDB;
import it.books.base.Book;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OldDeskDetailG {
	//Use this to get required share between G classes information
	@FXML
	BorderPane MainPane;

	private static TableView<Book> copy;

	private static List<Book> clonedBook = null;

	private static File ldFile;

	/**
	 * This method is used to share information between controller classes.
	 * @param rdFile specify the database file on which actually works.
	 * **/
	static void getDBFile(File rdFile){
		ldFile = rdFile;
	}
	@FXML
	private void initialize(){
	//Wait table rendering
		Task<Void> setTable = new Task<>() {
			@Override @SuppressWarnings("unchecked")
			protected Void call() throws Exception {
				Thread.sleep(512);
				copy = (TableView<Book>) MainPane.getCenter();

				copy.setOnMouseClicked(i -> {
					Book selected;
					if((selected = copy.getFocusModel().getFocusedItem()) != null){
						CodeTextBox.setText(selected.getCode());
						AuthorsTextBox.setText(selected.getAuthor());
						TitleTextBox.setText(selected.getTitle());
						EditionTextBox.setText(selected.getEdition());
						EditorTextBox.setText(selected.getEditor());
						CommentTextArea.setText(selected.getComments());
						GenreTextBox.setText(selected.getGenre());
						YearTextBox.setText(selected.getYear());
						PagesTextBox.setText(String.valueOf(selected.getPages()));
						SeriesTextBox.setText(selected.getSeries());
						OrigTitleTextBox.setText(selected.getOriginalTitle());
						CountryTextBox.setText(selected.getNation());
						PagesFormatTextBox.setText(selected.getPageFormat());
						TranslatorTextBox.setText(selected.getTranslator());
					}else{
						CodeTextBox.setText("");
						AuthorsTextBox.setText("");
						TitleTextBox.setText("");
						EditionTextBox.setText("");
						EditorTextBox.setText("");
						CommentTextArea.setText("");
						GenreTextBox.setText("");
						YearTextBox.setText("");
						PagesTextBox.setText("");
						SeriesTextBox.setText("");
						OrigTitleTextBox.setText("");
						CountryTextBox.setText("");
						PagesFormatTextBox.setText("");
						TranslatorTextBox.setText("");
					}
				});

				return null;
			}
		};
		new Thread(setTable).start();
	}

	//Tab #1
	@FXML
	TextField CodeTextBox, AuthorsTextBox, TitleTextBox, EditorTextBox, GenreTextBox, YearTextBox, PagesTextBox, EditionTextBox;
	//Tab #2
	@FXML
	TextArea CommentTextArea;
	@FXML
	TextField SeriesTextBox, OrigTitleTextBox, CountryTextBox, PagesFormatTextBox, TranslatorTextBox;
	//Tab #3
	@FXML
	TextField TitleSearchTextBox, CodeSearchTextBox, GenreSearchTextBox, AuthorSearchTextBox;

	@FXML
	@SuppressWarnings("unchecked")
	private void SearchBtn(){
		TableView<Book> table = (TableView<Book>) MainPane.getCenter();

		if(clonedBook == null){
			clonedBook = new ArrayList<>(table.getItems().stream().toList());
		}
		table.getItems().remove(0, table.getItems().size()); //Remove all
		//Search params
		ArrayList<Book> books = MicrosoftDB.connectAndSearch(ldFile, TitleSearchTextBox.getText(), CodeSearchTextBox.getText(), AuthorSearchTextBox.getText(), GenreSearchTextBox.getText());
		if(books == null){
			new Alert(Alert.AlertType.WARNING, "Nessun libro corrisponde ai criteri di ricerca. ", ButtonType.OK).showAndWait();
		}else{
			table.getItems().addAll(books);
		}
	}

	@FXML @SuppressWarnings("unchecked")
	private void ResetBtn(){
		if(!(clonedBook == null)){
			TableView<Book> table = (TableView<Book>) MainPane.getCenter();
			table.getItems().remove(0, table.getItems().size());
			table.getItems().addAll(clonedBook);
			clonedBook = null;
			System.gc();
		}
	}

}
