package fr.unistra.dnum.apogee.ws.server.stub.domaine;

import gouv.education.apogee.commun.client.ws.AdministratifMetier.EtatIAADTO;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.EtatIAEDTO;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.ProfilEtuDTO;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.RegimeInsDTO;

public class Constantes {
    public static final String INSCRIPTION_PAYEE = "P";
    public static final EtatIAADTO IAA_EN_COURS = etatIAAEnCours();
    public static final EtatIAEDTO IAE_EN_COURS = etatIAEEnCours();
    public static final ProfilEtuDTO PROFIL_NORMAL = profilNormal();
    public static final RegimeInsDTO FORMATION_INITIALE = formationInitale();
    public static final RegimeInsDTO FORMATION_CONTINUE = formationContinue();
    public static final String OUI = "O";
    public static final String NON = "N";

    private Constantes() {}

    private static EtatIAADTO etatIAAEnCours() {
        EtatIAADTO iaEnCours = new EtatIAADTO();
        iaEnCours.setCodeEtatIAA("E");
        iaEnCours.setLibEtatIAA("En cours");
        iaEnCours.setTemDosIAA(OUI);
        return iaEnCours;
    }

    private static EtatIAEDTO etatIAEEnCours() {
        EtatIAEDTO iaEnCours = new EtatIAEDTO();
        iaEnCours.setCodeEtatIAE("E");
        iaEnCours.setLibEtatIAE("En cours");
        return iaEnCours;
    }

    private static ProfilEtuDTO profilNormal() {
        ProfilEtuDTO profilNormal = new ProfilEtuDTO();
        profilNormal.setCode("NO");
        profilNormal.setLibelle("Profil normal");
        profilNormal.setLibelleWeb("Profil normal");
        return profilNormal;
    }

    private static RegimeInsDTO formationInitale() {
        RegimeInsDTO formationInitale = new RegimeInsDTO();
        formationInitale.setCodRgi("1");
        formationInitale.setLibRgi("Formation initiale");
        return formationInitale;
    }

    private static RegimeInsDTO formationContinue() {
        RegimeInsDTO formationContinue = new RegimeInsDTO();
        formationContinue.setCodRgi("2");
        formationContinue.setLibRgi("Formation continue");
        return formationContinue;
    }


}
