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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.PersoModel;

import java.io.IOException;
import java.sql.*;

public class ArchiveGestionPrimeController {

	Connection conn = Connexion.getConnection();
    @FXML
    private TableColumn<PersoModel, String> prenomColumn;

    @FXML
    private TableColumn<PersoModel,Integer> nbPrimeColumn;

    @FXML
    private TableColumn<PersoModel,String> nomColumn;
    @FXML
    private TableColumn<PersoModel,String> descriptionColum;

    @FXML
    private ChoiceBox<Integer> semDebChoiceBox;

    @FXML
    private ChoiceBox<Integer> semFinChoiceBox;

    @FXML
    private TableView<PersoModel> tablePrime;

    @FXML
    private ChoiceBox<String> villeChoiceBox;
    @FXML
    private ChoiceBox<Integer> anneeChoiceBox;

    
    @SuppressWarnings("unchecked")
	public void initialize() {
		tablePrime.setEditable(true);
//		create the table columns
		nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
		prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
		descriptionColum.setCellValueFactory(new PropertyValueFactory<>("description"));
		nbPrimeColumn.setCellValueFactory(new PropertyValueFactory<>("nbPrime"));
		tablePrime.getColumns().setAll(nbPrimeColumn,nomColumn,prenomColumn,descriptionColum);
		
		
        ObservableList<String> ville = FXCollections.observableArrayList();
        ObservableList<Integer> semDeb = FXCollections.observableArrayList();
        ObservableList<Integer> semFin = FXCollections.observableArrayList();
        ObservableList<Integer> annee = FXCollections.observableArrayList();
        try {
			
        	Statement st = conn.createStatement();
        	ResultSet rs = st.executeQuery("select distinct numSemaine from Semaines ");
        	
        	while (rs.next()) {
        		semDeb.add(rs.getInt(1));
        		semFin.add(rs.getInt(1));
        		semDebChoiceBox.setItems(semDeb);
        		semFinChoiceBox.setItems(semFin);
        	}
        	
        	rs = st.executeQuery("select distinct ville from Arretes");
        	
        	while (rs.next()) {
        		ville.add(rs.getString(1));
        		villeChoiceBox.setItems(ville);
        	}
        	
        	rs = st.executeQuery("select distinct annee from Semaines");
        	
        	while (rs.next()) {
        		annee.add(rs.getInt(1));
        		anneeChoiceBox.setItems(annee);
        	}
        	
		} catch (Exception e) {
			Erreur.databaseProblem();
			e.printStackTrace();
		}
        
	}
    private ObservableList<PersoModel> fetchDataFromDatabase() {
    	int semDeb = semDebChoiceBox.getValue();
    	int semFin = semFinChoiceBox.getValue();
    	
    	if (semDeb > semFin) {
    		Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("Error");
            alert.setHeaderText("Invalid input!");
            alert.setContentText( "il faut choisie soit une nouvelle semaine ou une semaine qui a déjà existé\n");
            alert.showAndWait();
		}
    	else {
    		ObservableList<PersoModel> data = FXCollections.observableArrayList();
            try {
            	String descrip = "";
                if (semDeb == semFin) {
                	PreparedStatement ps = conn.prepareStatement("select prime ,Personnes.nom ,prenom,description \r\n"
                			+ "from Personnes,Semaines,Arretes\r\n"
                			+ "where Personnes.idPersonne = Semaines.idPersonne\r\n"
                			+ "and Arretes.idArrete= Personnes.idArrete\r\n"
                			+ "and numSemaine =  ? \r\n"
                			+ "and ville = ? and annee = ? and dispoPrime = 1 "); 
                	
                	ps.setString(2, villeChoiceBox.getValue());
                	ps.setInt(1, semDeb);
                	ps.setInt(3, anneeChoiceBox.getValue());
                	ResultSet rs = ps.executeQuery();
                	
                    while (rs.next()) {
                    	int nbPrime = rs.getInt(1);
                    	String nom = rs.getString(2);
                    	String prenom = rs.getString(3);
                      descrip = rs.getString(4);
                        PersoModel perso = new PersoModel(nom,prenom,nbPrime,descrip);
                        data.add(perso);
                    }

                    return data;
                }else {
                	PreparedStatement ps = conn.prepareStatement(" select SUM(prime) as nb ,Personnes.nom, prenom from Personnes,Arretes,Semaines\r\n"
                			+ "where Personnes.idArrete = Arretes.idArrete\r\n"
                			+ "and ville= ? \r\n"
                			+ "and Semaines.idPersonne = Personnes.idPersonne\r\n"
                			+ "and numSemaine >= ? \r\n"
                			+ "and numSemaine <= ? and annee = ? and dispoPrime = 1 \r\n"
                			+ "group by Personnes.nom, prenom order by nb"); 
                	
                	ps.setString(1, villeChoiceBox.getValue());
                	ps.setInt(2, semDeb);
                	ps.setInt(3, semFin);
                	ps.setInt(4, anneeChoiceBox.getValue());
                	ResultSet rs = ps.executeQuery();
                	
                    while (rs.next()) {
                    	int nbPrime = rs.getInt(1);
                    	String nom = rs.getString(2);
                    	String prenom = rs.getString(3);
                      
                        PersoModel perso = new PersoModel(nom,prenom,nbPrime,descrip);
                        data.add(perso);
                    }

                    return data;
				}
            	
            }catch (SQLException e) {
                Erreur.databaseProblem();
                e.printStackTrace();
            }
		}
		return null;
        
    }
    
    @FXML
    void Afficher(ActionEvent event) {
    	
//    	check if is it empty!!!!!!!
    	if (villeChoiceBox.getValue()==null || semDebChoiceBox.getValue()==null || semFinChoiceBox.getValue()==null || anneeChoiceBox.getValue()==null) {
    		Erreur.msgEmptyfields();
		}else {
			ObservableList<PersoModel> data = fetchDataFromDatabase();
			tablePrime.setItems(data);
		}
    }
    
    private void clearInputFields() {
        villeChoiceBox.getSelectionModel().clearSelection();;
        semFinChoiceBox.getSelectionModel().clearSelection();;
        semDebChoiceBox.getSelectionModel().clearSelection();;
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
