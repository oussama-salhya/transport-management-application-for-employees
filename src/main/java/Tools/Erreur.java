package Tools;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class  Erreur {
	public static void msgNotNumeric(String str) {
		Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Invalid input!");
        alert.setContentText( str + " doit etre un nombre");
        alert.showAndWait();
	}
	public static void msgNumeric(String str) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Erreur");
		alert.setHeaderText("Invalid input!");
		alert.setContentText( str + " doit etre des lettres");
		alert.showAndWait();
	}
	public static void msgEmptyfields() {
	    Alert alert = new Alert(AlertType.ERROR);
	    alert.setTitle("Erreur");
	    alert.setHeaderText("Certains champs sont vides!");
	    alert.setContentText("Veillez remplir tous les champs");
	    alert.showAndWait();
	}
	public static void databaseProblem() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Erreur");
		alert.setHeaderText("Probleme de connexion au serveur");
		alert.setContentText("Serveur n'est pas accessible.Veullez vérfier la connexion au serveur.");
		alert.showAndWait();
	}
	public static void msgKeyNotExist(String str) {
	    Alert alert = new Alert(AlertType.ERROR);
	    alert.setTitle("Erreur");
	    alert.setHeaderText(str + " n'existe pas dans la base de donnée !");
	    alert.setContentText("Reécrire " + str);
	    alert.showAndWait();
	}
	public static void msgInfoInvalid() {
	    Alert alert = new Alert(AlertType.ERROR);
	    alert.setTitle("Erreur");
	    alert.setHeaderText("Invalid User Name or Password !");
	    alert.setContentText("Veuillez vérifier vos informations ");
	    alert.showAndWait();
	}
	public static void msgNumberOutOfRange() {
	    Alert alert = new Alert(AlertType.ERROR);
	    alert.setTitle("Erreur");
	    alert.setHeaderText("Note Invalid!");
	    alert.setContentText("Veuillez vérifier les notes données ");
	    alert.showAndWait();
	}
	
}
