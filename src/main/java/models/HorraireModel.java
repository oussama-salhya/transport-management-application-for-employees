package models;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class HorraireModel {
	private Integer numEquipe;
	private Integer heureDeb;
	private Integer heureFin;
	private BooleanProperty selected;
	public HorraireModel(Integer numEquipe, Integer heureDeb, Integer heureFin) {
		super();
		this.numEquipe = numEquipe;
		this.heureDeb = heureDeb;
		this.heureFin = heureFin;
		this.selected = new SimpleBooleanProperty(false);
	}
	
	
	public Integer getNumEquipe() {
		return numEquipe;
	}
	public void setNumEquipe(Integer numEquipe) {
		this.numEquipe = numEquipe;
	}
	public Integer getHeureDeb() {
		return heureDeb;
	}
	public void setHeureDeb(Integer heureDeb) {
		this.heureDeb = heureDeb;
	}
	public Integer getHeureFin() {
		return heureFin;
	}
	public void setHeureFin(Integer heureFin) {
		this.heureFin = heureFin;
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
