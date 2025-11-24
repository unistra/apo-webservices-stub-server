package fr.unistra.dnum.apogee.ws.server.stub.infrastructure;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.EtudiantPredicateBuilder;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import fr.unistra.dnum.apogee.ws.server.stub.test.DataSetTest;
import fr.unistra.dnum.apogee.ws.server.stub.test.TestDataSet;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.IdentifiantsEtudiantDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.InfoAdmEtuDTO4;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(EtudiantMetierYamlRepository.class)
@TestPropertySource(properties = "dataset.files=")
@DataSetTest
@TestDataSet("""
- infoAdm:
    numEtu: 12345678
""")
class EtudiantMetierYamlRepositoryTest {

    @Autowired EtudiantMetierYamlRepository repository;

    @Test
    void recupererIdentifiantsEtudiant() {
        IdentifiantsEtudiantDTO2 etudiant = repository.recuperer(
                EtudiantPredicateBuilder.builder()
                        .matchCodEtu("12345678")
                        .build())
                .map(Person::getIdentifiants)
                .orElseThrow();

        assertEquals(12345678, etudiant.getCodEtu());
    }


    @Test
    void recupererInfosAdmEtuV4() {
        InfoAdmEtuDTO4 etudiant = repository.recuperer("12345678")
                .map(Person::infoAdm)
                .orElseThrow();
        assertEquals(12345678, etudiant.getNumEtu());
    }

}