package lagraa_yasser_assil_project.models;
import lagraa_yasser_assil_project.Enums.*;

public class Enseignant {
private int idEnseignant;
private String nom;
private Specialite specialite;

// Constructor
public Enseignant(int idEnseignant, String nom, Specialite specialite) {
    this.idEnseignant = idEnseignant; //PK so no setter for this field 
    this.nom = nom;
    this.specialite = specialite;}

// Getters
public int getIdEnseignant() {return idEnseignant; }   
public String getNom() {return nom; }   
public Specialite getSpecialite() {return specialite; }   

// Setters
public void setNom(String nom) { this.nom = nom; }
public void setSpecialite(Specialite specialite) { if(this.specialite == null) { this.specialite = specialite; } }

}
