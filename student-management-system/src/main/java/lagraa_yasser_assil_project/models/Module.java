package lagraa_yasser_assil_project.models;

public class Module {
private Integer idModule;
private String nomModule;
private int coefficient;
private int VolumeHoraire;//in minutes
private Enseignant enseignant;

// Constructor
public Module( String nomModule, int coefficient, int volumeHoraire, Enseignant enseignant) {
    this.idModule = null; // ID will be set by the database
    this.nomModule = nomModule;
    this.coefficient = coefficient;
    this.VolumeHoraire = volumeHoraire; //in minutes
    this.enseignant = enseignant;
}

public Module( String nomModule, int coefficient, int volumeHoraire) {
    this.idModule = null; // ID will be set by the database
    this.nomModule = nomModule;
    this.coefficient = coefficient;
    this.VolumeHoraire = volumeHoraire;
    this.enseignant = null; // Will be set later
}

// Getters
public Integer getIdModule() { return idModule; }
public Enseignant getEnseignant() { return enseignant; }
public String getNomModule() { return nomModule; }
public int getCoefficient() { return coefficient; }
public int getVolumeHoraire() { return VolumeHoraire; }
// Setters
public void setNomModule(String nomModule) { this.nomModule = nomModule; }
public void setCoefficient(int coefficient) { if(coefficient > 0) this.coefficient = coefficient; }
public void setVolumeHoraire(int volumeHoraire) { if (volumeHoraire >= 0&&volumeHoraire<=1440)this.VolumeHoraire = volumeHoraire; }
public void setEnseignant(Enseignant enseignant) { this.enseignant = enseignant; }
public void setIdModule(Integer idModule) { this.idModule = idModule; }
}
