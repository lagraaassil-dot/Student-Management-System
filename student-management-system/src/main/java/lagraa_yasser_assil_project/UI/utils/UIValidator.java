package lagraa_yasser_assil_project.UI.utils;

import java.util.regex.Pattern;

/**
 * UIValidator — static utility methods for validating user input.
 *
 * Grade format rules (from spec):
 *   • Value must be in [0, 20]
 *   • Decimal part must be one of: .00, .25, .50, .75 (or integer, e.g. "14")
 *   • Invalid examples: 15.3, 21, -1, 10.1
 *   • Valid examples  : 0, 7.25, 14.50, 20, 9.75
 */
public final class UIValidator {

    private UIValidator() {} // static utility class

    // ── Grade validation ──────────────────────────────────────────────────────

    private static final Pattern GRADE_PATTERN =
            Pattern.compile("^(\\d{1,2})(\\.(?:00|25|50|75|0|5))?$");

    /**
     * Returns true if the string represents a valid grade.
     * Accepts integer values (e.g. "14") and values ending in .00/.25/.50/.75
     * (also .0 and .5 as shortcuts).
     * Value must be in the range [0, 20].
     */
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

    /**
     * Parses a valid grade string into a double.
     * Throws IllegalArgumentException if the input is invalid.
     */
    public static double parseGrade(String input) {
        if (!isValidGrade(input)) {
            throw new IllegalArgumentException(
                "Note invalide : \"" + input + "\". "
              + "La valeur doit être entre 0 et 20, "
              + "avec une décimale en .00 / .25 / .50 / .75 seulement.");
        }
        return Double.parseDouble(input.trim());
    }

    /**
     * Returns a human-readable error message for an invalid grade,
     * or null if the grade is valid.
     */
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
        return null; // valid
    }

    // ── General field validators ──────────────────────────────────────────────

    /** True if the string is non-null and non-blank. */
    public static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    /** True if the string is a valid email matching the project's allowed domains. */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@(gmail|hotmail|usthb|yahoo)\\.com$");
    }

    /**
     * True if the string represents a positive integer.
     * Used for coefficient and volume horaire validation.
     */
    public static boolean isPositiveInt(String s) {
        if (s == null || s.isBlank()) return false;
        try {
            return Integer.parseInt(s.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * True if the string represents an integer in [0, 1440] (minutes in a day).
     * Used for VolumeHoraire validation.
     */
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
