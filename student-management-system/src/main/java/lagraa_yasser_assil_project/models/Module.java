package lagraa_yasser_assil_project.models;

public class Module {
private int idModule;
private String nomModule;
private int coefficient;
private int VolumeHoraire;//in minutes

// Constructor
public Module(int idModule, String nomModule, int coefficient, int volumeHoraire) {
    this.idModule = idModule;
    this.nomModule = nomModule;
    this.coefficient = coefficient;
    this.VolumeHoraire = volumeHoraire;
}

// Getters
public int getIdModule() { return idModule; }
public String getNomModule() { return nomModule; }
public int getCoefficient() { return coefficient; }
public int getVolumeHoraire() { return VolumeHoraire; }
// Setters
public void setNomModule(String nomModule) { this.nomModule = nomModule; }
public void setCoefficient(int coefficient) { if(coefficient > 0) this.coefficient = coefficient; }
public void setVolumeHoraire(int volumeHoraire) { if (volumeHoraire >= 0&&volumeHoraire<=1440)this.VolumeHoraire = volumeHoraire; }
}
