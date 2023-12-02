package com.example.gt;

import Tools.Erreur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.PersoModel;

import java.io.IOException;
import java.sql.*;

public class ImprimerController {
	Connection conn = Connexion.getConnection();

    @FXML
    private TableColumn<PersoModel, String> arreteColumn;

    @FXML
    private ChoiceBox<Integer> equipeChoiceBox;
    @FXML
    private ChoiceBox<Integer> semChoiceBox;

    @FXML
    private TableColumn<PersoModel, String> nomColumn;

    @FXML
    private TableColumn<PersoModel, Integer> ordreColumn;

    @FXML
    private TableColumn<PersoModel, String> prenomColumn;

    @FXML
    private TableView<PersoModel> tableEquipe;

    @FXML
    private ChoiceBox<String> villeChoiceBox;
    
    @SuppressWarnings("unchecked")
	public void initialize() {
		tableEquipe.setEditable(true);
//		create the table columns
		ordreColumn.setCellValueFactory(new PropertyValueFactory<>("ordre"));
		arreteColumn.setCellValueFactory(new PropertyValueFactory<>("arrete"));
		nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
		prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
		tableEquipe.getColumns().setAll(ordreColumn,nomColumn,prenomColumn,arreteColumn);
		
        ObservableList<Integer> equipe = FXCollections.observableArrayList();
        ObservableList<Integer> semaine = FXCollections.observableArrayList();
        ObservableList<String> ville = FXCollections.observableArrayList();
        try {
			
        	Statement st = conn.createStatement();
        	ResultSet rs = st.executeQuery("select * from Equipes ");
        	
        	while (rs.next()) {
        		equipe.add(rs.getInt(4));
        		equipeChoiceBox.setItems(equipe);
        	}
        	
        	rs = st.executeQuery("select distinct ville from Arretes ");
        	
        	while (rs.next()) {
        		ville.add(rs.getString(1));
        		villeChoiceBox.setItems(ville);
        	}
		} catch (Exception e) {
			Erreur.databaseProblem();
			e.printStackTrace();
		}
        try {
        	Statement st = conn.createStatement();
        	ResultSet rs = st.executeQuery("select distinct numSemaine from Semaines order by numSemaine asc ");
        	int grandeSem = 999;
        	while (rs.next()) {
        		grandeSem = rs.getInt(1);
        		semaine.add(grandeSem);
        	}
        	
        	semChoiceBox.setItems(semaine);
        	semChoiceBox.setValue(Main.weekNumber);
        } catch (Exception e) {
        	Erreur.databaseProblem();
        	e.printStackTrace();
        }
        semChoiceBox.setOnAction(this::Afficher);
        equipeChoiceBox.setOnAction(this::Afficher);
        villeChoiceBox.setOnAction(this::clearEquipe);
        
	}
    private ObservableList<PersoModel> fetchDataFromDatabase() {
    	
		ObservableList<PersoModel> data = FXCollections.observableArrayList();
        try {
        	PreparedStatement ps = conn.prepareStatement("select ordre,Arretes.nom,Personnes.nom,prenom from Personnes,Equipes,Semaines,Arretes\r\n"
        			+ "where Equipes.idEquipe = Semaines.idEquipe\r\n"
        			+ "and Semaines.idPersonne = Personnes.idPersonne\r\n"
        			+ "and Arretes.idArrete =Personnes.idArrete\r\n"
        			+ "and ville= ? \r\n"
        			+ "and numSemaine = ? and annee = ? \r\n"
        			+ "and numEquipe = ? order by ordre asc"); 
        	
        	ps.setString(1, villeChoiceBox.getValue());
        	ps.setInt(2, semChoiceBox.getValue());
        	ps.setInt(3, Main.yearNumber);
         	ps.setInt(4, equipeChoiceBox.getValue());
        	ResultSet rs = ps.executeQuery();
        	
            while (rs.next()) {
            	int ordre = rs.getInt(1);
            	String arrete = rs.getString(2);
            	String nom = rs.getString(3);
            	String prenom = rs.getString(4);
            	
                PersoModel perso = new PersoModel(nom,prenom,arrete,ordre);
                data.add(perso);
            }
        }catch (SQLException e) {
            Erreur.databaseProblem();
            e.printStackTrace();
        }
        return data;
    
}
    
    void Afficher(ActionEvent event) {
//    	check if is it empty!!!!!!!
    	if (villeChoiceBox.getValue()==null) {
    		equipeChoiceBox.getSelectionModel().clearSelection();
    		 Alert alert = new Alert(AlertType.ERROR);
    	 	    alert.setTitle("Invalid input");
    	 	    alert.setHeaderText( " inserer la ville!");
    	 	    alert.setContentText("");
    	 	    alert.showAndWait();
		}else if (equipeChoiceBox.getValue()==null) {
    		equipeChoiceBox.getSelectionModel().clearSelection();
   		 Alert alert = new Alert(AlertType.ERROR);
   	 	    alert.setTitle("Invalid input");
   	 	    alert.setHeaderText( " inserer l'équipe!");
   	 	    alert.setContentText("");
   	 	    alert.showAndWait();
		}else {
			ObservableList<PersoModel> data = fetchDataFromDatabase();
			tableEquipe.setItems(data);
		}
    }
    void clearEquipe(ActionEvent event) {
    	equipeChoiceBox.getSelectionModel().clearSelection();
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

    @SuppressWarnings("unchecked")
	@FXML
    void imprimer(ActionEvent event) {
    	if (villeChoiceBox.getValue()==null) {
    		equipeChoiceBox.getSelectionModel().clearSelection();
    		 Alert alert = new Alert(AlertType.ERROR);
    	 	    alert.setTitle("Invalid input");
    	 	    alert.setHeaderText( " inserer la ville!");
    	 	    alert.setContentText("");
    	 	    alert.showAndWait();
		}else if (equipeChoiceBox.getValue()==null) {
    		equipeChoiceBox.getSelectionModel().clearSelection();
   		 Alert alert = new Alert(AlertType.ERROR);
   	 	    alert.setTitle("Invalid input");
   	 	    alert.setHeaderText( " inserer l'équipe!");
   	 	    alert.setContentText("");
   	 	    alert.showAndWait();
		}else {
			
			PrinterJob job = PrinterJob.createPrinterJob();
			TableView<PersoModel> tableView = new TableView<>();
			TableColumn<PersoModel, Integer> ordreColumn = new TableColumn<>("Ordre");
			TableColumn<PersoModel, String> arreteColumn = new TableColumn<>("Arrête");
			TableColumn<PersoModel, String> nomColumn = new TableColumn<>("Nom");
			TableColumn<PersoModel, String> prenomColumn = new TableColumn<>("Prenom");
//	    stageTitleColumn.setPrefWidth(80); 
//	    stageSubjectColumn.setPrefWidth(370); 
			
			// Set the cell value factories for the table columns
			ordreColumn.setCellValueFactory(new PropertyValueFactory<>("ordre"));
			arreteColumn.setCellValueFactory(new PropertyValueFactory<>("arrete"));
			nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
			prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
			
			// Set the cell factory to wrap the text content
//	    stageTitleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
//	    stageSubjectColumn.setCellFactory(TextFieldTableCell.forTableColumn());
			
			tableView.getColumns().setAll(ordreColumn, arreteColumn, nomColumn,prenomColumn);
			if (job != null) {
				ObservableList<PersoModel> data = FXCollections.observableArrayList();
				try {
					PreparedStatement ps = conn.prepareStatement("select ordre,Arretes.nom,Personnes.nom,prenom from Personnes,Equipes,Semaines,Arretes\r\n"
							+ "where Equipes.idEquipe = Semaines.idEquipe\r\n"
							+ "and Semaines.idPersonne = Personnes.idPersonne\r\n"
							+ "and Arretes.idArrete =Personnes.idArrete\r\n"
							+ "and ville= ? \r\n"
							+ "and numSemaine = ? and annee = ? \r\n"
							+ "and numEquipe = ? order by ordre asc"); 
					
					ps.setString(1, villeChoiceBox.getValue());
					ps.setInt(2, semChoiceBox.getValue());
					ps.setInt(3, Main.yearNumber);
					ps.setInt(4, equipeChoiceBox.getValue());
					ResultSet rs = ps.executeQuery();
					
					while (rs.next()) {
						int ordre = rs.getInt(1);
						String arrete = rs.getString(2);
						String nom = rs.getString(3);
						String prenom = rs.getString(4);
						PersoModel perso = new PersoModel(nom,prenom,arrete,ordre);
						data.add(perso);
					}
					tableView.setItems(data);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				// Obtenez le noeud racine de votre scène
				Node root = tableView;
				root.setStyle("-fx-pref-height: 1080px; -fx-pref-width: 1080px;");
				// Imprimez le contenu de la TableView
				boolean success = job.printPage(root);
				
				if (success) {
					job.endJob();
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText( " Erreur d'impression !");
					alert.setContentText("");
					alert.showAndWait();
				}
			}
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
