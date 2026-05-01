package lagraa_yasser_assil_project.models;
import java.util.Date;

public class Inscription {
    private Integer idInscription;
    private Etudiant etudiant;
    private ModuleEtude module;
    private Date dateInscription;
    // null = not yet examined, true = passed, false = eligible for rattrapage
    private Boolean IsValidated;

    public Inscription(Etudiant etudiant, ModuleEtude module, boolean IsValidated) {
        this.etudiant = etudiant;
        this.module = module;
        this.IsValidated = IsValidated;
        this.dateInscription = new Date();
    }

    public Inscription(Etudiant etudiant, ModuleEtude module) {
        this.etudiant = etudiant;
        this.module = module;
        this.dateInscription = new Date();
    }

    public Integer getIdInscription() { return idInscription; }
    public Etudiant getEtudiant() { return etudiant; }
    public ModuleEtude getModule() { return module; }
    public Date getDateInscription() { return dateInscription; }
    public Boolean getIsValidated() { return IsValidated; }

    public void setIdInscription(Integer idInscription) { this.idInscription = idInscription; }
    public void setEtudiant(Etudiant etudiant) { this.etudiant = etudiant; }
    public void setModule(ModuleEtude module) { this.module = module; }
    public void setDateInscription(Date dateInscription) { this.dateInscription = dateInscription; }
    public void setIsValidated(Boolean IsValidated) { this.IsValidated = IsValidated; }
}
