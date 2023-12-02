package models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class PersoModel {
	private String numMat;
	private String nom;
	private String prenom;
	private String arrete;
	private String primeDispo;
	private Integer nbPrime;
	private Integer ordre;
	private String section;
	private String description;
	private int equipe;
	
	private BooleanProperty selected;
	
	
	public PersoModel(String nom, String prenom, String arrete, Integer ordre) {
		super();
		this.nom = nom;
		this.prenom = prenom;
		this.arrete = arrete;
		this.ordre = ordre;
	}

	public PersoModel(String numMat, String nom, String prenom, String primeDispo) {
		super();
		this.numMat = numMat;
		this.nom = nom;
		this.prenom = prenom;
		this.primeDispo = primeDispo;
		this.selected = new SimpleBooleanProperty(false);
	}
	public PersoModel(String numMat, String nom, String prenom, String section,int i) {
		super();
		this.numMat = numMat;
		this.nom = nom;
		this.prenom = prenom;
		this.section = section;
		this.selected = new SimpleBooleanProperty(false);
	}

	public PersoModel(String numMat, String nom, String prenom, String arrete, String primeDispo) {
		this.numMat = numMat;
		this.nom = nom;
		this.prenom = prenom;
		this.arrete = arrete;
		this.primeDispo = primeDispo;
		this.selected = new SimpleBooleanProperty(false);
	}
	public PersoModel(String numMat, String nom, String prenom, String arrete, String primeDispo,String section) {
		this.numMat = numMat;
		this.nom = nom;
		this.prenom = prenom;
		this.arrete = arrete;
		this.primeDispo = primeDispo;
		this.section = section;
		this.selected = new SimpleBooleanProperty(false);
	}
	
	
	public PersoModel(String nom, String prenom, Integer nbPrime) {
		this.nom = nom;
		this.prenom = prenom;
		this.nbPrime = nbPrime;
		this.selected = new SimpleBooleanProperty(false);
	}
	public PersoModel(String nom, String prenom, Integer nbPrime,String description) {
		this.nom = nom;
		this.prenom = prenom;
		this.nbPrime = nbPrime;
		this.description = description;
		this.selected = new SimpleBooleanProperty(false);
	}
	
	
	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public Integer getOrdre() {
		return ordre;
	}

	public void setOrdre(Integer ordre) {
		this.ordre = ordre;
	}
	public String getNumMat() {
		return numMat;
	}
	public void setNumMat(String numMat) {
		this.numMat = numMat;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getPrenom() {
		return prenom;
	}
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
	public String getArrete() {
		return arrete;
	}
	public void setArrete(String arrete) {
		this.arrete = arrete;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPrimeDispo() {
		return primeDispo;
	}
	public void setPrimeDispo(String primeDispo) {
		this.primeDispo = primeDispo;
	}
	
	public Integer getNbPrime() {
		return nbPrime;
	}

	public void setNbPrime(Integer nbPrime) {
		this.nbPrime = nbPrime;
	}

	public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
	
	
}
