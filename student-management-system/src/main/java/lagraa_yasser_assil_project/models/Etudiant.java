package lagraa_yasser_assil_project.models;

import java.util.Date;

public class Etudiant {
    private static int idCounter = 1; // Static counter to generate unique IDs
    private final int idEtudiant;
    private String nom;
    private String prenom;
    private Date dateNaissance;
    private String email;
    private Note[] notes; // Array to hold the student's grades, can be initialized later when modules are assigned


    public Etudiant( String nom, String prenom, Date dateNaissance, String email, Note[] notes) {

        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.email = email;
        this.notes = notes;

        /* function to generate ID
        assign findALL() to a list
        sort()
        find last element<- +1
         */
    }
    public Etudiant(int idEtudiant, String nom, String prenom, Date dateNaissance, String email) {
        this.idEtudiant = idCounter; // Assign unique ID and increment counter
        idCounter++;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.email = email;
        this.notes = new Note[3]; // Initialize with an empty array of 3 (cc, exam, rattrapage)
    }

    // Getters
    public int getIdEtudiant() { return idEtudiant; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public Date getDateNaissance() { return dateNaissance; }
    public String getEmail() { return email; }
    public Note[] getNotes() { return notes; }

    // Setters
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }
    public void setEmail(String email) { this.email = email; }
    public void setNotes(int Type, double valeur) {
        if (Type >= 0 && Type < notes.length) {
            notes[Type].setValeur(valeur);
        }
    }

}