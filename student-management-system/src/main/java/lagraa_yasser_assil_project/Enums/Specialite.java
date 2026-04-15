package lagraa_yasser_assil_project.Enums;

public enum Specialite {
    // INFO
    GL("Génie Logiciel", Departement.INFORMATIQUE),
    IA("Intelligence Artificielle", Departement.INFORMATIQUE),
    CYBER("Cybersécurité", Departement.INFORMATIQUE),
    SI("Systèmes d'Information", Departement.INFORMATIQUE),
    BDD("Bases de Données", Departement.INFORMATIQUE),
    RESEAUX("Réseaux Informatiques", Departement.INFORMATIQUE),
    DISTRIB("Systèmes Distribués", Departement.INFORMATIQUE),
    DATA("Data Science / Big Data", Departement.INFORMATIQUE),
    THEORIE("Informatique Théorique", Departement.INFORMATIQUE),
    VISION("Vision par Ordinateur", Departement.INFORMATIQUE),
    IMAGE("Traitement d'Images", Departement.INFORMATIQUE),
    IHM("Interaction Homme-Machine", Departement.INFORMATIQUE),
    HPC("Calcul Haute Performance", Departement.INFORMATIQUE),
    CLOUD("Cloud Computing", Departement.INFORMATIQUE),

    // ELECTRONIQUE
    SIGNAL("Traitement du Signal", Departement.ELECTRONIQUE),
    TELECOM("Télécommunications", Departement.ELECTRONIQUE),
    MICRO_E("Microélectronique", Departement.ELECTRONIQUE),
    EMBARQUE("Systèmes Embarqués", Departement.ELECTRONIQUE),
    INSTRUM("Instrumentation", Departement.ELECTRONIQUE),
    CAPTEURS("Réseaux de Capteurs", Departement.ELECTRONIQUE),
    SANS_FIL("Communications Sans Fil", Departement.ELECTRONIQUE),
    RADAR("Radar & Antennes", Departement.ELECTRONIQUE),

    // ELECTROTECHNIQUE
    MACHINES("Machines Électriques", Departement.ELECTROTECHNIQUE),
    RESEAUX_E("Réseaux Électriques", Departement.ELECTROTECHNIQUE),
    RENOUV_E("Énergies Renouvelables", Departement.ELECTROTECHNIQUE),
    COMMANDE_E("Commande Électrique", Departement.ELECTROTECHNIQUE),
    PUISSANCE("Électronique de Puissance", Departement.ELECTROTECHNIQUE),
    SMART_GRID("Smart Grid", Departement.ELECTROTECHNIQUE),

    // AUTOMATIQUE
    AUTO_IND("Automatique Industrielle", Departement.AUTOMATIQUE),
    ROBOTIQUE("Robotique", Departement.AUTOMATIQUE),
    COMMANDE_S("Commande des Systèmes", Departement.AUTOMATIQUE),
    NON_LINEAIRE("Systèmes Non Linéaires", Departement.AUTOMATIQUE),
    AUTO_AV("Automatique Avancée", Departement.AUTOMATIQUE),
    CTRL_OPT("Contrôle Optimal", Departement.AUTOMATIQUE),

    // GENIE MECANIQUE
    CONCEPTION("Conception Mécanique", Departement.GENIE_MECANIQUE),
    RDM("Résistance des Matériaux", Departement.GENIE_MECANIQUE),
    DYNAMIQUE("Dynamique des Structures", Departement.GENIE_MECANIQUE),
    VIBRATIONS("Vibrations", Departement.GENIE_MECANIQUE),
    MAINTENANCE("Maintenance Industrielle", Departement.GENIE_MECANIQUE),
    USINAGE("Fabrication / Usinage", Departement.GENIE_MECANIQUE),
    MECATRONIQUE("Mécatronique", Departement.GENIE_MECANIQUE),

    // ENERGETIQUE
    THERMIQUE("Thermique", Departement.ENERGETIQUE),
    MACHINES_T("Machines Thermiques", Departement.ENERGETIQUE),
    RENOUV_T("Énergies Renouvelables", Departement.ENERGETIQUE),
    FROID("Froid & Climatisation", Departement.ENERGETIQUE),
    TRANSFERT("Transfert Thermique", Departement.ENERGETIQUE),

    // GENIE CIVIL
    STRUCTURES("Structures", Departement.GENIE_CIVIL),
    GEOTECH("Géotechnique", Departement.GENIE_CIVIL),
    HYDRAULIQUE("Hydraulique", Departement.GENIE_CIVIL),
    MATERIAUX("Matériaux de Construction", Departement.GENIE_CIVIL),
    ART("Ouvrages d'Art", Departement.GENIE_CIVIL),
    PARASISMIQUE("Génie Parasismique", Departement.GENIE_CIVIL),

    // GENIE CHIMIQUE
    PROCEDES("Génie des Procédés", Departement.GENIE_CHIMIQUE),
    PETROCHIMIE("Pétrochimie", Departement.GENIE_CHIMIQUE),
    RAFFINAGE("Raffinage", Departement.GENIE_CHIMIQUE),
    CATALYSE("Catalyse", Departement.GENIE_CHIMIQUE),
    ENVIRONNEMENT("Génie Environnemental", Departement.GENIE_CHIMIQUE),
    EAUX("Traitement des Eaux", Departement.GENIE_CHIMIQUE),

    // CHIMIE
    C_ORG("Chimie Organique", Departement.CHIMIE),
    C_INORG("Chimie Inorganique", Departement.CHIMIE),
    C_ANAL("Chimie Analytique", Departement.CHIMIE),
    C_PHYS("Chimie Physique", Departement.CHIMIE),
    C_MAT("Chimie des Matériaux", Departement.CHIMIE),

    // PHYSIQUE
    P_THEO("Physique Théorique", Departement.PHYSIQUE),
    P_MAT("Physique des Matériaux", Departement.PHYSIQUE),
    OPTIQUE("Optique", Departement.PHYSIQUE),
    P_NUC("Physique Nucléaire", Departement.PHYSIQUE),
    P_ENERG("Physique Énergétique", Departement.PHYSIQUE),

    // BIOLOGIE
    MICROBIO("Microbiologie", Departement.BIOLOGIE),
    BIOTECH("Biotechnologie", Departement.BIOLOGIE),
    GENETIQUE("Génétique", Departement.BIOLOGIE),
    PHYSIO("Physiologie", Departement.BIOLOGIE),
    BIOCHIMIE("Biochimie", Departement.BIOLOGIE),

    // GEOLOGIE
    GEOL("Géologie", Departement.GEOLOGIE),
    GEOPHYS("Géophysique", Departement.GEOLOGIE),
    HYDRO("Hydrogéologie", Departement.GEOLOGIE),
    SEDIM("Sédimentologie", Departement.GEOLOGIE),
    PETROLE("Géologie Pétrolière", Departement.GEOLOGIE);

    private final String label;
    private final Departement departement;

    Specialite(String label, Departement departement) {
        this.label = label;
        this.departement = departement;
    }

    public Departement getDepartement() { return departement; }

    @Override
    public String toString() { return label; }
}