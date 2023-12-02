package com.example.gt;

import Tools.Erreur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.HorraireModel;

import java.io.IOException;
import java.sql.*;
public class GestionHorraireController {

	Connection conn = Connexion.getConnection();

    @FXML
    private TextField heureDebutField;

    @FXML
    private TableColumn<HorraireModel, Integer> heureDubutColumn;

    @FXML
    private TableColumn<HorraireModel,Integer > heureFinColumn;

    @FXML
    private TextField heureFinField;

    @FXML
    private TableColumn<HorraireModel, Integer> numEquipeColumn;

    @FXML
    private TextField numEquipeField;

    @FXML
    private TableColumn<HorraireModel,Boolean> selectCheckbox;


    @FXML
    private TableView<HorraireModel> tableHorraire;
    @SuppressWarnings("unchecked")
	public void initialize() {
		tableHorraire.setEditable(true);
//		create the table columns
		selectCheckbox.setCellFactory(CheckBoxTableCell.forTableColumn(selectCheckbox));
		selectCheckbox.setCellValueFactory(new PropertyValueFactory<>("selected"));
		numEquipeColumn.setCellValueFactory(new PropertyValueFactory<>("numEquipe"));
		heureDubutColumn.setCellValueFactory(new PropertyValueFactory<>("heureDeb"));
		heureFinColumn.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
		tableHorraire.getColumns().setAll(numEquipeColumn,heureDubutColumn,heureFinColumn,selectCheckbox);
		ObservableList<HorraireModel> data = fetchDataFromDatabase();
        // Set the data in the TableView
        tableHorraire.setItems(data);

	}
    private ObservableList<HorraireModel> fetchDataFromDatabase() {
        ObservableList<HorraireModel> data = FXCollections.observableArrayList();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM Equipes");
            while (rs.next()) {
				Integer numEquipe = rs.getInt(4);
				Integer heureDeb = rs.getInt(2);
				Integer heureFin = rs.getInt(3);
                HorraireModel horraire = new HorraireModel(numEquipe,heureDeb,heureFin);
                data.add(horraire);
            }
            st.close();
        }catch (SQLException e) {
            Erreur.databaseProblem();
        }
        return data;
    }
    @FXML
    void ajouterAEquipe(ActionEvent event) {
		if (numEquipeField.getText().isEmpty()|| heureDebutField.getText().isEmpty() || heureFinField.getText().isEmpty()) {
			Erreur.msgEmptyfields();
		}else if (!isNumeric(numEquipeField.getText())) {
			Erreur.msgNotNumeric("Numéro d'equipe");
		}else if (!isNumeric(heureDebutField.getText())) {
			Erreur.msgNotNumeric("Heure Début");
		}else if (!isNumeric(heureFinField.getText())) {
			Erreur.msgNotNumeric("Heure Fin");
		}else if (Integer.parseInt(heureDebutField.getText()) >= Integer.parseInt(heureFinField.getText())) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Invalid input!");
			alert.setContentText( "il faut que l'heure Debut doit être superieur à l'heure Fin");
			alert.showAndWait();
		}else {
			try {
				PreparedStatement psCheck = conn.prepareStatement("select * from Equipes where numEquipe = ?");
				psCheck.setInt(1, Integer.parseInt(numEquipeField.getText()));
				ResultSet rs = psCheck.executeQuery();
				if (rs.next()) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("Invalid input!");
					alert.setContentText( "le numero de l'equipe que vous avez donneé est déjà utiliser\n");
					alert.showAndWait();
				}else{
					PreparedStatement ps2 = conn.prepareStatement("insert into Equipes(heureDeb,heureFin,numEquipe)\r\n"
							+ "values (?,?,?)");

					ps2.setInt(1, Integer.parseInt(heureDebutField.getText()));
					ps2.setInt(2, Integer.parseInt(heureFinField.getText()));
					ps2.setInt(3, Integer.parseInt(numEquipeField.getText()));
					ps2.executeUpdate();
					clearInputFields();

					tableHorraire.getItems().removeAll(tableHorraire.getItems());
					initialize();
				}

			}catch (SQLException e) {
				Erreur.databaseProblem();
				e.printStackTrace();
			}
		}
    }
    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private void clearInputFields() {
        numEquipeField.setText("");
        heureDebutField.setText("");
        heureFinField.setText("");
    }
    @FXML
    void fermer(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(scene);
			stage.show();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @FXML
    void supprimerHorraire(ActionEvent event) {
		ObservableList<HorraireModel> selectedItems = tableHorraire.getItems().filtered(HorraireModel::isSelected);
		try {
			if (selectedItems.size() == 1) {
				int numequipe = selectedItems.get(0).getNumEquipe();
				PreparedStatement psCheck = conn.prepareStatement("select * from Equipes,Semaines\r\n"
						+ "where Semaines.idEquipe = Equipes.idEquipe\r\n"
						+ "and numEquipe = ?");
				psCheck.setInt(1, numequipe);
				ResultSet rsCheck = psCheck.executeQuery();
				if (!rsCheck.next()) {
					PreparedStatement st = conn.prepareStatement("DELETE FROM Equipes WHERE numEquipe = ?");
					st.setInt(1, numequipe);
					st.executeUpdate();
					// Optional: Refresh the TableView after the operation
					tableHorraire.getItems().removeAll(selectedItems);
				}else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("Erreur de suppression");
					alert.setContentText( "cette horraire est déjà associé à des personne \n");
					alert.showAndWait();
				}
			}else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("Invalid input!");
				alert.setContentText( "il faut choisie dans chaque suppression une seule horraire\n");
				alert.showAndWait();
			}
		} catch (SQLException e) {
			Erreur.databaseProblem();
			e.printStackTrace();
		}
		initialize();
    }

	@FXML
	void openGestionArrete(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("gestionArrete.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(scene);
			stage.show();
			stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@FXML
	void openGestionEquipe(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("gestionEquipesRespoGT.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(scene);
			stage.show();
			stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void openGestionHorraire(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("gestionEquipeHorraire.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(scene);
			stage.show();
			stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void openGestionPerso(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("gestionPerso.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(scene);
			stage.show();
			stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void openGestionPrime(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("gestionPrime.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(scene);
			stage.show();
			stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void openGestionCompte(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("GestionCompte.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(scene);
			stage.show();
			stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void openImprimerEquipe(ActionEvent event){
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("imprimer.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(scene);
			stage.show();
			stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void openArchive(ActionEvent event){
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Archive.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.setScene(scene);
			stage.show();
			stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
