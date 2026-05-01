package lagraa_yasser_assil_project.Enums;

// All departments available in the institution.
public enum Departement {
    INFORMATIQUE("Informatique"),
    ELECTRONIQUE("Électronique"),
    ELECTROTECHNIQUE("Électrotechnique"),
    AUTOMATIQUE("Automatique"),
    GENIE_MECANIQUE("Génie Mécanique"),
    ENERGETIQUE("Énergétique"),
    GENIE_CIVIL("Génie Civil"),
    GENIE_CHIMIQUE("Génie Chimique / Procédés"),
    CHIMIE("Chimie"),
    PHYSIQUE("Physique"),
    BIOLOGIE("Biologie (SNV)"),
    GEOLOGIE("Géologie / STU");

    private final String label;
    Departement(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
