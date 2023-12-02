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
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.PersoModel;

import java.io.IOException;
import java.sql.*;
public class GestionEquipesChefController {
	Connection conn = Connexion.getConnection();
	static String section ;

    @FXML
    private ChoiceBox<Integer> equipeChoiceBox;
    @FXML
    private ChoiceBox<Integer> semChoiceBox;
    
    @FXML
    private TextField sectionField;

    @FXML
    private TableColumn<PersoModel, String> nomColumn;

    @FXML
    private TableColumn<PersoModel, String> numMatColumn;
    
    @FXML
    private TableColumn<PersoModel, String> prenomColumn;

    @FXML
    private TableColumn<PersoModel, Boolean> selectCheckbox;

    @FXML
    private TableView<PersoModel> tablePerso;
    @SuppressWarnings("unchecked")
    
	public void initialize() {
    	sectionField.setText(section);
		tablePerso.setEditable(true);
//		create the table columns
		selectCheckbox.setCellFactory(CheckBoxTableCell.forTableColumn(selectCheckbox));
		selectCheckbox.setCellValueFactory(new PropertyValueFactory<>("selected"));
		numMatColumn.setCellValueFactory(new PropertyValueFactory<>("numMat"));
		nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
		prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
		tablePerso.getColumns().setAll(numMatColumn,nomColumn,prenomColumn,selectCheckbox);
        ObservableList<Integer> equipe = FXCollections.observableArrayList();
        ObservableList<Integer> semaine = FXCollections.observableArrayList();
        
    	
        try {
        	Statement st = conn.createStatement();
        	ResultSet rs = st.executeQuery("select * from Equipes ");
        	
        	while (rs.next()) {
        		equipe.add(rs.getInt(4));
        		equipeChoiceBox.setItems(equipe);
        	}
		} catch (Exception e) {
			Erreur.databaseProblem();
		}
        try {
        	Statement st = conn.createStatement();
        	ResultSet rs = st.executeQuery("select distinct numSemaine from Semaines order by numSemaine asc ");
        	int grandeSem = 999;
        	while (rs.next()) {
        		grandeSem = rs.getInt(1);
        		semaine.add(grandeSem);
        	}
        	int diffSem = Main.weekNumber - grandeSem;
        	for (int i = 1; i <= diffSem; i++) {
				semaine.add(grandeSem + i);
			}
        	semChoiceBox.setItems(semaine);
        	semChoiceBox.setValue(Main.weekNumber);
        } catch (Exception e) {
        	Erreur.databaseProblem();
        }

        semChoiceBox.setOnAction(this::AfficherEquipe);
        ObservableList<PersoModel> data = fetchDataFromDatabase(Main.weekNumber);
		// Set the data in the TableView
		tablePerso.setItems(data);
		
	}
    private ObservableList<PersoModel> fetchDataFromDatabase(int sem) {
        ObservableList<PersoModel> data = FXCollections.observableArrayList();
        try {
        	PreparedStatement ps = conn.prepareStatement("select numMatricule,Personnes.nom,prenom from Personnes,Chefs\r\n"
        			+ "where Personnes.idChef = Chefs.idChef\r\n"
        			+ "and atelier = ?  and prenom != ''\r\n"
        			+ "and Personnes.idPersonne not in\r\n"
        			+ "(select idPersonne from Semaines\r\n"
        			+ "where numSemaine = ?\r\n"
        			+ "and annee = ?)");
        	ps.setString(1, section);
        	ps.setInt(2,sem);
        	ps.setInt(3, Main.yearNumber);
        	ResultSet rs = ps.executeQuery();
        	
            while (rs.next()) {
            	String numMat = rs.getString(1);
            	String nom = rs.getString(2);
            	String prenom = rs.getString(3);
                PersoModel perso = new PersoModel(numMat,nom,prenom,section,0);
                data.add(perso);
            }
        }catch (SQLException e) {
            Erreur.databaseProblem();
            e.printStackTrace();
        }
        return data;
    }
    @FXML
    void ajouterAEquipe(ActionEvent event) {
    	if (equipeChoiceBox.getValue() == null) {
	    	Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid input!");
            alert.setContentText( "il faut selectionner l'équipe\n");
            alert.showAndWait();
		}else {
			int sem = semChoiceBox.getValue();
			int equipe = equipeChoiceBox.getValue();
			inserer(sem,equipe);
			
		}
    }
    void AfficherEquipe(ActionEvent event) {
			int semaine = semChoiceBox.getValue();
			ObservableList<PersoModel> data = fetchDataFromDatabase(semaine);
			// Set the data in the TableView
			tablePerso.setItems(data);
			
    }
    void inserer(int nvSem , int equipe){
    	ObservableList<PersoModel> selectedItems = tablePerso.getItems().filtered(PersoModel::isSelected);
    	try {
		    if (selectedItems.isEmpty()) {
		    	Alert alert = new Alert(AlertType.ERROR);
	            alert.setTitle("Error");
	            alert.setHeaderText("Invalid input!");
	            alert.setContentText( "il faut selectionner au moins une personne\n");
	            alert.showAndWait();
			}else {
				PreparedStatement ps1 = conn.prepareStatement("select idEquipe from Equipes where numEquipe = ?");
				ResultSet rs1;
				int idequipe = 0;
				ps1.setInt(1, equipe);
				rs1 = ps1.executeQuery();
				if (rs1.next()) {
					idequipe = rs1.getInt(1);
				}
				for (PersoModel perso : selectedItems) {

					PreparedStatement ps2 = conn.prepareStatement("select idPersonne from Personnes where numMatricule = ?");
					ResultSet rs2;
					String numMat = perso.getNumMat();
						int idperso = 0;
						ps2.setString(1, numMat);
						rs2 = ps2.executeQuery();
						if (rs2.next()) {
							idperso = rs2.getInt(1);

							PreparedStatement st = conn.prepareStatement("insert into Semaines(idPersonne,idEquipe,numSemaine,prime,description,annee,confirme)\r\n"
									+ "values (?,?,?,0,'',?,'NON')");
							st.setInt(1, idperso);
							st.setInt(2, idequipe);
							st.setInt(3, nvSem);
							st.setInt(4, Main.yearNumber);

							st.executeUpdate();
							ObservableList<PersoModel> data = fetchDataFromDatabase(nvSem);
							// Set the data in the TableView
							tablePerso.setItems(data);

							}

						ps1.close();
						ps2.close();
					}

			}


			}catch (SQLException e) {
			Erreur.databaseProblem();
			e.printStackTrace();
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
    @FXML
    void openArchiveGestionEquipe(ActionEvent event){
    	try {
    		ArchiveGestionEquipesChefController.section = section;
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ArchivegestionEquipesChef.fxml"));
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
    void afficherListPerso(ActionEvent event){
    	try {
    		SuppPersoEquipesChefController.section = section;
    		FXMLLoader loader = new FXMLLoader(getClass().getResource("suppPersoEquipeChef.fxml"));
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

	public void openGestionEquipeChef(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("gestionEquipesChef.fxml"));
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
