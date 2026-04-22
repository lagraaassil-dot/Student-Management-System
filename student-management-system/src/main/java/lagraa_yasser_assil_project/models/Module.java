package lagraa_yasser_assil_project.models;

public class Module {
private Integer idModule;
private String nomModule;
private int coefficient;
private int VolumeHoraire;//in minutes
private Integer idEnseignant; // Foreign key to the Enseignant table

// Constructor
public Module( String nomModule, int coefficient, int volumeHoraire, Integer idEnseignant) {
    this.idModule = null; // ID will be set by the database
    this.nomModule = nomModule;
    this.coefficient = coefficient;
    this.VolumeHoraire = volumeHoraire; //in minutes
    this.idEnseignant = idEnseignant;
}

public Module( String nomModule, int coefficient, int volumeHoraire) {
    this.idModule = null; // ID will be set by the database
    this.nomModule = nomModule;
    this.coefficient = coefficient;
    this.VolumeHoraire = volumeHoraire;
    this.idEnseignant = null; // ID will be set later
}

// Getters
public Integer getIdModule() { return idModule; }
public Integer getIdEnseignant() { return idEnseignant; }
public String getNomModule() { return nomModule; }
public int getCoefficient() { return coefficient; }
public int getVolumeHoraire() { return VolumeHoraire; }
// Setters
public void setNomModule(String nomModule) { this.nomModule = nomModule; }
public void setCoefficient(int coefficient) { if(coefficient > 0) this.coefficient = coefficient; }
public void setVolumeHoraire(int volumeHoraire) { if (volumeHoraire >= 0&&volumeHoraire<=1440)this.VolumeHoraire = volumeHoraire; }
public void setIdEnseignant(Integer idEnseignant) { this.idEnseignant = idEnseignant; }
public void setIdModule(Integer idModule) { if (this.idModule==null)this.idModule = idModule; }
}
