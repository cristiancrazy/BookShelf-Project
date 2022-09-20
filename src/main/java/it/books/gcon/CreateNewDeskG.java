/* ========================================
 * Author: Cristian Capraro
 * September 2022
 * This is controller class of
 * createNewDesk.fxml
 * - init a new workspace
 * ======================================== */

package it.books.gcon;

import it.books.Main;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class CreateNewDeskG {

	@FXML
	private void initialize(){
		TypeComboBox.getItems().add(0, "Microsoft Access Database - locale (.accdb)");
		//TypeComboBox.getItems().add(1, "Database di SQLLite - locale");
	}

	@FXML
	AnchorPane MainPane;
	@FXML
	private ComboBox<String> TypeComboBox;
	@FXML
	private TextField NameTextBox;
	@FXML
	private CheckBox DefaultLocationCheckBox;

	@FXML
	private void CreateButton(){
		//Checks 1 -> DB name not empty
		String dbName;
		if(!(dbName = NameTextBox.getText()).isEmpty()){
			//Check 2 -> Simple Dbname
			if(false){
				new Alert(Alert.AlertType.ERROR, "Il nome non pu\u00F3 contenere caratteri accentati, speciali o segni di interpunzione.", ButtonType.OK).showAndWait();
				return;
			}

			//Check 3 -> Selected type
			switch (TypeComboBox.selectionModelProperty().get().getSelectedIndex()) {
				case 0 -> { //Microsoft Access DB
					//Check 4 -> Default Location
					Path DBPath = null;
					if (DefaultLocationCheckBox.isSelected()) {
						DBPath = Path.of("." + File.pathSeparator, "MS_DATABASE", dbName);
						if (DBPath.toFile().exists()) {
							new Alert(Alert.AlertType.ERROR, "Esiste gi\u00E0 un database con il nome fornito!", ButtonType.OK).showAndWait();
							return;
						}
					} else { //No default location
						DirectoryChooser dChooser = new DirectoryChooser();
						File MDDir = dChooser.showDialog(MainPane.getScene().getWindow());
						if (MDDir == null) { //Check 5B -> Create a db
							new Alert(Alert.AlertType.ERROR, "Specificare il percorso del database!", ButtonType.OK).showAndWait();
							return;
						} else {
							DBPath = MDDir.toPath().resolve(dbName);
						}
					}
					//Making directory which store database
					if (DBPath.toFile().mkdir()) {
						//Create .ini and accdb file
						try {
							File ini;
							(ini = DBPath.resolve("config.ini").toFile()).createNewFile();
							//Write INI File
							Wini configFile = new Wini(ini);
							configFile.put("BookShelf App", "Version", Main.getVersion());
							configFile.put("BookShelf App", "Database", "NEW_ACCDB");
							configFile.put("BookShelf App", "Created on", LocalDateTime.now());
							configFile.put("BookShelf App", "Status", "INIT_REQUIRED");
							configFile.store();

							DBPath.resolve(dbName + ".accdb").toFile().createNewFile();
							new Alert(Alert.AlertType.CONFIRMATION, "Il Database \u00E9 stato creato con successo!\n" + DBPath.resolve(dbName + ".accdb").toFile().getAbsolutePath(), ButtonType.OK).showAndWait();
						} catch (IOException ignored) {
							return;
						}

					} else {
						new Alert(Alert.AlertType.ERROR, "Impossibile creare il database nella cartella selezionata!", ButtonType.OK).showAndWait();
						return;
					}
				}
				case 1 -> { //SQLLite database
					new Alert(Alert.AlertType.WARNING, "Non \u00E9 possibile creare un database SQLLite in questa versione di BookShelf.", ButtonType.OK).showAndWait();
					return;
				}
				default -> {
					new Alert(Alert.AlertType.ERROR, "Selezionare la tipologia di database da creare!", ButtonType.OK).showAndWait();
					return;
				}
			}


		}else{
			new Alert(Alert.AlertType.ERROR, "\u00C9 obbligatorio indicare un nome per il nuovo database.", ButtonType.OK).showAndWait();
			return;
		}
		MainPane.getScene().getWindow().hide(); //Hide the config window
	}

	@FXML
	private void AbortButton() {
		MainPane.getScene().getWindow().hide(); //Hide the config window
	}

}