package lagraa_yasser_assil_project;

import lagraa_yasser_assil_project.dao.*;
import lagraa_yasser_assil_project.models.*;
import lagraa_yasser_assil_project.Enums.Specialite;
import java.util.Date;
import java.util.List;

public class App {
    public static void main(String[] args) {
        // Initialize DAOs
        EtudiantDAO etudiantDAO = new EtudiantDAO();
        EnseignantDAO enseignantDAO = new EnseignantDAO();
        ModuleDAO moduleDAO = new ModuleDAO();
        InscriptionDAO inscriptionDAO = new InscriptionDAO();
        NoteDAO noteDAO = new NoteDAO();

        System.out.println("--- STARTING AUTOMATED TEST ---");

        // 1. Setup: Create Teacher and Student
        Enseignant prof = new Enseignant("Dr. Mekahlia", Specialite.IA);
        enseignantDAO.addEnseignant(prof);
        System.out.println("Added Teacher: " + prof.getNom() + " (ID: " + prof.getIdEnseignant() + ")");

        Etudiant s1 = new Etudiant("Lagraa", "Assil", new Date(), "assil@usthb.com");
        etudiantDAO.addEtudiant(s1);
        System.out.println("Added Student: " + s1.getNom() + " (ID: " + s1.getIdEtudiant() + ")");

        // 2. Module: Create and link to Teacher
        ModuleEtude oop = new ModuleEtude("OOP Java", 3, 90, prof);
        moduleDAO.addModule(oop);
        System.out.println("Added Module: " + oop.getNomModule());

        // 3. Enrollment: Enroll student in the module
        inscriptionDAO.enrollStudent(s1, oop);
        Inscription enrollment = inscriptionDAO.getInscriptionById(inscriptionDAO.getInscriptionsByStudent(s1.getIdEtudiant()).get(0).getIdInscription());
        System.out.println("Student enrolled in OOP. Inscription ID: " + enrollment.getIdInscription());

        // 4. Grading (Normal Session): Fail the student first (CC: 8, Exam: 9)
        // Avg = (8*0.4 + 9*0.6) = 3.2 + 5.4 = 8.6 (Fail)
        noteDAO.addNote(new Note(8.0, 0, true, enrollment)); // CC
        noteDAO.addNote(new Note(9.0, 1, true, enrollment)); // Exam
        
        double firstAvg = inscriptionDAO.calculateAndSaveAverage(enrollment.getIdInscription());
        System.out.println("First Session Average: " + firstAvg + " | Validated: " + 
                            inscriptionDAO.getInscriptionById(enrollment.getIdInscription()).getIsValidated());

        // 5. Resit (Rattrapage): Add a resit grade (14.0)
        // Best Exam = max(9, 14) = 14. 
        // New Avg = (8*0.4 + 14*0.6) = 3.2 + 8.4 = 11.6 (Pass)
        noteDAO.addNote(new Note(14.0, 2, false, enrollment)); 
        double finalAvg = inscriptionDAO.calculateAndSaveAverageAfterResit(enrollment.getIdInscription());
        System.out.println("After Resit Average: " + finalAvg + " | Validated: " + 
                            inscriptionDAO.getInscriptionById(enrollment.getIdInscription()).getIsValidated());

        // 6. Diploma: Check if student graduated
        boolean graduated = etudiantDAO.checkAndSetDiploma(s1);
        System.out.println("Student Graduated? " + s1.isDiplome());

        System.out.println("--- TEST COMPLETE ---");
    }
}