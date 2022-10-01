/* =================================================== *
 * Author: Cristian Capraro
 * Date: 27-09-2022
 * First version: 1.0.6 (pre)
 * BookShelf project - sources
 *
 * This is another graphic controller class for the
 * leasingDetailed.fxml file.
 * ================================================== */

//TODO: Recheck this class and add it to the main graphic.

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
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

public class LeasingDetailedG {
	private static File dbFile;
	private static EvBook selected;

	//Used to share data between classes
	public static void setDbFile(File dbIn){
		dbFile = dbIn;
	}

	public static void setSelected(EvBook selected) {
		LeasingDetailedG.selected = selected;
	}

	@FXML
	AnchorPane MainPane;
	@FXML
	Button CloseBtn, UnlockBtn, SubmitBtn;
	@FXML
	TextField CodeAndTitle, Name, Surname, TelBox;
	@FXML
	DatePicker BeginLease, EndLease;

	//Init
	@FXML
	public void initialize(){
		CodeAndTitle.setText(selected.getCode());
		BeginLease.setValue(LocalDate.now());
		BeginLease.chronologyProperty().set(Chronology.ofLocale(Locale.ITALY));
		EndLease.setValue(LocalDate.now().plus(1, ChronoUnit.MONTHS));
		if(selected.isLeasing()){
			Name.setEditable(false);
			Name.setText(selected.leasedTo().split(";")[0]);
			Surname.setEditable(false);
			Surname.setText(selected.leasedTo().split(";")[1]);
			TelBox.setEditable(false);
			TelBox.setText(selected.leasedTo().split(";")[2]);
			BeginLease.setEditable(false);
			BeginLease.setValue(LocalDate.of(Integer.parseInt(selected.getBeginDate().split("-")[2]), Integer.parseInt(selected.getBeginDate().split("-")[1]), Integer.parseInt(selected.getBeginDate().split("-")[0])));
			EndLease.setEditable(false);
			EndLease.setValue(LocalDate.of(Integer.parseInt(selected.getEndDate().split("-")[2]), Integer.parseInt(selected.getEndDate().split("-")[1]), Integer.parseInt(selected.getEndDate().split("-")[0])));

		}
	}

	//Actions
	public void Close(){
		MainPane.getScene().getWindow().hide();
	}

	public void Submit(){ //Add leasing
		//Check unlocking
		Predicate<TextField> isEmpty = (i) -> i.getText().isEmpty();
		if(isEmpty.test(Name)||isEmpty.test(Surname)||isEmpty.test(TelBox)) {
			new Alert(Alert.AlertType.ERROR, "Nome, Cognome e Recapito\n del soggetto sono necessari.", ButtonType.OK).showAndWait();
			return;
		}
		if (!isEmpty.test(CodeAndTitle)) {
			try {
				EvBook book = Objects.requireNonNull(MicrosoftDB.connectAndSearch(dbFile, CodeAndTitle.getText()));
				book.setLease(BeginLease.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), EndLease.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), Name.getText(), Surname.getText(), TelBox.getText());
				MicrosoftDB.DeleteEntry(dbFile, -1, book.getCode(), book.getTitle());
				MicrosoftDB.connectAndUploadANewBook(dbFile, book);
				new Alert(Alert.AlertType.INFORMATION, "Prestito aggiunto", ButtonType.OK).showAndWait().ifPresentOrElse(i -> {MainPane.getScene().getWindow().hide();}, () -> {MainPane.getScene().getWindow().hide();});
			} catch (NullPointerException e) {
				new Alert(Alert.AlertType.ERROR, "Errore: Impossibile aggiornare il database.", ButtonType.OK).showAndWait();
			}
		}
	}

	public void Unlock(){ //Remove leasing
		selected.endLease();
		MicrosoftDB.DeleteEntry(dbFile, -1, selected.getCode() , selected.getTitle());
		MicrosoftDB.connectAndUploadANewBook(dbFile, selected);
		MainPane.getScene().getWindow().hide();
	}
}
