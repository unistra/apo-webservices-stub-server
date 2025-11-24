package fr.unistra.dnum.apogee.ws.server.stub.presentation.ws;

import fr.unistra.dnum.apogee.ws.export.cli.EtudiantMetierClientConfig;
import fr.unistra.dnum.apogee.ws.server.stub.test.TestDataSet;
import fr.unistra.dnum.apogee.ws.server.stub.test.TestDataSetTestExecutionListener;
import gouv.education.apogee.commun.client.ws.ReferentielMetier.ComposanteDTO3;
import gouv.education.apogee.commun.client.ws.ReferentielMetier.ReferentielMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.ReferentielMetier.WebBaseException_Exception;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.util.TestSocketUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;


@SpringBootPresentationTest
@TestDataSet("""
- infoAdm:
      numEtu: 12345678
  etapes:
  - composante: &COMP1 { codComposante: COMP1, libComposante: Composante1 }
    etape: &LT1
      codeEtp: LT1
      versionEtp: '321'
- infoAdm:
      numEtu: 12345679
  etapes:
  - composante: &COMP2 { codComposante: COMP2, libComposante: Composante2 }
    etape: *LT1
""")
class ReferentielMetierEndpointTest {
    @DynamicPropertySource
    static void setRandomServerPort(DynamicPropertyRegistry registry) {
        int port = TestSocketUtils.findAvailableTcpPort();
        registry.add("server.port", () -> port);
    }

    @Autowired
    ReferentielMetierServiceInterface referentielMetier;

    @Test
    void recupererComposanteV2() throws WebBaseException_Exception {
        List<ComposanteDTO3> composanteDTO3s = referentielMetier.recupererComposanteV2(null, null);
        assertThat(composanteDTO3s)
                .extracting(ComposanteDTO3::getCodCmp, ComposanteDTO3::getLibCmp)
                .containsExactlyInAnyOrder(
                        tuple("COMP1", "Composante1"),
                        tuple("COMP2", "Composante2")
                );
    }

    @ParameterizedTest(name = "recupererRegimeInscription {0} => {2}")
    @CsvSource({
            "1, O, Formation initiale hors apprentissage, Initiale",
            "2, N, Formation continue diplômante financée, Continue F",
            "3, O, Reprise études non financée sans conv, Rep.non Fi",
            "4, O, Contrat apprentissage, Apprentiss",
            "5, O, Formation continue hors contrat prof, FC hors CP",
            "6, O, Contrat de professionnalisation, FC ContPro",
    })
    void recupererRegimeInscription(String codRegIns, String temoinEnService, String libRegIns, String LicRegIns)
            throws gouv.education.apogee.commun.client.ws.ReferentielMetier.WebBaseException_Exception {
        assertThat(referentielMetier.recupererRegimeInscription(codRegIns, temoinEnService))
                .isNotNull()
                .singleElement()
                .satisfies(
                        ri -> assertThat(ri.getCodRegIns()).isEqualTo(codRegIns),
                        ri -> assertThat(ri.getLibRegIns()).isEqualTo(libRegIns),
                        ri -> assertThat(ri.getLicRegIns()).isEqualTo(LicRegIns),
                        ri -> assertThat(ri.getTemEnSve()).isEqualTo(temoinEnService)
                );
    }

}