package lagraa_yasser_assil_project.models;
import java.util.Date;
public class Enrolments {
private Integer idEnrolment;    
private Etudiant etudiant;
private Module module;
private Date dateInscription;
private Boolean IsValidated; //null if not taken any exam true if validated false if not -> flagged as eligible for tatrapage
//Constructor
public Enrolments(Etudiant etudiant,Module module,boolean IsValidated){
this.etudiant=etudiant;
this.module=module;
this.IsValidated=IsValidated;

this.dateInscription=new Date();
}

public Enrolments(Etudiant etudiant,Module module){
this.etudiant=etudiant;
this.module=module;
this.dateInscription=new Date();
}

//getters
public Integer getIdEnrolment() { return idEnrolment; }
public Etudiant getEtudiant() { return etudiant; }
public Module getModule() { return module; }
public Date getDateInscription() { return dateInscription; }
public Boolean getIsValidated() { return IsValidated; }
//setters
public void setIdEnrolment(Integer idEnrolment) { this.idEnrolment = idEnrolment; }
public void setEtudiant(Etudiant etudiant) { this.etudiant = etudiant; }
public void setModule(Module module) { this.module = module; }
public void setDateInscription(Date dateInscription) { this.dateInscription = dateInscription; }
public void setIsValidated(Boolean IsValidated) { this.IsValidated = IsValidated; }

}
