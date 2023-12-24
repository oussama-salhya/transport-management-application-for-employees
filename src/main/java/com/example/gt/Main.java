package com.example.gt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Calendar;


public class Main extends Application {

    static Calendar calendar = Calendar.getInstance();
    //    public static int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
    public static int weekNumber = 43;
    public static int yearNumber = calendar.get(Calendar.YEAR);
    public static int maxNumber = 6;



    @Override
    public void start(Stage primaryStage) throws IOException {
//Gestion des personnelles :
        //		GestionPerso.fxml
// Gestion des equipe :
        // 		gestionEquipes.fxml
// Gestion des arretes :
        //		gestionArrete.fxml
// la Gestion des  horraires des Equipe:
        //		gestionEquipeHorraire.fxml
// page des choix
        //		pagechoix.fxml
// page d'impression :
        //		imprimer.fxml
// gestion des primes
        //		gestionPrime.fxml
// gestion de la disponibilté des prime
        //		gestonDispoPrime.fxml
// gestion de la disponibilté des prime
        //		login.fxml

        // Get the current date
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root);
        primaryStage.initStyle(StageStyle.UNDECORATED);

        primaryStage.setScene(scene);
        primaryStage.show();

    }
    public static void main(String[] args) {
        launch(args);
    }

}
