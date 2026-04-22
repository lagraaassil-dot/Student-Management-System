package lagraa_yasser_assil_project.models;

import java.util.Date;

public class Etudiant {
    private Integer idEtudiant;
    private String nom;
    private String prenom;
    private Date dateNaissance;
    private String email;
    private boolean isDiplome; // true if the student has graduated, false otherwise
   


    // Constructor
        public Etudiant( String nom, String prenom, Date dateNaissance, String email) {  
        idEtudiant=null;     
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.email = email;
        this.isDiplome = false; // Default to not graduated
    }


    // Getters
    public Integer getIdEtudiant() { return idEtudiant; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public Date getDateNaissance() { return dateNaissance; }
    public String getEmail() { return email; }
    public boolean isDiplome() { return isDiplome; }


    // Setters
    public void setId(Integer id){if (this.idEtudiant==null)this.idEtudiant=id;}
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }
    public void setEmail(String email) {if(email.matches("^[A-Za-z0-9+_.-]+@(gmail|hotmail|usthb|yahoo)\\.com$")) this.email = email; }
    public void setDiplome (boolean isDiplome) { this.isDiplome = isDiplome; }



    



}