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
public class GestionEquipesController {
	Connection conn = Connexion.getConnection();
	// nombre Max -------------------------------------------------------!
    @FXML
    private TableColumn<PersoModel, String> arreteColumn;

    @FXML
    private ChoiceBox<Integer> equipeChoiceBox;
    @FXML
    private ChoiceBox<Integer> semChoiceBox;
    
    @FXML
    private TextField capacite;
    @FXML
    private TextField nbPersoConfirme;

    @FXML
    private TableColumn<PersoModel, String> nomColumn;

    @FXML
    private TableColumn<PersoModel, String> numMatColumn;
    
    @FXML
    private TableColumn<PersoModel, String> dispoPrimeColumn;
    
    @FXML
    private TableColumn<PersoModel, String> sectionColumn;

    @FXML
    private TableColumn<PersoModel, String> prenomColumn;

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
		dispoPrimeColumn.setCellValueFactory(new PropertyValueFactory<>("primeDispo"));
		tablePerso.getColumns().setAll(numMatColumn,nomColumn,prenomColumn,arreteColumn,sectionColumn,dispoPrimeColumn,selectCheckbox);
        ObservableList<Integer> equipe = FXCollections.observableArrayList();
        ObservableList<Integer> semaine = FXCollections.observableArrayList();
        try {
        	Statement st = conn.createStatement();
        	ResultSet rs = st.executeQuery("select * from Equipes ");
        	
        	while (rs.next()) {
        		equipe.add(rs.getInt(4));
        	}
        	equipeChoiceBox.setItems(equipe);
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
        equipeChoiceBox.setOnAction(this::AfficherEquipe);
        semChoiceBox.setOnAction(this::AfficherEquipe);
        capacite.setText(String.valueOf(Main.maxNumber));
        
	}
    private ObservableList<PersoModel> fetchDataFromDatabase(int equipe) {
        ObservableList<PersoModel> data = FXCollections.observableArrayList();
        try {
        	PreparedStatement ps = conn.prepareStatement("select numMatricule,Personnes.nom,prenom,Arretes.nom,atelier,dispoPrime \r\n"
        			+ "        			 from Personnes,Arretes,Semaines,Equipes,Chefs\r\n"
        			+ "        			 where Personnes.idArrete = Arretes.idArrete\r\n"
        			+ "					 and Personnes.idPersonne = Semaines.idPersonne\r\n"
        			+ "					 and Semaines.idEquipe = Equipes.idEquipe and Chefs.idChef = Personnes.idChef\r\n"
        			+ "        			 and Semaines.numSemaine =?\r\n"
        			+ "					 and annee =  ?\r\n"
        			+ "					 and numEquipe = ? and confirme = 'NON'");
        	ps.setInt(1,semChoiceBox.getValue());
        	ps.setInt(2, Main.yearNumber);
        	ps.setInt(3, equipe);
        	ResultSet rs = ps.executeQuery();
        	
            while (rs.next()) {
            	String numMat = rs.getString(1);
            	String nom = rs.getString(2);
            	String prenom = rs.getString(3);
            	String arrete = rs.getString(4);
                String section = rs.getString(5);
                String dispoPrime = rs.getString(6);
                if (dispoPrime.equals("1")) {
					dispoPrime = "OUI";
				}else {
					dispoPrime = "NON";
				}
                PersoModel perso = new PersoModel(numMat,nom,prenom,arrete,dispoPrime,section);
                data.add(perso);
            }
        }catch (SQLException e) {
            Erreur.databaseProblem();
            e.printStackTrace();
        }
        return data;
    }
    void AfficherEquipe(ActionEvent event) {
    	if (equipeChoiceBox.getValue()==null || capacite.getText().isEmpty()) {
    		Erreur.msgEmptyfields();
		}else if (!isNumeric(capacite.getText())) {
			Erreur.msgNotNumeric("Capacité");
		}else {
			int equipe;
			equipe = equipeChoiceBox.getValue();
			ObservableList<PersoModel> data = fetchDataFromDatabase(equipe);
			// Set the data in the TableView
			tablePerso.setItems(data);
			actualiserNbpersoConfirme();
			
		}
    }
    void actualiserNbpersoConfirme() {
    	int equipe = equipeChoiceBox.getValue();
    	PreparedStatement ps1;
		try {
			ps1 = conn.prepareStatement("select idEquipe from Equipes where numEquipe = ?");
			ResultSet rs1;
			int idequipe = 0;
			ps1.setInt(1, equipe);
			rs1 = ps1.executeQuery();
			if (rs1.next()) {
				idequipe = rs1.getInt(1);
			}
			PreparedStatement ps = conn.prepareStatement("select COUNT(Equipes.idEquipe) from Equipes,Semaines	\r\n"
					+ "where Equipes.idEquipe = Semaines.idEquipe\r\n"
					+ "and numSemaine = ?\r\n"
					+ "and annee=?\r\n"
					+ "and	Equipes.idEquipe = ? and confirme = 'OUI'");
			ps.setInt(1, semChoiceBox.getValue());
			ps.setInt(2, Main.yearNumber);
			ps.setInt(3, idequipe);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				//les nombre des personnes dans un bus
				int persoNumber = rs.getInt(1);
				nbPersoConfirme.setText(String.valueOf(persoNumber));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    void ajouterAEquipe(ActionEvent event) {
			int sem =semChoiceBox.getValue();
			int equipe = equipeChoiceBox.getValue();
			inserer(sem,equipe);
    }
    
    void inserer(int nvSem , int equipe){

        int maxNumber = Integer.parseInt(capacite.getText());
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
				PreparedStatement ps = conn.prepareStatement("select COUNT(Equipes.idEquipe) from Equipes,Semaines	\r\n"
						+ "where Equipes.idEquipe = Semaines.idEquipe\r\n"
						+ "and numSemaine = ?\r\n"
						+ "and annee=?\r\n"
						+ "and	Equipes.idEquipe = ? and confirme = 'OUI'");
				ps.setInt(1, semChoiceBox.getValue());
				ps.setInt(2, Main.yearNumber);
				ps.setInt(3, idequipe);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					//les nombre des personnes dans un bus
					int persoNumber = rs.getInt(1);
					
					if(persoNumber >= maxNumber) {
						Alert alert = new Alert(AlertType.ERROR);
			            alert.setTitle("Error");
			            alert.setHeaderText("nombre des place est saturé!");
			            alert.setContentText( "il faut donner les primes\n");
			            alert.showAndWait();
			            return;
					}else if (persoNumber + selectedItems.size() > maxNumber ) {
						Alert alert = new Alert(AlertType.ERROR);
			            alert.setTitle("Error");
			            alert.setHeaderText("nombre des personnes selectionné vas saturé l'équipe!");
			            alert.setContentText( "vous avez selectionné : " + selectedItems.size()
			            		+ "\n le nombre des personnes déjà confirmé dans cette équipe est : " + persoNumber
			            		+ "\n le nombre max est : " + maxNumber);;
			            alert.showAndWait();
					}else {
						for (PersoModel perso : selectedItems) {
							
							PreparedStatement ps2 = conn.prepareStatement("select idPersonne from Personnes where numMatricule = ?");
							ResultSet rs2;
							String numMat = perso.getNumMat();
								int idperso = 0;
								ps2.setString(1, numMat);
								rs2 = ps2.executeQuery();
								if (rs2.next()) {
									idperso = rs2.getInt(1);
									
									PreparedStatement st = conn.prepareStatement("update Semaines\r\n"
											+ "set \r\n"
											+ "confirme = 'OUI'\r\n"
											+ "where idEquipe = ?\r\n"
											+ "and idPersonne = ? \r\n"
											+ "and numSemaine = ? and annee = ?");
									st.setInt(1, idequipe);
									st.setInt(2, idperso);
									st.setInt(3, nvSem);
									st.setInt(4, Main.yearNumber);
									
									st.executeUpdate();
									int newEquipe = equipeChoiceBox.getValue();
									ObservableList<PersoModel> data = fetchDataFromDatabase(newEquipe);
									// Set the data in the TableView
									tablePerso.setItems(data);
									
									}
								
								ps1.close();
								ps2.close();
							}
					}
				}
					
				}
				}catch (SQLException e) {
			Erreur.databaseProblem();
			e.printStackTrace();
		}
    	actualiserNbpersoConfirme();
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
    void afficherListPerso(ActionEvent event) {
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getResource("suppPersoEquipe.fxml"));
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
    void ModifierCapacite(ActionEvent event) {
    	if (capacite.isEditable()) {
    		capacite.setEditable(false);
			
		}else {
			capacite.setEditable(true);
			
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
