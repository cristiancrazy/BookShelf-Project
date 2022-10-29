/* =================================================
 * Author: Cristian Capraro
 * Personal Project under MIT Licence
 * This class is the graphic controller to work on
 * new Microsoft database specific format -
 * This is the controller of add book form.
 * ================================================= */


package it.books.gcon;

import it.books.MicrosoftDB;
import it.books.base.EvBook;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AddBookFormG {
	private static File workingDB = null;
	public static TableView<EvBook> parent;

	/** Method used to share data between classes, it will be used to
	 * get the working database path **/
	public static void setWorkingDB(File dbFile) {
		workingDB = dbFile;
	}

	/** Method used to share data between classes, it will be used to
	 * get the parent tableview to work on **/
	public static void setParent(TableView<EvBook> p){
		parent = p;
	}

	//Graphics Init
	@FXML
	public void initialize(){
		//Set value - locale
		DatePickerData.chronologyProperty().set(Chronology.ofLocale(Locale.ITALIAN));
		DatePickerData.setValue(LocalDate.now());
		IDBookData.setText(""+(MicrosoftDB.connectAndGetLastID(workingDB)+1));
	}

	//FXML Components
	@FXML
	TextField IDBookData, CodeData, TitleData, AuthorsData, OriginalData, GenreData, YearData, EditionData, EditorData, SeriesData, PagesData, FormatData, CountryData, ShelfData;

	@FXML
	TextArea CommentBox;

	@FXML
	DatePicker DatePickerData;
	@FXML
	AnchorPane MainPane;
	//FXML Actions

	//Add
	public void AddToDBBtn(){
		if(TitleData.getText().isEmpty()||CodeData.getText().isEmpty()){
			new Alert(Alert.AlertType.ERROR, "Per registrare un nuovo libro bisogna inserire almeno il TITOLO e il CODICE (o ISBN)", ButtonType.OK).showAndWait();
		}else{
			EvBook newBook = new EvBook(Integer.parseInt(IDBookData.getText()), CodeData.getText(), TitleData.getText(), AuthorsData.getText());
			if(PagesData.getText().isEmpty()) PagesData.setText("0");
			if(YearData.getText().isEmpty()) YearData.setText("0");
			try{
				newBook.addDetails(OriginalData.getText(), GenreData.getText(), YearData.getText(), EditionData.getText(), EditorData.getText() ,SeriesData.getText(), Integer.parseInt(PagesData.getText()), FormatData.getText(), CountryData.getText(), ShelfData.getText(), DatePickerData.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
				newBook.setComments(CommentBox.getText());
				int lastID;
				if((lastID = MicrosoftDB.connectAndUploadANewBook(workingDB, newBook)) != -1){
					EvBook bookFinalized = new EvBook(lastID, newBook.getCode(), newBook.getTitle(), newBook.getAuthors());
					bookFinalized.addDetails(newBook.getOriginal(), newBook.getGenre(), newBook.getYear(), newBook.getEdition(), newBook.getPublisher(), newBook.getSeries(), newBook.getPages(), newBook.getPagesFormat(), newBook.getCountry(), newBook.getShelf(), newBook.getOwnDate());
					bookFinalized.setComments(newBook.getComments());
					parent.getItems().add(bookFinalized);
					new Alert(Alert.AlertType.INFORMATION, "Libro aggiunto al database.", ButtonType.OK).showAndWait();
					MainPane.getScene().getWindow().hide(); //Hide Window
				}else{
					new Alert(Alert.AlertType.ERROR, "Errore con il database.", ButtonType.OK).showAndWait();
				}

			}catch (NumberFormatException e){
				new Alert(Alert.AlertType.ERROR, "Errore!\nSi prega di controllare l'anno di pubblicazione e il numero di pagine.", ButtonType.OK).showAndWait();
			}
		}
	}

	//Abort
	public void CloseBtn(){
		MainPane.getScene().getWindow().hide();
	}

}
