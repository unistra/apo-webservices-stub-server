package fr.unistra.dnum.apogee.ws.server.stub.presentation.ws;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.Constantes;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import fr.unistra.dnum.apogee.ws.server.stub.test.TestDataSet;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootPresentationTest
@TestDataSet("""
- infoAdm:
      prenom1: Estelle
      numEtu: 12345678
  ias:
  - statut: &Etudiant { code: '01', libelle: Etudiant, libelleWeb: Etudiant }
    cpam: { libCpam: CPAM }
    regimeIns: &FormationInitiale { codRgi: '1', libRgi: Formation Initiale }
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
""")
class AdministratifMetierEndpointTest {

    public static final String EN_COURS = Constantes.IAA_EN_COURS.getCodeEtatIAA();
    public static final String FORMATION_INITIALE = "1";
    public static final String STATUT_ETUDIANT = "01";

    @DynamicPropertySource
    static void setRandomServerPort(DynamicPropertyRegistry registry) {
        int port = TestSocketUtils.findAvailableTcpPort();
        registry.add("server.port", () -> port);
    }

    @Autowired AdministratifMetierEndpoint endpoint;
    static final String ANNEE = Person.PersonBuilder.anneeU(LocalDateTime.now());

    @Test
    void recupererAnneesIa() {
        RecupererAnneesIa request = new RecupererAnneesIa();
        request.setCodEtu("12345678");
        request.setEtatInscriptionIA(EN_COURS);
        List<String> anneesIa = endpoint.recupererAnneesIa(request).getRecupererAnneesIaReturn();

        assertThat(anneesIa)
                .containsExactly(ANNEE);
    }

    @Test
    void recupererIAAnnuellesResponseV2() {
        RecupererIAAnnuellesV2 request = new RecupererIAAnnuellesV2();
        request.setCodEtu("12345678");
        request.setAnnee(ANNEE);
        request.setEtatIAA(EN_COURS);
        List<InsAdmAnuDTO2> iaAnnuelles = endpoint.recupererIAAnnuellesResponseV2(request).getRecupererIAAnnuellesReturnV2();

        assertThat(iaAnnuelles)
                .singleElement()
                .satisfies(
                        ia -> assertThat(ia.getAnneeIAA()).isEqualTo(ANNEE),
                        ia -> assertThat(ia.getEtatIaa())
                                .extracting(EtatIAADTO::getCodeEtatIAA).isEqualTo(EN_COURS),
                        ia -> assertThat(ia.getRegimeIns())
                                .extracting(RegimeInsDTO::getCodRgi).isEqualTo(FORMATION_INITIALE),
                        ia -> assertThat(ia.getStatut())
                                .extracting(StatutEtuDTO::getCode).isEqualTo(STATUT_ETUDIANT)
                );
    }

    @Test
    void recupererIAEtapesV3() {
        RecupererIAEtapesV3 request = new RecupererIAEtapesV3();
        request.setCodEtu("12345678");
        request.setAnnee(ANNEE);
        request.setEtatIAA(EN_COURS);
        request.setEtatIAE(EN_COURS);
        List<InsAdmEtpDTO3> etapes = endpoint.recupererIAEtapesV3(request).getRecupererIAEtapesV3Return();

        assertThat(etapes)
                .singleElement()
                .satisfies(
                        iae -> assertThat(iae.getAnneeIAE()).isEqualTo(ANNEE),
                        iae -> assertThat(iae.getEtatIaa()).extracting(EtatIAADTO::getCodeEtatIAA).isEqualTo(EN_COURS),
                        iae -> assertThat(iae.getEtatIae()).extracting(EtatIAEDTO::getCodeEtatIAE).isEqualTo(EN_COURS),
                        iae -> assertThat(iae.getRegimeIns()).extracting(RegimeInsDTO::getCodRgi).isEqualTo(FORMATION_INITIALE),
                        iae -> assertThat(iae.getComposante()).satisfies(
                                cge -> assertThat(cge.getCodComposante()).isEqualTo("COMP"),
                                cge -> assertThat(cge.getLibComposante()).isEqualTo("Composante")
                        ),
                        iae -> assertThat(iae.getCge()).satisfies(
                                cge -> assertThat(cge.getCodeCGE()).isEqualTo("CGE"),
                                cge -> assertThat(cge.getLibCGE()).isEqualTo("Centre de Gestion")
                        )
                ).extracting(InsAdmEtpDTO3::getEtape)
                .satisfies(
                        etp -> assertThat(etp.getCodeEtp()).isEqualTo("LT1"),
                        etp -> assertThat(etp.getVersionEtp()).isEqualTo("321"),
                        etp -> assertThat(etp.getLibLongEtp()).isEqualTo("Licence de test première année"),
                        etp -> assertThat(etp.getLibWebVet()).isEqualTo("LICENCE de test première année")
                );
    }
}