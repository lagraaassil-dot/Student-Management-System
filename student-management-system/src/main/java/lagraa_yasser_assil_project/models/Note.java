package lagraa_yasser_assil_project.models;

public class Note {
    private  Integer idNote;
    private double valeur;
    private int TypeNote; // 0 for continuous assessment, 1 for exam, 2 for rattrapage
    private Enrolments enrolment; // Association with Enrolments class

    public Note( double valeur, int TypeNote, boolean Session, Enrolments enrolment) {
        this.valeur = valeur;
        this.TypeNote = TypeNote;
        this.enrolment = enrolment;
    }

//getters
public Integer getIdNote() {
    return idNote;
}
public double getValeur() {
        return valeur;
}
public int getTypeNote() {
        return TypeNote;
    }
public Enrolments getEnrolment() {
        return enrolment;
    }    
//setters
    public void setValeur(double valeur) {
        if (valeur >= 0 && valeur <= 20) {
            this.valeur = valeur;
        } else {
            throw new IllegalArgumentException("La valeur de la note doit être comprise entre 0 et 20.");
        }
    }
    public void setTypeNote(int TypeNote) {
        if (TypeNote == 0 || TypeNote == 1 || TypeNote == 2) {
            this.TypeNote = TypeNote;
        } else {
            throw new IllegalArgumentException("Le type de note doit être 0 (continuous assessment), 1 (exam) ou 2 (rattrapage).");
        }
    } 
    public void setEnrolment(Enrolments enrolment) {
        this.enrolment = enrolment;
    }   
    public void setIdNote(Integer idNote) {
       this.idNote = idNote;
    }

        
}

    
