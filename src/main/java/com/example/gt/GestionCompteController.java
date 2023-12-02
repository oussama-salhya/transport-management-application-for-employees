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
import models.CompteModel;

import java.io.IOException;
import java.sql.*;

public class GestionCompteController {
	Connection conn = Connexion.getConnection();

    @FXML
    private TextField mdp;
    @FXML
    private TextField section;

    @FXML
    private TableColumn<CompteModel, String> mdpColumn;

    @FXML
    private TextField newMdp;

    @FXML
    private TextField newUser;

    @FXML
    private TableColumn<CompteModel, String> sectionColumn;

    @FXML
    private TableColumn<CompteModel, Boolean> selectCheckbox;

    @FXML
    private TableView<CompteModel> tableCompte;

    @FXML
    private TextField user;
    @FXML
    private TextField newSection;

    @FXML
    private TableColumn<CompteModel, String> userColumn;
    
    @SuppressWarnings("unchecked")
	public void initialize() {
		tableCompte.setEditable(true);
//		create the table columns
		selectCheckbox.setCellFactory(CheckBoxTableCell.forTableColumn(selectCheckbox));
		selectCheckbox.setCellValueFactory(new PropertyValueFactory<>("selected"));
		sectionColumn.setCellValueFactory(new PropertyValueFactory<>("section"));
		userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
		mdpColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
		tableCompte.getColumns().setAll(userColumn,mdpColumn,sectionColumn,selectCheckbox);
		ObservableList<CompteModel> data = fetchDataFromDatabase();
        // Set the data in the TableView
        tableCompte.setItems(data);
	} 
    private ObservableList<CompteModel> fetchDataFromDatabase() {
        ObservableList<CompteModel> data = FXCollections.observableArrayList();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select utilisateur,password,atelier from Comptes,Chefs "
            		+ "where Chefs.idCompte = Comptes.idCompte ");
            while (rs.next()) {
            	String user = rs.getString(1);
            	String mdp = rs.getString(2);
            	String section = rs.getString(3);
                CompteModel compte = new CompteModel(user,mdp,section);
                data.add(compte);
            }
            st.close();
        }catch (SQLException e) {
            Erreur.databaseProblem();
            e.printStackTrace();
        }
        return data;
    }

    @FXML
    void ajouterCompte(ActionEvent event) {
    	if (user.getText().isEmpty() || mdp.getText().isEmpty() || section.getText().isEmpty()) {
			Erreur.msgEmptyfields();
		}else {
        	try {
        		PreparedStatement ps = conn.prepareStatement("select idChef from Chefs where atelier = ?");
        		ps.setString(1, section.getText());
        		ResultSet rs = ps.executeQuery(); 
				if (rs.next()) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("Invalid input!");
					alert.setContentText( "cette section est déja inserer\n");
					alert.showAndWait();
				}else {
					ps = conn.prepareStatement("select idCompte from Comptes where utilisateur = ?");
	        		ps.setString(1, user.getText());
	        		rs = ps.executeQuery(); 
					if (rs.next()) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");
						alert.setHeaderText("Invalid input!");
						alert.setContentText( "Nom d'utilisateur est déja inserer\n");
						alert.showAndWait();
					}else {
						ps = conn.prepareStatement("insert into Comptes(utilisateur,password)\r\n"
								+ "values(?, ?)");
		        		ps.setString(1, user.getText());
		        		ps.setString(2, mdp.getText());
		        		ps.executeUpdate(); 
		        		ps = conn.prepareStatement("select idCompte from Comptes where utilisateur = ?");
		        		ps.setString(1, user.getText());
		        		rs = ps.executeQuery(); 
		        		rs.next();
		        		ps = conn.prepareStatement("insert into Chefs(atelier,idCompte)\r\n"
		        				+ "values(?, ?)");
		        		ps.setString(1, section.getText());
		        		ps.setInt(2, rs.getInt(1));
		        		ps.executeUpdate(); 
		        		initialize();
		        		clearInputFields();
					}
				}
			} catch (SQLException e) {
				Erreur.databaseProblem();
				e.printStackTrace();
			}
		}
    }
    @FXML
    void changerDonnee(ActionEvent event) {
    	ObservableList<CompteModel> selectedItems = tableCompte.getItems().filtered(CompteModel::isSelected);
    	if (newUser.getText().isEmpty() || newMdp.getText().isEmpty() || newSection.getText().isEmpty()) {
			Erreur.msgEmptyfields();
		}else if (selectedItems.size() == 0) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Invalid input!");
			alert.setContentText( "il faut selectionner au moins une personne \n");
			alert.showAndWait();
		}else if (selectedItems.size() > 1) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Invalid input!");
			alert.setContentText( "il faut selectionner une personne dans chaque suppression\n");
			alert.showAndWait();
		} else  {
			
			CompteModel compte = selectedItems.get(0);

				try {
				 	String compteSection = compte.getSection();

	                	PreparedStatement ps = conn.prepareStatement("select idChef from Chefs,Comptes where Chefs.idCompte = Comptes.idCompte and  atelier = ? and utilisateur != ? ");
	                	ps.setString(1, newSection.getText());
						ps.setString(2, compte.getUser());
	                	ResultSet rs = ps.executeQuery(); 
	                	if (rs.next()) {
	                		Alert alert = new Alert(AlertType.ERROR);
	                		alert.setTitle("Error");
	                		alert.setHeaderText("Invalid input!");
	                		alert.setContentText( "cette section est déja inserer\n");
	                		alert.showAndWait();
	                	}else {
	                		ps = conn.prepareStatement("select idCompte from Comptes where utilisateur = ? ");
	                		ps.setString(1, newUser.getText());
	                		rs = ps.executeQuery(); 
	                		if (rs.next()) {
	                			Alert alert = new Alert(AlertType.ERROR);
	                			alert.setTitle("Error");
	                			alert.setHeaderText("Invalid input!");
	                			alert.setContentText( "Nom d'utilisateur est déja inserer\n");
	                			alert.showAndWait();
	                		}else {
	                			ps = conn.prepareStatement("update Comptes \r\n"
	                					+ "set utilisateur = ? , \r\n"
	                					+ "password = ? \r\n"
	                					+ "where utilisateur = ?");
	                			ps.setString(1, newUser.getText());
	                			ps.setString(2, newMdp.getText());
	                			ps.setString(3, compte.getUser());
	                			ps.executeUpdate(); 
	                			ps = conn.prepareStatement("select idCompte from Comptes where utilisateur = ?");
	    		        		ps.setString(1, newUser.getText());
	    		        		rs = ps.executeQuery(); 
	    		        		rs.next();
	    		        		ps = conn.prepareStatement("update Chefs\r\n"
	                					+ "set atelier = ? \r\n"
	                					+ "where idCompte = ?");
	                			ps.setString(1, newSection.getText());
	                			ps.setInt(2, rs.getInt(1));
	                			ps.executeUpdate(); 
	                			initialize();
	                			clearInputFields();
	                		}
	                	}
						

				} catch (SQLException e) {
					Erreur.databaseProblem();
					e.printStackTrace();
				}
		}
		}

    private void clearInputFields() {
        user.setText("");
        mdp.setText("");
        section.setText("");
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
    void supprimerCompte(ActionEvent event) {
    	ObservableList<CompteModel> selectedItems = tableCompte.getItems().filtered(CompteModel::isSelected);
    	
    	if (selectedItems.size() == 0) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Invalid input!");
			alert.setContentText( "il faut selectionner au moins une personne \n");
			alert.showAndWait();
		}else if (selectedItems.size() > 1) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Invalid input!");
			alert.setContentText( "il faut selectionner une personne dans chaque suppression\n");
			alert.showAndWait();
		} else {
			CompteModel compte = selectedItems.get(0);
			PreparedStatement ps;
			try {
			if (compte.getSection().equals("admin")) {
				ps = conn.prepareStatement("select COUNT(idCompte) from Comptes,Chefs \r\n"
						+ "where atelier='admin' and Comptes.idCompte = Chefs.idCompte");
				ResultSet check = ps.executeQuery();
				if (check.next()) {
					if (check.getInt(1) == 1) {

						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");
						alert.setHeaderText("Invalid input!");
						alert.setContentText( "vous ne pouvez pas supprimer le dernier compte admin \n");
						alert.showAndWait();
						initialize();
					}else {
						ps = conn.prepareStatement("delete Comptes where utilisateur = ?");
						ps.setString(1, compte.getUser());
						ps.executeUpdate() ;
						initialize();
					}
				}
			}else {
				ps = conn.prepareStatement("select Comptes.idCompte from Comptes,Personnes,Chefs "
						+ "where Chefs.idCompte = Comptes.idCompte and utilisateur = ? and  Personnes.idChef = Chefs.idChef");
				ps.setString(1, compte.getUser());
				ResultSet rs = ps.executeQuery(); 
				if (rs.next()) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("Invalid input!");
					alert.setContentText( "ce compte et sa section est lié a des Personnes \n");
					alert.showAndWait();
					initialize();
				}else {
					ps = conn.prepareStatement("delete Chefs where atelier = ?");
					ps.setString(1, compte.getSection());
					ps.executeUpdate() ;
					ps = conn.prepareStatement("delete Comptes where utilisateur = ?");
					ps.setString(1, compte.getUser());
					ps.executeUpdate() ;
					initialize();
				}
				
			}
				
			} catch (SQLException e) {
				Erreur.databaseProblem();
				e.printStackTrace();
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
