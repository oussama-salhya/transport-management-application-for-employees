package com.example.gt;

import Tools.Erreur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class login {
	Connection conn = Connexion.getConnection();
    @FXML
    private Button exitButton;

    @FXML
    private PasswordField  passwordField;

    @FXML
    private TextField userField;

    @FXML
    void exit(ActionEvent event) {
    	Stage stage = new Stage();
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		stage.close();
    }

    @FXML
    void login(ActionEvent event) {
    	if (userField.getText().isEmpty() || passwordField.getText().isEmpty()) {
			Erreur.msgEmptyfields();
		}else {
			String user = userField.getText();
			String password = passwordField.getText();
			try {
				PreparedStatement ps = conn.prepareStatement("select atelier from Chefs,Comptes\r\n"
						+ "where Chefs.idCompte = Comptes.idCompte \r\n"
						+ "and utilisateur = ?\r\n"
						+ "and password = ?");
				ps.setString(1, user);
				ps.setString(2, password);
				
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					try {
					String section = rs.getString(1);
					if (section.equals("admin")) {
						FXMLLoader loader = new FXMLLoader(getClass().getResource("gestionEquipesRespoGT.fxml"));
				        Parent root;
							root = loader.load();
							Scene scene = new Scene(root);
							Stage stage = new Stage();
							stage.initStyle(StageStyle.UNDECORATED);
							stage.setScene(scene);
							stage.show();
							stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
							stage.close();
					}else {
						GestionEquipesChefController.section = section;
						FXMLLoader loader = new FXMLLoader(getClass().getResource("gestionEquipesChef.fxml"));
				        Parent root;
							root = loader.load();
							Scene scene = new Scene(root);
							Stage stage = new Stage();
							stage.initStyle(StageStyle.UNDECORATED);
							stage.setScene(scene);
							stage.show();
							stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
							stage.close();
					}
					
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("Invalid input!");
					alert.setContentText( "Nom d'utilisateur ou Mot de pass est invalid\n");
					alert.showAndWait();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

}
