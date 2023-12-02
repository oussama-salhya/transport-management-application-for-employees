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
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.ArreteModel;
import models.PersoModel;

import java.io.IOException;
import java.sql.*;
public class GestionArreteController {


	Connection conn = Connexion.getConnection();
    @FXML
    private ChoiceBox<String> arreteChoiceBox;
    @FXML
    private ChoiceBox<String> DispoPrimeChoiceBox;

   
    @FXML
    private TableColumn<ArreteModel, String> nomArreteColumn;

    @FXML
    private TextField nomArreteField;

    @FXML
    private TableColumn<ArreteModel, Integer> ordreColumn;

    @FXML
    private TableColumn<ArreteModel, Boolean> selectCheckbox;

    @FXML
    private TableView<ArreteModel> tablearrete;

    @FXML
    private ChoiceBox<String> villeChoiceBox;

    @FXML
    private TableColumn<ArreteModel, String> villeColumn;

    @FXML
    private TextField villeField;

    @SuppressWarnings("unchecked")
    
	public void initialize() {
		tablearrete.setEditable(true);
//		create the table columns
		selectCheckbox.setCellFactory(CheckBoxTableCell.forTableColumn(selectCheckbox));
		selectCheckbox.setCellValueFactory(new PropertyValueFactory<>("selected"));
		nomArreteColumn.setCellValueFactory(new PropertyValueFactory<>("nomArrete"));
		villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));
		ordreColumn.setCellValueFactory(new PropertyValueFactory<>("ordre"));
		tablearrete.getColumns().setAll(ordreColumn,villeColumn,nomArreteColumn,selectCheckbox);
		ObservableList<ArreteModel> data = fetchDataFromDatabase();
        // Set the data in the TableView
        tablearrete.setItems(data);
        ObservableList<String> ville = FXCollections.observableArrayList();
        ObservableList<String> dispoPrime = FXCollections.observableArrayList();
	      dispoPrime.add("OUI");
	      dispoPrime.add("NON");
	      DispoPrimeChoiceBox.setItems(dispoPrime);
	        try {
	        	
	        	Statement st = conn.createStatement();
	        	ResultSet rs = st.executeQuery("select distinct  ville from Arretes where Arretes.nom !='Aucun'");
	        	while (rs.next()) {
	        		ville.add(rs.getString(1));
	        		villeChoiceBox.setItems(ville);
	        	}
	        	
	        } catch (Exception e) {
	        	Erreur.databaseProblem();
	        }
	        villeChoiceBox.setOnAction(this::mouse);
	        
        
	}
    private ObservableList<ArreteModel> fetchDataFromDatabase() {
        ObservableList<ArreteModel> data = FXCollections.observableArrayList();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM Arretes where ordre!=0 order by ville asc,ordre");
            while (rs.next()) {
            	String nom = rs.getString(2);
            	String ville = rs.getString(3);
            	int ordre = rs.getInt(4);

                ArreteModel perso = new ArreteModel(nom,ville,ordre);
                data.add(perso);
            }
        }catch (SQLException e) {
        	Erreur.databaseProblem();
        }
        return data;
        
        
    }
    @FXML
    void ajouterArrete(ActionEvent event) {
    	if (!nomArreteField.getText().isEmpty()&& !villeField.getText().isEmpty() && arreteChoiceBox.getValue()==null
    			&&villeChoiceBox.getValue()==null && DispoPrimeChoiceBox.getValue()!=null) {
    		if (isNumeric(villeField.getText())) {
				Erreur.msgNumeric("Ville");
			}
    		PreparedStatement ps2;
			try {
				ps2 = conn.prepareStatement("  insert into Arretes(nom,ville,ordre,dispoPrime)\r\n"
						+ "  values(?,?,?,?)");
				ps2.setString(1, "Aucun");
				ps2.setString(2, villeField.getText());
				ps2.setInt(3, 0);
				ps2.setInt(4, 0);
				ps2.executeUpdate();
				
				ps2 = conn.prepareStatement("  insert into Arretes(nom,ville,ordre,dispoPrime)\r\n"
						+ "  values(?,?,?,?)");
				ps2.setString(1, nomArreteField.getText());
				ps2.setString(2, villeField.getText());
				ps2.setInt(3, 1);
				int dispoPrime = 0;
				if (DispoPrimeChoiceBox.getValue().equals("OUI")) {
					dispoPrime = 1;
				}
				ps2.setInt(4, dispoPrime);
				ps2.executeUpdate();
				clearInputFields();

                tablearrete.getItems().removeAll(tablearrete.getItems());
				initialize();
			} catch (SQLException e) {
				Erreur.databaseProblem();
				e.printStackTrace();
			}
		}else if(nomArreteField.getText().isEmpty()||villeChoiceBox.getValue()==null || arreteChoiceBox.getValue()==null || DispoPrimeChoiceBox.getValue()==null) {
        	Erreur.msgEmptyfields();
        }else if (isNumeric(villeField.getText()) && !villeField.getText().isEmpty()) {
				Erreur.msgNumeric("Ville de l'arrête");
        }else if (!villeField.getText().isEmpty()) {
        	Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid input!");
            alert.setContentText( "il faut choisie soit une nouvelle ville ou une ville qui a déjà existé\n");
            alert.showAndWait();
        }else{
            try {
            	String nomArrete = nomArreteField.getText();
                PreparedStatement psCheck = conn.prepareStatement("SELECT * FROM Arretes where nom=?");
                psCheck.setString(1, nomArrete);
                ResultSet rsCheck = psCheck.executeQuery(); 
                if (!rsCheck.next()) {
//                	calculer l'ordre
                	String arretAvant = arreteChoiceBox.getValue();
                	PreparedStatement ps = conn.prepareStatement("  select ordre from Arretes where Arretes.nom = ?");
                	ps.setString(1, arretAvant);
                	ResultSet rs = ps.executeQuery();
                	int ordreAvant;
                	if (rs.next()) {
                		ordreAvant = rs.getInt(1);
					}else {
						Erreur.databaseProblem();
						ordreAvant = 999 ;
					}
                	
//                	changer l'ordre des autres si nouveau arrete au milieu 
                	ps = conn.prepareStatement("  select nom from Arretes where Arretes.ordre > ? and  ville=? order by ordre asc");
                	ps.setInt(1, ordreAvant);
                	ps.setString(2, villeChoiceBox.getValue());
                	rs = ps.executeQuery();
                	if (rs.next()) {
                		int ordreApres = ordreAvant + 2;
                		PreparedStatement ps1 = conn.prepareStatement("update Arretes set ordre = ? where nom = ? and ville=? ;");
                		do {
                			ps1.setInt(1, ordreApres);
                			ps1.setString(2, rs.getString(1));
                			ps1.setString(3, villeChoiceBox.getValue());
                			ps1.addBatch();
                			ordreApres++;
                		} while (rs.next()); 
                		ps1.executeBatch();
					}
                	PreparedStatement ps2 = conn.prepareStatement("  insert into Arretes(nom,ville,ordre,dispoPrime)\r\n"
                			+ "  values(?,?,?,?)");
                	ps2.setString(1, nomArrete);
                	ps2.setString(2, villeChoiceBox.getValue());
                	ps2.setInt(3, ordreAvant + 1);
    				int dispoPrime = 0;
    				if (DispoPrimeChoiceBox.getValue().equals("OUI")) {
    					dispoPrime = 1;
    				}
    				ps2.setInt(4, dispoPrime);
                	ps2.executeUpdate();
                clearInputFields();
                tablearrete.getItems().removeAll(tablearrete.getItems());
                initialize();
                }else {
                	Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid input!");
                    alert.setContentText( "Nom du arrete est déjà inseré \n vous avez déjà inserer cette arrete \n");
                    alert.showAndWait();
                }
            } catch (SQLException e	) {
                Erreur.databaseProblem();
                e.printStackTrace();
            }
        }
		clearInputFields();
		initialize();
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
    nomArreteField.setText("");
    villeField.setText("");
    arreteChoiceBox.getSelectionModel().clearSelection();
    villeChoiceBox.getSelectionModel().clearSelection();
}

    void mouse(ActionEvent event) {
    	villeField.setEditable(false);
		 ObservableList<String> arrete = FXCollections.observableArrayList();
			
       try {
			
       	PreparedStatement st = conn.prepareStatement("select nom from Arretes where ville = ?");
       	st.setString(1, villeChoiceBox.getValue());
       	ResultSet rs = st.executeQuery();

       	while (rs.next()) {
       		arrete.add(rs.getString(1));
       		arreteChoiceBox.setItems(arrete);
       	}
		} catch (Exception e) {
			Erreur.databaseProblem();
		}
    }
	@FXML
	void  imprimer(ActionEvent event){


			PrinterJob job = PrinterJob.createPrinterJob();
			TableView<ArreteModel> tableView = new TableView<>();
			TableColumn<ArreteModel, Integer> ordreColumn = new TableColumn<>("L'ordre d'arrête dans le trajet");
			TableColumn<ArreteModel, String> villeColumn = new TableColumn<>("Ville");
			TableColumn<ArreteModel, String> nomColumn = new TableColumn<>("nom d'arrête");
//	    stageTitleColumn.setPrefWidth(80);
//	    stageSubjectColumn.setPrefWidth(370);

			// Set the cell value factories for the table columns
			ordreColumn.setCellValueFactory(new PropertyValueFactory<>("ordre"));
			villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));
			nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomArrete"));

			// Set the cell factory to wrap the text content
//	    stageTitleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
//	    stageSubjectColumn.setCellFactory(TextFieldTableCell.forTableColumn());

			tableView.getColumns().setAll( nomColumn ,villeColumn,ordreColumn);
			if (job != null) {
				ObservableList<ArreteModel> data = FXCollections.observableArrayList();
				try {
					PreparedStatement ps = conn.prepareStatement("SELECT * FROM Arretes where ordre!=0 order by ville asc,ordre");
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
							String nom = rs.getString(2);
							String ville = rs.getString(3);
							int ordre = rs.getInt(4);

							ArreteModel perso = new ArreteModel(nom,ville,ordre);
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
    void supprimerArrete(ActionEvent event) {
    	
    	ObservableList<ArreteModel> selectedItems = tablearrete.getItems().filtered(ArreteModel::isSelected);		
//		reorganisation des ordre

		if (selectedItems.size()==1) {
			ArreteModel arrete = selectedItems.get(0);
			int ordre = arrete.getOrdre();
			String ville = arrete.getVille();
			try {
				PreparedStatement ps2;
				ResultSet rs2;
				ps2 = conn.prepareStatement("select COUNT(idArrete) from  Arretes where ville = ?");
				ps2.setString(1, ville);
				rs2 = ps2.executeQuery();
				int nbArrete = 0 ;
				if (rs2.next()) {
					nbArrete = rs2.getInt(1);
				}
				if (nbArrete == 2) {
//    			on a une seul arrete
					PreparedStatement st = conn.prepareStatement("DELETE FROM Arretes WHERE nom = 'Aucun' and ville = ?");
					st.setInt(1, 1); 
					st.setString(1, arrete.getVille()); 
					st.executeUpdate();
					PreparedStatement psCheck = conn.prepareStatement("select Personnes.prenom from  Personnes,Arretes\r\n"
							+ "where Personnes.idArrete = Arretes.idArrete\r\n"
							+ "and   Arretes.nom = ?");
					psCheck.setString(1, arrete.getNomArrete());
					ResultSet rsCheck = psCheck.executeQuery();
					if (!rsCheck.next()) {
						try {
		    				PreparedStatement st3 = conn.prepareStatement("DELETE FROM Arretes WHERE nom = ?");
		    					st3.setString(1, arrete.getNomArrete()); 
		    					st3.executeUpdate();
		    			} catch (SQLException e) {
		    				Erreur.databaseProblem();
		    				e.printStackTrace();
		    			}
		    			// Optional: Refresh the TableView after the operation
		    			clearInputFields();
		    			tablearrete.getItems().removeAll(tablearrete.getItems());
		    			initialize();
					}else {
						Alert alert = new Alert(AlertType.ERROR);
			            alert.setTitle("Error");
			            alert.setHeaderText("Erreur de suppression");
			            alert.setContentText( "cette arrête est déjà associé à une personne \n");
			            alert.showAndWait();
					}
				}else {
					PreparedStatement psCheck = conn.prepareStatement("select Personnes.prenom from  Personnes,Arretes\r\n"
							+ "where Personnes.idArrete = Arretes.idArrete\r\n"
							+ "and   Arretes.nom = ?");
					psCheck.setString(1, arrete.getNomArrete());
					ResultSet rsCheck = psCheck.executeQuery();
					if (!rsCheck.next()) {
//						changer l'ordre des autres si  arrete à supprimer au milieu 
	    				PreparedStatement ps;
	    				ResultSet rs;
	    				ps = conn.prepareStatement("  select nom from Arretes where Arretes.ordre > ? and  ville=? order by ordre asc");
	    				ps.setInt(1, ordre);
	    				ps.setString(2, ville);
	    				rs = ps.executeQuery();
	    				if (rs.next()) {
	    					PreparedStatement ps1 = conn.prepareStatement("update Arretes set ordre = ? where nom = ? and ville=? ;");
	    					do {
	    						ps1.setInt(1, ordre);
	    						ps1.setString(2, rs.getString(1));
	    						ps1.setString(3,ville);
	    						ps1.addBatch();
	    						ordre++;
	    					} while (rs.next()); 
	    					ps1.executeBatch();
	    					try {
			    				PreparedStatement st = conn.prepareStatement("DELETE FROM Arretes WHERE nom = ?");
			    					st.setString(1, arrete.getNomArrete()); 
			    					st.executeUpdate();
			    			} catch (SQLException e) {
			    				Erreur.databaseProblem();
			    				e.printStackTrace();
			    			}
			    			// Optional: Refresh the TableView after the operation
			    			clearInputFields();
			    			tablearrete.getItems().removeAll(tablearrete.getItems());
			    			initialize();
	    				}else {
	    					try {
			    				PreparedStatement st = conn.prepareStatement("DELETE FROM Arretes WHERE nom = ?");
			    					st.setString(1, arrete.getNomArrete()); 
			    					st.executeUpdate();
			    			} catch (SQLException e) {
			    				Erreur.databaseProblem();
			    				e.printStackTrace();
			    			}
			    			// Optional: Refresh the TableView after the operation
			    			clearInputFields();
			    			tablearrete.getItems().removeAll(tablearrete.getItems());
			    			initialize();
						}
						
					}else {
						Alert alert = new Alert(AlertType.ERROR);
			            alert.setTitle("Error");
			            alert.setHeaderText("Erreur de suppression");
			            alert.setContentText( "cette arrête est déjà associé à une personne \n");
			            alert.showAndWait();
					}
				}
				
			} catch (Exception e) {
				Erreur.databaseProblem();
				e.printStackTrace();
			}
		}else {
		  Alert alert = new Alert(AlertType.ERROR);
          alert.setTitle("Error");
          alert.setHeaderText("Invalid input!");
          alert.setContentText( "il faut choisie dans chaque suppression une seule arrête\n");
          alert.showAndWait();
		}
		clearInputFields();
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
