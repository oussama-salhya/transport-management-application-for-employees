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
public class GestionPersoController {
	Connection conn = Connexion.getConnection();

    @FXML
    private ChoiceBox<String> arreteChoiceBox;
    
    @FXML
    private ChoiceBox<String> secChoiceBox;


    @FXML
    private TableColumn<PersoModel, String> arreteColumn;

    @FXML
    private TableColumn<PersoModel, String> nomColumn;
    
    @FXML
    private TableColumn<PersoModel, String> sectionColumn;


    @FXML
    private TableColumn<PersoModel, String> numMatColumn;


    @FXML
    private TableColumn<PersoModel, String> prenomColumn;


    @FXML
    private TableColumn<PersoModel, Integer> primeDisponibiliteColumn;

    @FXML
    private TableColumn<PersoModel, Boolean> selectCheckbox;
    @FXML
    private TextField numMatField;

    @FXML
    private TextField prenomField;

    @FXML
    private TableView<PersoModel> tablePerso;
    
    @FXML
    private TextField nomField;
    
    @SuppressWarnings("unchecked")
    
	public void initialize() {
		tablePerso.setEditable(true);
//		create the table columns
		selectCheckbox.setCellFactory(CheckBoxTableCell.forTableColumn(selectCheckbox));
		selectCheckbox.setCellValueFactory(new PropertyValueFactory<>("selected"));
		numMatColumn.setCellValueFactory(new PropertyValueFactory<>("numMat"));
		sectionColumn.setCellValueFactory(new PropertyValueFactory<>("section"));
		nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
		prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
		arreteColumn.setCellValueFactory(new PropertyValueFactory<>("arrete"));
		primeDisponibiliteColumn.setCellValueFactory(new PropertyValueFactory<>("primeDispo"));
		tablePerso.getColumns().setAll(numMatColumn,nomColumn,prenomColumn,arreteColumn,primeDisponibiliteColumn,sectionColumn,selectCheckbox);
		ObservableList<PersoModel> data = fetchDataFromDatabase();
        // Set the data in the TableView
        tablePerso.setItems(data);
        ObservableList<String> arrete = FXCollections.observableArrayList();
        ObservableList<String> section = FXCollections.observableArrayList();
        try {
			
        	Statement st = conn.createStatement();
        	ResultSet rs = st.executeQuery("select nom from Arretes where Arretes.nom !='Aucun' ");
        	
        	while (rs.next()) {
        		arrete.add(rs.getString(1));
        	}
        	arreteChoiceBox.setItems(arrete);
        	
        	rs = st.executeQuery("select distinct atelier from Chefs where atelier != 'admin'");
        	
        	while (rs.next()) {
        		section.add(rs.getString(1));
        	}
        	secChoiceBox.setItems(section);
		} catch (Exception e) {
			Erreur.databaseProblem();
			e.printStackTrace();
		}
        
	} 
    private ObservableList<PersoModel> fetchDataFromDatabase() {
        ObservableList<PersoModel> data = FXCollections.observableArrayList();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT Personnes.idPersonne,Personnes.nom,prenom,numMatricule,dispoPrime,atelier\r\n"
            		+ "FROM personnes,Arretes,Chefs\r\n"
            		+ "where Personnes.idArrete = Arretes.idArrete\r\n"
            		+ "and Personnes.idChef	= Chefs.idChef\r\n"
            		+ "and prenom != ''");
            while (rs.next()) {
            	int idPerso = rs.getInt(1);
            	String nom = rs.getString(2);
            	String prenom = rs.getString(3);
            	String numMat = rs.getString(4);
                String primeDispo ;
                if (rs.getInt(5)==1) {
                	 primeDispo="OUI" ;
				}else {
					primeDispo="NON" ;					
				}
                String section = rs.getString(6);
                
//                trouvez nom d'arrete a partie idPerso
                PreparedStatement ps = conn.prepareStatement("select Arretes.nom from Arretes,Personnes where Personnes.idArrete = Arretes.idArrete and Personnes.idPersonne=?");
                ps.setInt(1, idPerso);
                ResultSet rs1 = ps.executeQuery();
                String arrete ;
                if (rs1.next()) {
                	arrete = rs1.getString(1);
				}else {
					arrete="pas d'arrete";
				}
                PersoModel perso = new PersoModel(numMat,nom,prenom,arrete,primeDispo,section);
                data.add(perso);
            }
            st.close();
        }catch (SQLException e) {
            Erreur.databaseProblem();
            e.printStackTrace();
        }
        return data;
    }
    @FXML
    void ajouterPerso(ActionEvent event) {

        if(nomField.getText().isEmpty()||prenomField.getText().isEmpty()||numMatField.getText().isEmpty()||
        		arreteChoiceBox.getValue()==null || (secChoiceBox.getValue()==null)) {
        	Erreur.msgEmptyfields();
        }else if(!isNumeric(numMatField.getText())){
        	Erreur.msgNotNumeric("Numero du matricule");
        }else if (isNumeric(nomField.getText())) {
				Erreur.msgNumeric("Nom");
			
        }else if (isNumeric(prenomField.getText())) {
				Erreur.msgNumeric("Prenom");
        }else {
            try {
            	
                PreparedStatement psCheck = conn.prepareStatement("SELECT prenom FROM Personnes where numMatricule=?  ");
                psCheck.setString(1, numMatField.getText());
                ResultSet rsCheck = psCheck.executeQuery(); 
                if (!rsCheck.next()) {
//                trouvez id de l'arrete 	
                PreparedStatement ps = conn.prepareStatement("select Arretes.idArrete from Arretes where Arretes.nom = ?");
                ps.setString(1, arreteChoiceBox.getValue());
                ResultSet rs = ps.executeQuery();
                rs.next();
                int idArrete = rs.getInt(1);
                String section = secChoiceBox.getValue();
                ps = conn.prepareStatement("select idChef from Chefs where atelier = ?");
                ps.setString(1, section);
                rs = ps.executeQuery();
                rs.next();
                int idChef = rs.getInt(1);
                PreparedStatement ps1 = conn.prepareStatement("insert into Personnes (nom, prenom, numMatricule,idArrete,idChef)\r\n"
                				+ "values (? ,?,? ,?,?);");
                ps1.setString(1, nomField.getText());
                ps1.setString(2, prenomField.getText());
                ps1.setString(3, numMatField.getText());
                ps1.setInt(4, idArrete);
                
                ps1.setInt(5, idChef);
                ps1.executeUpdate();
                clearInputFields();
                tablePerso.getItems().removeAll(tablePerso.getItems());
                initialize();
                }else {
                	PreparedStatement ps = conn.prepareStatement("SELECT prenom,nom FROM Personnes where numMatricule=?  and prenom = '' ");
                    ps.setString(1, numMatField.getText());
                    ResultSet rs = ps.executeQuery(); 
                	if (rs.next()) {
                		PreparedStatement ps2 = conn.prepareStatement("select Arretes.idArrete from Arretes where Arretes.nom = ?");
                        ps2.setString(1, arreteChoiceBox.getValue());
                        ResultSet rs2 = ps2.executeQuery();
                        rs2.next();
                        int idArrete = rs2.getInt(1);
                        
                        String section = secChoiceBox.getValue();
                        ps = conn.prepareStatement("select idChef from Chefs where atelier = ?");
                        ps.setString(1, section);
                        rs = ps.executeQuery();
                        rs.next();
                        int idChef = rs.getInt(1);
                        PreparedStatement ps1 = conn.prepareStatement("update Personnes\r\n"
                        		+ "set \r\n"
                        		+ "nom = ?,\r\n"
                        		+ "prenom = ?,\r\n"
                        		+ "idArrete = ? ,\r\n"
                        		+ "idChef = ?\r\n"
                        		+ "where numMatricule = ?");
		                ps1.setString(1, nomField.getText());
		                ps1.setString(2, prenomField.getText());
		                ps1.setInt(3, idArrete);
		                ps1.setInt(4, idChef);
		                ps1.setString(5, numMatField.getText());
		                ps1.executeUpdate();
		                clearInputFields();
		                tablePerso.getItems().removeAll(tablePerso.getItems());
		                initialize();
					}else {
						
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");
						alert.setHeaderText("Invalid input!");
						alert.setContentText( "Numéro du matricule est déjà inseré \n vous avez déjà inserer ce personne \n");
						alert.showAndWait();
					}
                }
            } catch (SQLException e	) {
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
        Double.parseDouble(str);
        return true;
    } catch (NumberFormatException e) {
        return false;
    }
}
private void clearInputFields() {
    nomField.setText("");
    prenomField.setText("");
    numMatField.setText("");
    arreteChoiceBox.getSelectionModel().clearSelection();
    secChoiceBox.getSelectionModel().clearSelection();
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
    void supprimerPerso(ActionEvent event) {
    	ObservableList<PersoModel> selectedItems = tablePerso.getItems().filtered(PersoModel::isSelected);
    	
		try {
		    PreparedStatement st = conn.prepareStatement("update Personnes set prenom = ''\r\n"
		    		+ "where numMatricule = ? ");
		    for (PersoModel perso : selectedItems) {
		        st.setString(1, perso.getNumMat()); 
		        st.executeUpdate();
		    }
		    // Optional: Refresh the TableView after the operation
		    tablePerso.getItems().removeAll(selectedItems);
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
