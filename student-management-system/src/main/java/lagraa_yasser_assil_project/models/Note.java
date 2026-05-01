package lagraa_yasser_assil_project.models;

public class Note {
    private Integer idNote;
    private double valeur;
    private int TypeNote; // 0 = CC, 1 = Exam, 2 = Rattrapage
    private Inscription enrolment;

    public Note(double valeur, int TypeNote, boolean Session, Inscription enrolment) {
        this.valeur = valeur;
        this.TypeNote = TypeNote;
        this.enrolment = enrolment;
    }

    public Integer getIdNote() { return idNote; }
    public double getValeur() { return valeur; }
    public int getTypeNote() { return TypeNote; }
    public Inscription getEnrolment() { return enrolment; }

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
            throw new IllegalArgumentException("Le type de note doit être 0, 1 ou 2.");
        }
    }

    public void setEnrolment(Inscription enrolment) { this.enrolment = enrolment; }
    public void setIdNote(Integer idNote) { this.idNote = idNote; }
}
