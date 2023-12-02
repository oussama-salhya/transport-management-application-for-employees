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
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.PersoModel;

import java.io.IOException;
import java.sql.*;
public class SuppPersoEquipesController {
	Connection conn = Connexion.getConnection();

    @FXML
    private TableColumn<PersoModel, String> arreteColumn;

    @FXML
    private ChoiceBox<Integer> equipeChoiceBox;

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
    private TableColumn<PersoModel, Boolean> selectCheckbox;


    @FXML
    private TableView<PersoModel> tablePerso;
    @SuppressWarnings("unchecked")
	public void initialize() {
		tablePerso.setEditable(true);
//		create the table columns
		selectCheckbox.setCellFactory(CheckBoxTableCell.forTableColumn(selectCheckbox));
		selectCheckbox.setCellValueFactory(new PropertyValueFactory<>("selected"));
		numMatColumn.setCellValueFactory(new PropertyValueFactory<>("numMat"));
		nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
		prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
		arreteColumn.setCellValueFactory(new PropertyValueFactory<>("arrete"));
		sectionColumn.setCellValueFactory(new PropertyValueFactory<>("section"));
		tablePerso.getColumns().setAll(numMatColumn,nomColumn,prenomColumn,arreteColumn,sectionColumn,selectCheckbox);
		
        ObservableList<Integer> equipe = FXCollections.observableArrayList();
        ObservableList<Integer> semaine = FXCollections.observableArrayList();
        try {
			
        	Statement st = conn.createStatement();
        	ResultSet rs = st.executeQuery("select numEquipe from Equipes ");
        	
        	while (rs.next()) {
        		equipe.add(rs.getInt(1));
        	}
        	equipeChoiceBox.setItems(equipe);
        	
        	rs = st.executeQuery("select distinct numSemaine from Semaines ");
        	int lastWeek = 999;
        	while (rs.next()) {
        		lastWeek = rs.getInt(1);
        		semaine.add(lastWeek);
        	}
        	semChoiceBox.setItems(semaine);
        	semChoiceBox.setValue(lastWeek);
        	
		} catch (Exception e) {
			Erreur.databaseProblem();
		}
        equipeChoiceBox.setOnAction(this::choixSem);
        semChoiceBox.setOnAction(this::choixSem);
        
	}
    private ObservableList<PersoModel> fetchDataFromDatabase(int sem,int annee, int equipe) {
        ObservableList<PersoModel> data = FXCollections.observableArrayList();
        try {
        	PreparedStatement ps = conn.prepareStatement("select numMatricule,Personnes.nom,prenom,Arretes.nom,atelier\r\n"
        			+ "from Personnes,Arretes,Chefs,Semaines,Equipes\r\n"
        			+ "where Personnes.idArrete = Arretes.idArrete \r\n"
        			+ "and Chefs.idChef = Personnes.idChef\r\n"
        			+ "and Semaines.idPersonne = Personnes.idPersonne\r\n"
        			+ "and Semaines.idEquipe = Equipes.idEquipe\r\n"
        			+ "and numSemaine =  ? \r\n"
        			+ "and numEquipe = ? \r\n"
        			+ "and annee = ? and confirme ='OUI'");
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
    void choixSem(ActionEvent event) {
    	if (equipeChoiceBox.getValue() == null ) {
    		Erreur.msgEmptyfields();
		}
    	else {
            	ObservableList<PersoModel> data = fetchDataFromDatabase(semChoiceBox.getValue(),Main.yearNumber,
            			equipeChoiceBox.getValue());
            	// Set the data in the TableView
            	tablePerso.setItems(data);
				
			}
    }
    
    @FXML
    void supprimerPerso(ActionEvent event) {
    	ObservableList<PersoModel> selectedItems = tablePerso.getItems().filtered(PersoModel::isSelected);
    	
		try {
			for (PersoModel perso : selectedItems) {
			PreparedStatement ps2 = conn.prepareStatement("select idPersonne from Personnes where numMatricule = ?");
			ResultSet rs2;
			String numMat = perso.getNumMat();
				int idperso = 0;
				ps2.setString(1, numMat);
				rs2 = ps2.executeQuery();
				PreparedStatement ps1 = conn.prepareStatement("select idEquipe from Equipes where numEquipe = ?");
				ResultSet rs1;
				int idequipe = 0;
				ps1.setInt(1, equipeChoiceBox.getValue());
				rs1 = ps1.executeQuery();
				if (rs1.next()) {
					idequipe = rs1.getInt(1);
				}
				if (rs2.next()) {
					idperso = rs2.getInt(1);
					
					PreparedStatement st = conn.prepareStatement("update Semaines\r\n"
							+ "set \r\n"
							+ "confirme = 'NON'\r\n"
							+ "where idEquipe = ?\r\n"
							+ "and idPersonne = ? \r\n"
							+ "and numSemaine = ? and annee = ?");
					st.setInt(1, idequipe);
					st.setInt(2, idperso);
					st.setInt(3, semChoiceBox.getValue());
					st.setInt(4, Main.yearNumber);
					
					st.executeUpdate();
					}
			}
				ObservableList<PersoModel> data = fetchDataFromDatabase(semChoiceBox.getValue(),Main.yearNumber,
						equipeChoiceBox.getValue());
				// Set the data in the TableView
				tablePerso.setItems(data);
		} catch (SQLException e) {
			Erreur.databaseProblem();
			e.printStackTrace();
		}
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
}
