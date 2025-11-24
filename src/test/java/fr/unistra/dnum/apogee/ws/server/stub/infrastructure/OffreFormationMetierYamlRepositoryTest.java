package fr.unistra.dnum.apogee.ws.server.stub.infrastructure;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.SEFilter;
import fr.unistra.dnum.apogee.ws.server.stub.test.DataSetTest;
import fr.unistra.dnum.apogee.ws.server.stub.test.TestDataSet;
import gouv.education.apogee.commun.client.ws.OffreFormationMetier.*;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.yaml.snakeyaml.Yaml;

import java.util.List;

import static fr.unistra.dnum.apogee.ws.test.AssertUtil.tableau;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OffreFormationMetierYamlRepository.class)
@TestPropertySource(properties = "dataset.files=")
@DataSetTest
@TestDataSet("""
- infoAdm:
    numEtu: 12345678
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
    regimeIns: &FormationInitiale { codRgi: '1', libRgi: Formation Initiale }
- infoAdm:
    numEtu: 12345679
  etapes:
  - diplome: *LT
    etape: *LT1
    regimeIns: &FormationContinue { codRgi: '2', libRgi: Formation Continue }
""")
class OffreFormationMetierYamlRepositoryTest {

    @Autowired Yaml yaml;
    @Autowired OffreFormationMetierYamlRepository repository;

    @Test
    void testInit() {
        List<DiplomeDTO4> diplomes = repository.liste(SEFilter.builder().codDip("LT").build())
                .toList();
        assertThat(diplomes)
                .isNotEmpty()
                .filteredOn(d -> "LT".equals(d.getCodDip()))
                .singleElement()
                .satisfies(
                        dpl -> assertThat(dpl.getLibDip()).isEqualTo("Licence de Test")
                )
                .satisfies(dpl -> assertThat(dpl.getEtbHab()).satisfies(
                        etab -> assertThat(etab)
                                .extracting(EtablissementHabiliteDTO::getCodEtb, EtablissementHabiliteDTO::getLibEtbHab)
                                .containsExactly("UNIV","UNIVERSITÉ")
                ))
                .extracting(DiplomeDTO4::getListVersionDiplome,tableau(TableauVersionDiplomeDTO4::getItem))
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
                        etp -> assertThat(etp.getLibEtp()).isEqualTo("Licence de test première année"),
                        etp -> assertThat(etp)
                                .extracting(EtapeDTO4::getListComposanteCentreGestion,tableau(TableauComposanteCentreGestionDTO4::getItem))
                                .singleElement()
                                .extracting(
                                        ComposanteCentreGestionDTO::getCodCentreGestion,ComposanteCentreGestionDTO::getLibCentreGestion,
                                        ComposanteCentreGestionDTO::getCodComposante,ComposanteCentreGestionDTO::getLibComposante)
                                .containsExactly(
                                        "CGE","Centre de Gestion",
                                        "COMP","Composante")
                )
                .extracting(EtapeDTO4::getListVersionEtape,tableau(TableauVersionEtapeDTO4::getItem))
                .singleElement()
                .satisfies(
                        vet -> assertThat(vet.getCodVrsVet()).isEqualTo(321),
                        vet -> assertThat(vet.getLibWebVet()).isEqualTo("LICENCE de test première année"),
                        vet -> assertThat(vet.getComposante())
                                .extracting(ComposanteOrganisatriceDTO::getCodComposante,ComposanteOrganisatriceDTO::getLibComposante)
                                .containsExactly("COMP","Composante"),
                        vet -> assertThat(vet)
                                .extracting(VersionEtapeDTO42::getListRegime, tableau(TableauListeRegimeDTO::getItem))
                                .extracting(RegimeDTO::getCodRgi,RegimeDTO::getLibRgi)
                                .containsExactlyInAnyOrder(
                                        Tuple.tuple("1","Formation Initiale"),
                                        Tuple.tuple("2", "Formation Continue")
                                )
                );
    }

}