package lagraa_yasser_assil_project.models;
import lagraa_yasser_assil_project.Enums.*;

public class Enseignant {
private Integer idEnseignant;
private String nom;
private Specialite specialite;

// Constructor
public Enseignant( String nom, Specialite specialite) {
    this.nom = nom;
    this.specialite = specialite;}

// Getters
public Integer getIdEnseignant() {return idEnseignant; }   
public String getNom() {return nom; }   
public Specialite getSpecialite() {return specialite; }   

// Setters
public void setNom(String nom) { this.nom = nom; }
public void setSpecialite(Specialite specialite) { this.specialite = specialite;  }
public void setIdEnseignant(Integer idEnseignant) { this.idEnseignant = idEnseignant; }
}
