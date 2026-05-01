package lagraa_yasser_assil_project.UI.utils;

import java.util.regex.Pattern;

// Static helpers for validating grades and common field formats.
public final class UIValidator {

    private UIValidator() {} 

    

    private static final Pattern GRADE_PATTERN =
            Pattern.compile("^(\\d{1,2})(\\.(?:00|25|50|75|0|5))?$");

    
    public static boolean isValidGrade(String input) {
        if (input == null || input.isBlank()) return false;
        if (!GRADE_PATTERN.matcher(input.trim()).matches()) return false;
        try {
            double v = Double.parseDouble(input.trim());
            return v >= 0.0 && v <= 20.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    
    public static double parseGrade(String input) {
        if (!isValidGrade(input)) {
            throw new IllegalArgumentException(
                "Note invalide : \"" + input + "\". "
              + "La valeur doit être entre 0 et 20, "
              + "avec une décimale en .00 / .25 / .50 / .75 seulement.");
        }
        return Double.parseDouble(input.trim());
    }

    
    public static String gradeErrorMessage(String input) {
        if (input == null || input.isBlank())
            return "La note ne peut pas être vide.";
        if (!GRADE_PATTERN.matcher(input.trim()).matches())
            return "Format invalide. Utilisez XX, XX.25, XX.50 ou XX.75.";
        try {
            double v = Double.parseDouble(input.trim());
            if (v < 0)  return "La note ne peut pas être négative.";
            if (v > 20) return "La note ne peut pas dépasser 20.";
        } catch (NumberFormatException e) {
            return "Valeur numérique non reconnue.";
        }
        return null; 
    }

    

    
    public static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@(gmail|hotmail|usthb|yahoo)\\.com$");
    }

    
    public static boolean isPositiveInt(String s) {
        if (s == null || s.isBlank()) return false;
        try {
            return Integer.parseInt(s.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    
    public static boolean isValidVolumeHoraire(String s) {
        if (s == null || s.isBlank()) return false;
        try {
            int v = Integer.parseInt(s.trim());
            return v >= 0 && v <= 1440;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
