package models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ArreteModel {
	private String nomArrete ;
	private String ville;
	private Integer ordre;
	private BooleanProperty selected;
	public ArreteModel(String nomArrete, String ville, Integer ordre) {
		super();
		this.nomArrete = nomArrete;
		this.ville = ville;
		this.ordre = ordre;
		this.selected = new SimpleBooleanProperty(false);
	}
	public String getNomArrete() {
		return nomArrete;
	}
	public void setNomArrete(String nomArrete) {
		this.nomArrete = nomArrete;
	}
	public String getVille() {
		return ville;
	}
	public void setVille(String ville) {
		this.ville = ville;
	}
	public Integer getOrdre() {
		return ordre;
	}
	public void setOrdre(Integer ordre) {
		this.ordre = ordre;
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
