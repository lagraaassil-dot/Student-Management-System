package lagraa_yasser_assil_project.models;

public class ModuleEtude {
    private Integer idModule;
    private String nomModule;
    private int coefficient;
    private int VolumeHoraire; // in minutes
    private Enseignant enseignant;

    public ModuleEtude(String nomModule, int coefficient, int volumeHoraire, Enseignant enseignant) {
        this.idModule = null;
        this.nomModule = nomModule;
        this.coefficient = coefficient;
        this.VolumeHoraire = volumeHoraire;
        this.enseignant = enseignant;
    }

    // Overload without a teacher — can be assigned later
    public ModuleEtude(String nomModule, int coefficient, int volumeHoraire) {
        this.idModule = null;
        this.nomModule = nomModule;
        this.coefficient = coefficient;
        this.VolumeHoraire = volumeHoraire;
        this.enseignant = null;
    }

    public Integer getIdModule() { return idModule; }
    public Enseignant getEnseignant() { return enseignant; }
    public String getNomModule() { return nomModule; }
    public int getCoefficient() { return coefficient; }
    public int getVolumeHoraire() { return VolumeHoraire; }

    public void setNomModule(String nomModule) { this.nomModule = nomModule; }
    public void setCoefficient(int coefficient) { if (coefficient > 0) this.coefficient = coefficient; }
    public void setVolumeHoraire(int volumeHoraire) { if (volumeHoraire >= 0 && volumeHoraire <= 1440) this.VolumeHoraire = volumeHoraire; }
    public void setEnseignant(Enseignant enseignant) { this.enseignant = enseignant; }
    public void setIdModule(Integer idModule) { this.idModule = idModule; }
}
