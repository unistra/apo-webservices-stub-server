package fr.unistra.dnum.apogee.ws.server.stub.presentation.ws;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import fr.unistra.dnum.apogee.ws.server.stub.test.TestDataSet;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootPresentationTest
@TestDataSet("""
- codInd: 12345678
  infoAdm:
      prenom1: Estelle
      nomPatronymique: ETUDIANTE
      numEtu: 12345678
      numeroINE: 201234567AB
      sexe: F
  coordonnees:
  - email: estelle@example.org
    emailAnnuaire: estelle.etudiante@etu.univ.fr
    loginAnnuaire: estelle
    adresseAnnuelle: &TourEiffel
      libAd1: Champ-de-Mars
      libAd2: Av. Gustave Eiffel
      commune:
        codePostal: "75007"
        nomCommune: PARIS
      pays: &France
        codPay: "100"
        libPay: FRANCE
    adresseFixe: &PorteDeBrandebourg
      libAde: Pariser Platz, 10117 Berlin
      pays: &Allemagne
        codPay: "109"
        libPay: ALLEMAGNE
  ias:
  - statut: &Etudiant { code: '01', libelle: Etudiant, libelleWeb: Etudiant }
    cpam: { libCpam: CPAM }
    regimeIns: &FormationInitiale { codRgi: '1', libRgi: Formation Initiale }
    #regimeIns: &FormationContinue { codRgi: '2', libRgi: Formation Continue }
  etapes:
  - cge: &CGE { codeCGE: CGE, libCGE: Centre de Gestion }
    composante: &COMP { codComposante: COMP, libComposante: Composante }
    diplome: &LT
        codeDiplome: LT
        libLongDiplome: Licence de Test
        libVdi: Licence TEST
        libWebVdi: LICENCE de test
        versionDiplome: '321'
    etape: &LT1
        codeEtp: LT1
        versionEtp: '321'
        libLongEtp: Licence de test première année
        libWebVet: LICENCE de test première année
    regimeIns: *FormationInitiale
    #regimeIns: *FormationContinue
""")
class EtudiantMetierEndpointTest {
    @DynamicPropertySource
    static void setRandomServerPort(DynamicPropertyRegistry registry) {
        int port = TestSocketUtils.findAvailableTcpPort();
        registry.add("server.port", () -> port);
    }

    @Autowired EtudiantMetierServiceInterface etudiantMetier;
    static final String ANNEE = Person.PersonBuilder.anneeU(LocalDateTime.now());

    /** @see EtudiantMetierEndpoint#recupererIdentifiantsEtudiantV2(RecupererIdentifiantsEtudiantV2) */
    @Test void recupererIdentifiantsEtudiantV2() throws WebBaseException_Exception {
        IdentifiantsEtudiantDTO2 etudiant =
            etudiantMetier.recupererIdentifiantsEtudiantV2(
                /*_codEtu*/ "12345678",
                /*_codInd*/ null,
                /*_numINE*/ null,
                /*_numBoursier*/ null,
                /*_codOPI*/ null,
                /*_nom*/ null,
                /*_prenom*/ null,
                /*_dateNaiss*/ null,
                /*_temoinRecupAnnu*/ null
            );
        assertNotNull(etudiant);
        assertEquals(12345678, etudiant.getCodEtu());
        assertEquals(12345678,etudiant.getCodInd());
        assertEquals("201234567AB",etudiant.getNumeroINE());
    }

    /** @see EtudiantMetierEndpoint#recupererInfosAdmEtuV4(RecupererInfosAdmEtuV4)  */
    @Test void recupererInfosAdmEtuV4() throws WebBaseException_Exception {
        InfoAdmEtuDTO4 etudiant = etudiantMetier.recupererInfosAdmEtuV4(
                /*_codEtu*/ "12345678"
        );

        assertNotNull(etudiant);
        assertEquals("ETUDIANTE", etudiant.getNomPatronymique());
        assertEquals("Estelle", etudiant.getPrenom1());
        assertEquals(12345678, etudiant.getNumEtu());
        assertEquals("201234567AB", etudiant.getNumeroINE());
        assertEquals("F", etudiant.getSexe());
    }


    /** @see EtudiantMetierEndpoint#recupererInfosAdmEtuV3(RecupererInfosAdmEtuV3)  */
    @Test void recupererInfosAdmEtuV3() throws WebBaseException_Exception {
        InfoAdmEtuDTO3 etudiant = etudiantMetier.recupererInfosAdmEtuV3(
                /*_codEtu*/ "12345678"
        );

        assertNotNull(etudiant);
        assertEquals("ETUDIANTE", etudiant.getNomPatronymique());
        assertEquals("Estelle", etudiant.getPrenom1());
        assertEquals(12345678, etudiant.getNumEtu());
        assertEquals("201234567AB", etudiant.getNumeroINE());
        assertEquals("F", etudiant.getSexe());
    }

    /** @see EtudiantMetierEndpoint#recupererAdressesEtudiantV2(RecupererAdressesEtudiantV2) */
    @Test void recupererAdressesEtudiantV2() throws WebBaseException_Exception {
        CoordonneesDTO2 coordonnees = etudiantMetier.recupererAdressesEtudiantV2(
                /*_codEtu*/ "12345678",
                /*_annee*/ ANNEE,
                /*_recupAnnuaire*/ null
        );

        assertNotNull(coordonnees);
        assertEquals("estelle@example.org",coordonnees.getEmail());
        assertEquals("estelle.etudiante@etu.univ.fr",coordonnees.getEmailAnnuaire());
        assertEquals("estelle",coordonnees.getLoginAnnuaire());

        AdresseDTO2 adrAnnu = coordonnees.getAdresseAnnuelle();
        assertNotNull(adrAnnu);
        assertEquals("Champ-de-Mars", adrAnnu.getLibAd1());
        assertEquals("Av. Gustave Eiffel", adrAnnu.getLibAd2());
        assertEquals("75007", adrAnnu.getCommune().getCodePostal());
        assertEquals("PARIS", adrAnnu.getCommune().getNomCommune());
        assertEquals("100", adrAnnu.getPays().getCodPay());
        assertEquals("FRANCE", adrAnnu.getPays().getLibPay());

        AdresseDTO2 adrFixe = coordonnees.getAdresseAnnuelle();
        assertNotNull(adrFixe);
        assertEquals("Champ-de-Mars", adrFixe.getLibAd1());
        assertEquals("Av. Gustave Eiffel", adrFixe.getLibAd2());
        assertEquals("75007", adrFixe.getCommune().getCodePostal());
        assertEquals("PARIS", adrFixe.getCommune().getNomCommune());
        assertEquals("100", adrFixe.getPays().getCodPay());
        assertEquals("FRANCE", adrFixe.getPays().getLibPay());
    }

    /** @see EtudiantMetierEndpoint#recupererListeEtudiants(RecupererListeEtudiants) */
    @Test void recupererListeEtudiants() throws WebBaseException_Exception {
        EtudiantCritereDTO critere = new EtudiantCritereDTO();
        critere.setAnnee(ANNEE);
        List<EtudiantDTO2> liste = etudiantMetier.recupererListeEtudiants(critere);

        assertThat(liste).isNotEmpty()
                .filteredOn(etudiant -> "12345678".equals(etudiant.getCodEtu()))
                .singleElement()
                .satisfies(
                        estelle -> assertThat(estelle.getPrenom()).isEqualTo("Estelle"),
                        estelle -> assertThat(estelle.getNom()).isEqualTo("ETUDIANTE"),
                        estelle -> assertThat(estelle.getNumeroIne()).isEqualTo("201234567AB")
                );
    }

}
