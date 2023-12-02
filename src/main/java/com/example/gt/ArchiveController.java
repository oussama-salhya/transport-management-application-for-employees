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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.PersoModel;

import java.io.IOException;
import java.sql.*;
public class ArchiveController {
	Connection conn = Connexion.getConnection();

	@FXML
	private TableColumn<PersoModel, String> arreteColumn;

	@FXML
	private ChoiceBox<Integer> equipeChoiceBox;
	@FXML
	private ChoiceBox<Integer> anneeChoiceBox;

	@FXML
	private TableColumn<PersoModel, String> nomColumn;

	@FXML
	private TableColumn<PersoModel, String> numMatColumn;

	@FXML
	private TableColumn<PersoModel, String> sectionColumn;

	@FXML
	private TableColumn<PersoModel, String> prenomColumn;


	@FXML
	private ChoiceBox<Integer> semChoiceBox;


	@FXML
	private TableView<PersoModel> tablePerso;
	@SuppressWarnings("unchecked")
	public void initialize() {
		tablePerso.setEditable(true);
//		create the table columns
		numMatColumn.setCellValueFactory(new PropertyValueFactory<>("numMat"));
		nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
		prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
		arreteColumn.setCellValueFactory(new PropertyValueFactory<>("arrete"));
		sectionColumn.setCellValueFactory(new PropertyValueFactory<>("section"));
		tablePerso.getColumns().setAll(numMatColumn,nomColumn,prenomColumn,arreteColumn,sectionColumn);

		ObservableList<Integer> equipe = FXCollections.observableArrayList();
		ObservableList<Integer> semaine = FXCollections.observableArrayList();
		ObservableList<Integer> annee = FXCollections.observableArrayList();
		try {

			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select numEquipe from Equipes ");

			while (rs.next()) {
				equipe.add(rs.getInt(1));
			}
			equipeChoiceBox.setItems(equipe);

			rs = st.executeQuery("select distinct numSemaine from Semaines ");

			while (rs.next()) {
				semaine.add(rs.getInt(1));
			}
			semChoiceBox.setItems(semaine);

			rs = st.executeQuery("select distinct annee from Semaines ");

			while (rs.next()) {
				annee.add(rs.getInt(1));
			}
			anneeChoiceBox.setItems(annee);

		} catch (Exception e) {
			Erreur.databaseProblem();
		}

	}
	private ObservableList<PersoModel> fetchDataFromDatabase(int sem,int annee, int equipe) {
		ObservableList<PersoModel> data = FXCollections.observableArrayList();
		try {
			PreparedStatement ps = conn.prepareStatement("select numMatricule,Personnes.nom,prenom,Arretes.nom,atelier\r\n"
					+ "from Personnes,Arretes,Chefs,Semaines,Equipes\r\n"
					+ "where Personnes.idArrete = Arretes.idArrete \r\n"
					+ "and Personnes.idChef =Chefs.idChef\r\n"
					+ "and Semaines.idPersonne = Personnes.idPersonne\r\n"
					+ "and Semaines.idEquipe = Equipes.idEquipe\r\n"
					+ "and numSemaine =  ? \r\n"
					+ "and numEquipe = ? \r\n"
					+ "and annee = ? ");
			ps.setInt(1,sem);
			ps.setInt(2,equipe);
			ps.setInt(3,annee);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String numMat = rs.getString(1);
				String nom = rs.getString(2);
				String prenom = rs.getString(3);
				String arrete = rs.getString(4);
				String section = rs.getString(5);
				PersoModel perso = new PersoModel(numMat,nom,prenom,arrete,"",section);
				data.add(perso);
			}
		}catch (SQLException e) {
			Erreur.databaseProblem();
			e.printStackTrace();
		}
		return data;
	}
	@FXML
	void choixSem(ActionEvent event) {
		if (anneeChoiceBox.getValue() == null ||semChoiceBox.getValue() == null ||equipeChoiceBox.getValue() == null ) {
			Erreur.msgEmptyfields();
		}
		else {
			ObservableList<PersoModel> data = fetchDataFromDatabase(semChoiceBox.getValue(),anneeChoiceBox.getValue(),
					equipeChoiceBox.getValue());
			// Set the data in the TableView
			tablePerso.setItems(data);

		}
	}

	private void clearInputFields() {
		semChoiceBox.getSelectionModel().clearSelection();
		equipeChoiceBox.getSelectionModel().clearSelection();
		anneeChoiceBox.getSelectionModel().clearSelection();
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
	@FXML
	void openArchivePrime(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ArchivegestionPrime.fxml"));
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
