package fr.unistra.dnum.apogee.ws.server.stub.presentation.ws;

import fr.unistra.dnum.apogee.ws.server.stub.test.TestDataSet;
import gouv.education.apogee.commun.client.ws.OffreFormationMetier.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;

import java.util.List;

import static fr.unistra.dnum.apogee.ws.test.AssertUtil.tableau;
import static org.assertj.core.api.Assertions.assertThat;

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
class OffreFormationMetierEndpointTest {
    @DynamicPropertySource
    static void setRandomServerPort(DynamicPropertyRegistry registry) {
        int port = TestSocketUtils.findAvailableTcpPort();
        registry.add("server.port", () -> port);
    }

    @Autowired OffreFormationMetierServiceInterface ofService;

    @Test
    void recupererSEV4() throws WebBaseException_Exception {
        SECritereDTO2 seCritereDTO2 = new SECritereDTO2();
        seCritereDTO2.setCodDip("LT");
        seCritereDTO2.setCodVrsVdi("tous");
        seCritereDTO2.setCodEtp("tous");
        seCritereDTO2.setCodVrsVet("tous");
        seCritereDTO2.setCodElp("aucun");
        List<DiplomeDTO4> diplomes = ofService.recupererSEV4(seCritereDTO2);

        assertThat(diplomes)
                .singleElement()
                .satisfies(
                        dpl -> assertThat(dpl.getCodDip()).isEqualTo("LT"),
                        dpl -> assertThat(dpl.getLibDip()).isEqualTo("Licence de Test")
                )
                .extracting(DiplomeDTO4::getListVersionDiplome, tableau(TableauVersionDiplomeDTO4::getItem))
                .singleElement()
                .satisfies(
                        vdi -> assertThat(vdi.getCodVrsVdi()).isEqualTo(321),
                        vdi -> assertThat(vdi.getLibCourtVdi()).isEqualTo("Licence TEST"),
                        vdi -> assertThat(vdi.getLibWebVdi()).isEqualTo("LICENCE de test")
                )
                .extracting(VersionDiplomeDTO4::getOffreFormation)
                .extracting(OffreFormationDTO4::getListEtape,tableau(TableauEtapeDTO4::getItem))
                .singleElement()
                .satisfies(
                        etp -> assertThat(etp.getCodEtp()).isEqualTo("LT1"),
                        etp -> assertThat(etp.getLibCourtEtp()).isEqualTo("Licence TEST"),
                        etp -> assertThat(etp.getLibEtp()).isEqualTo("Licence de test première année")
                )
                .satisfies(etp -> assertThat(etp)
                        .extracting(EtapeDTO4::getListVersionEtape,tableau(TableauVersionEtapeDTO4::getItem))
                        .singleElement()
                        .satisfies(
                                vet -> assertThat(vet.getCodVrsVet()).isEqualTo(321),
                                vet -> assertThat(vet.getLibWebVet()).isEqualTo("LICENCE de test première année"),
                                vet -> assertThat(vet.getComposante()).satisfies(
                                                comp -> assertThat(comp.getCodComposante()).isEqualTo("COMP"),
                                                comp -> assertThat(comp.getLibComposante()).isEqualTo("Composante")
                                )
                        )
                )
                .extracting(EtapeDTO4::getListComposanteCentreGestion,tableau(TableauComposanteCentreGestionDTO4::getItem))
                .singleElement()
                .satisfies(
                        cge -> assertThat(cge.getCodCentreGestion()).isEqualTo("CGE"),
                        cge -> assertThat(cge.getLibCentreGestion()).isEqualTo("Centre de Gestion"),
                        cge -> assertThat(cge.getCodComposante()).isEqualTo("COMP"),
                        cge -> assertThat(cge.getLibComposante()).isEqualTo("Composante")
                );
    }

}
