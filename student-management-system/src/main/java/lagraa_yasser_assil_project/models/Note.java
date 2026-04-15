package lagraa_yasser_assil_project.models;

public class Note {
    private static int idCounter = 1; // Static counter to generate unique IDs
    private final int idNote;
    private final int idEtudiant;
    private final int idModule;
    private double valeur;
    private int TypeNote; // 0 for continuous assessment, 1 for exam, 2 for rattrapage
    private boolean Session; // true for normal session, false for rattrapage

    public Note(int idEtudiant, int idModule, double valeur, int TypeNote, boolean Session) {
        this.idNote = idCounter; // Assign unique ID and increment counter
        idCounter++;
        this.idEtudiant = idEtudiant;
        this.idModule = idModule;
        this.valeur = valeur;
        this.TypeNote = TypeNote;
        this.Session = Session;
    }
        public Note(int idEtudiant, int idModule, double valeur, int TypeNote) {
        this.idNote = idCounter; // Assign unique ID and increment counter
        idCounter++;
        this.idEtudiant = idEtudiant;
        this.idModule = idModule;
        this.valeur = valeur;
        this.TypeNote = TypeNote;
        this.Session = true; // Default to normal session
    }
//getters
public int getiIdEtudiant(){
    return idEtudiant;
}
public int getiIdModule(){
    return idModule;
}
public double getId(){
    return idNote;
}
public int getValeur() {
        return (int) valeur;
}
public int getTypeNote() {
        return TypeNote;
    }
public boolean isSession() {
        return Session;
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
        if (TypeNote == 1 || TypeNote == 2 || TypeNote == 3) {
            this.TypeNote = TypeNote;
        } else {
            throw new IllegalArgumentException("Le type de note doit être 1 (exam), 2 (continuous assessment) ou 3 (rattrapage).");
        }
    }    

    public void setSession(boolean Session) {
        this.Session = Session;
    }
        
}

    
