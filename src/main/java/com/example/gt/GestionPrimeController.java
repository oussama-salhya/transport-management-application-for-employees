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

public class GestionPrimeController {
	int annee = Main.yearNumber;
	Connection conn = Connexion.getConnection();
    @FXML
    private TableColumn<PersoModel, String> prenomColumn;

    @FXML
    private TableColumn<PersoModel,Integer> nbPrimeColumn;

    @FXML
    private TableColumn<PersoModel,String> nomColumn;

    @FXML
    private TableColumn<PersoModel,Boolean> selectCheckbox;

    @FXML
    private TableView<PersoModel> tablePrime;

    @FXML
    private ChoiceBox<String> villeChoiceBox;
    @FXML
    private ChoiceBox<Integer> semChoiceBox;
    @FXML
    private TextField descripField;
    
    @SuppressWarnings("unchecked")
	public void initialize() {
		tablePrime.setEditable(true);
//		create the table columns
		selectCheckbox.setCellFactory(CheckBoxTableCell.forTableColumn(selectCheckbox));
		selectCheckbox.setCellValueFactory(new PropertyValueFactory<>("selected"));
		nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
		prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
		nbPrimeColumn.setCellValueFactory(new PropertyValueFactory<>("nbPrime"));
		tablePrime.getColumns().setAll(nbPrimeColumn,nomColumn,prenomColumn,selectCheckbox);
		
		
        ObservableList<String> ville = FXCollections.observableArrayList();
        ObservableList<Integer> semaine = FXCollections.observableArrayList();
        villeChoiceBox.setOnAction(this::Afficher);
        try {
			
        	Statement st = conn.createStatement();
        	ResultSet rs ;
        	
        	rs = st.executeQuery("select distinct ville from Arretes");
        	
        	while (rs.next()) {
        		ville.add(rs.getString(1));
        	}
        	villeChoiceBox.setItems(ville);
        	
        	rs = st.executeQuery("select distinct numSemaine from Semaines");
        	int lastWeek = 999;
        	while (rs.next()) {
        		lastWeek = rs.getInt(1);
        		semaine.add(lastWeek);
        	}
        	semChoiceBox.setItems(semaine);
        	semChoiceBox.setValue(lastWeek);
        	

            semChoiceBox.setOnAction(this::Afficher);
            villeChoiceBox.setOnAction(this::Afficher);
            
		} catch (Exception e) {
			Erreur.databaseProblem();
			e.printStackTrace();
		}
        
	}
    private ObservableList<PersoModel> fetchDataFromDatabase() {
    	
    		ObservableList<PersoModel> data = FXCollections.observableArrayList();
            try {
                	PreparedStatement ps = conn.prepareStatement("select sum(prime) as nb ,Personnes.nom ,prenom \r\n"
                			+ "                			from Personnes,Semaines,Arretes,Equipes\r\n"
                			+ "                			where Personnes.idPersonne = Semaines.idPersonne\r\n"
                			+ "                			and Arretes.idArrete= Personnes.idArrete and Equipes.idEquipe = Semaines.idEquipe \r\n"
                			+ "                			and ville = ? and dispoPrime = 1 \r\n"
                			+ "							and Personnes.idPersonne in\r\n"
                			+ "							(\r\n"
                			+ "							select idPersonne from Semaines \r\n"
                			+ "							where numSemaine = ? and annee = ? and confirme='NON'\r\n"
                			+ "							)\r\n"
                			+ "                			 group by prenom,Personnes.nom order by nb "); 
                	
                	ps.setString(1, villeChoiceBox.getValue());
                	
                	ps.setInt(2, semChoiceBox.getValue());
                	ps.setInt(3, annee);
                	ResultSet rs = ps.executeQuery();
                	
                    while (rs.next()) {
                    	int nbPrime = rs.getInt(1);
                    	String nom = rs.getString(2);
                    	String prenom = rs.getString(3);
                        PersoModel perso = new PersoModel(nom,prenom,nbPrime);
                        data.add(perso);
                    }
                    return data;
                
            }catch (SQLException e) {
                Erreur.databaseProblem();
                e.printStackTrace();
            }
            return null;
    }
    
    void Afficher(ActionEvent event) {
    	
//    	check if is it empty!!!!!!!
    	if (villeChoiceBox.getValue()==null) {
			Erreur.msgEmptyfields();
		}else {
			ObservableList<PersoModel> data = fetchDataFromDatabase();
			tablePrime.setItems(data);
		}
    }
    @FXML
    void ajouterPrime(ActionEvent event) {
				ObservableList<PersoModel> selectedItems = tablePrime.getItems().filtered(PersoModel::isSelected);
				try {
					
					PreparedStatement ps = conn.prepareStatement(" update Semaines\r\n"
							+ "set prime = prime + 1, description = ?\r\n"
							+ "where numSemaine = ? and annee = ?\r\n"
							+ "and idPersonne in(\r\n"
							+ "select idPersonne from Personnes\r\n"
							+ "where nom = ?\r\n"
							+ "and prenom = ?\r\n"
							+ ")"); 
				for (PersoModel persoModel : selectedItems) {
					String nom = persoModel.getNom();
					String prenom = persoModel.getPrenom();
						String descrip;
						if (descripField.getText().isEmpty()) {
							descrip ="";
						}else {
							descrip = descripField.getText();
						}
						ps.setString(1, descrip);
						ps.setInt(2, semChoiceBox.getValue());
						ps.setInt(3, annee);
						ps.setString(4, nom);
						ps.setString(5, prenom);
						ps.addBatch();
					
				}
				ps.executeBatch();
					} catch (SQLException e) {
						Erreur.databaseProblem();
						e.printStackTrace();
					}
    	ObservableList<PersoModel> data = fetchDataFromDatabase();
		tablePrime.setItems(data);
    	
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
    void supprimerPrime(ActionEvent event) {
	 
			ObservableList<PersoModel> selectedItems = tablePrime.getItems().filtered(PersoModel::isSelected);
			for (PersoModel persoModel : selectedItems) {
				PreparedStatement ps1;
				int nbPrime = 0;
				try {
					ps1 = conn.prepareStatement("select prime\r\n"
							+ "from Personnes,Semaines,Arretes,Equipes\r\n"
							+ "where Personnes.idPersonne = Semaines.idPersonne\r\n"
							+ "and Arretes.idArrete= Personnes.idArrete and Equipes.idEquipe = Semaines.idEquipe \r\n"
							+ "and ville = ? and annee = ? and numSemaine = ? and Personnes.nom=  ? and prenom = ?"
							 );
					ps1.setString(1, villeChoiceBox.getValue());
					ps1.setInt(2, annee);
					ps1.setInt(3, semChoiceBox.getValue());
					ps1.setString(4, persoModel.getNom());
					ps1.setString(5, persoModel.getPrenom());
					ResultSet rs = ps1.executeQuery();
					if (rs.next()) {
						
						nbPrime=rs.getInt(1);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					Erreur.databaseProblem();
				} 
            	
            	
				if (nbPrime==0) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
			        alert.setHeaderText("Invalid input!");
			        alert.setContentText( "vouz avez arrivez à le nombre minimal du prime pour "
			        + persoModel.getNom()+ " pour la semaine " + semChoiceBox.getValue() +" ! \n");
			        alert.showAndWait();
				}else {
					
					String nom = persoModel.getNom();
					String prenom = persoModel.getPrenom();
					
					try {
						PreparedStatement ps = conn.prepareStatement(" update Semaines\r\n"
								+ "set prime = prime - 1\r\n"
								+ "where numSemaine = ?\r\n"
								+ "and idPersonne in(\r\n"
								+ "select idPersonne from Personnes\r\n"
								+ "where nom = ?\r\n"
								+ "and prenom = ?\r\n"
								+ ")"); 
						ps.setInt(1, semChoiceBox.getValue());
						ps.setString(2, nom);
						ps.setString(3, prenom);
						ps.executeUpdate();
					} catch (SQLException e) {
						Erreur.databaseProblem();
						e.printStackTrace();
					}
				}
			}
	
	ObservableList<PersoModel> data = fetchDataFromDatabase();
	tablePrime.setItems(data);
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
