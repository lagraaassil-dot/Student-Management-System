package lagraa_yasser_assil_project.models;

import java.util.Date;

public class Etudiant {
    private Integer idEtudiant;
    private String nom;
    private String prenom;
    private Date dateNaissance;
    private String email;
    private boolean isDiplome; // flipped to true once all modules are validated

    public Etudiant(String nom, String prenom, Date dateNaissance, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.email = email;
        this.isDiplome = false;
    }

    // Getters
    public Integer getIdEtudiant() { return idEtudiant; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public Date getDateNaissance() { return dateNaissance; }
    public String getEmail() { return email; }
    public boolean isDiplome() { return isDiplome; }

    // Setters
    public void setId(Integer id) { this.idEtudiant = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }
    public void setDiplome(boolean isDiplome) { this.isDiplome = isDiplome; }

    // Only accept addresses from known domains
    public void setEmail(String email) {
        if (email.matches("^[A-Za-z0-9+_.-]+@(gmail|hotmail|usthb|yahoo)\\.com$"))
            this.email = email;
    }
}
