package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.ldap;

import fr.unistra.dnum.apogee.ws.server.stub.config.DatasetConfigurationProperties;
import fr.unistra.dnum.apogee.ws.server.stub.config.LdapServerConfig;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.SupannPerson;
import fr.unistra.dnum.apogee.ws.server.stub.infrastructure.DataSet;
import fr.unistra.dnum.apogee.ws.server.stub.infrastructure.EtudiantMetierYamlRepository;
import fr.unistra.dnum.apogee.ws.server.stub.test.DataSetTest;
import fr.unistra.dnum.apogee.ws.server.stub.test.TestDataSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.ldap.support.LdapUtils.newLdapName;

@SpringJUnitConfig({
        EtudiantMetierYamlRepository.class,
        DataSet.class,
        LdapServerConfig.class,
})
@EnableAutoConfiguration
@EnableConfigurationProperties(DatasetConfigurationProperties.class)
@TestPropertySource(properties = {
        "dataset.files=",
        "spring.ldap.base=ou=people,dc=univ,dc=fr",
        "spring.ldap.embedded.port=0",
        "spring.ldap.embedded.base-dn=dc=univ,dc=fr",
        "spring.ldap.embedded.validation.enabled=false"
})
@DataSetTest
@TestDataSet("""
- ldap:
    uid: olivier
    sn: [ ENSEIGNANT ]
    givenName: [ Olivier ]
    supannAliasLogin: enseignant
    mail: olivier.enseignant@univ.fr
    eduPersonPrimaryAffiliation: teacher
    eduPersonAffiliation: [teacher, member]

- ldap:
    uid: nathalie
    sn: [ GESTIONNAIRE ]
    givenName: [ Nathalie ]
    supannAliasLogin: gestionnaire
    mail: nathalie.gestionnaire@univ.fr
    eduPersonPrimaryAffiliation: employee
    eduPersonAffiliation: [employee, member]

- codInd: 12345678
  infoAdm:
      prenom1: Estelle
      nomPatronymique: ETUDIANTE
      numEtu: 12345678
      numeroINE: 201234567AB
  coordonnees:
  - email: estelle@example.org
    emailAnnuaire: estelle.etudiante@etu.univ.fr
    loginAnnuaire: estelle
""")
class LdapPopulatorTest {

    @Autowired LdapTemplate ldapTemplate;

    @Test void testOlivierExistsInLdap() {
        SupannPerson estelle = ldapTemplate.findByDn(
                newLdapName("uid=olivier"),
                SupannPerson.class);
        assertThat(estelle).isNotNull();
        assertThat(estelle.getGivenName()).containsExactly("Olivier");
        assertThat(estelle.getSn()).containsExactly("ENSEIGNANT");
        assertThat(estelle.getSupannEtuId()).isNull();
        assertThat(estelle.getEduPersonPrimaryAffiliation()).isEqualTo("teacher");
        assertThat(estelle.getEduPersonAffiliation()).contains("teacher");
        assertThat(estelle.getMail()).isEqualTo("olivier.enseignant@univ.fr");
    }

    @Test void testNathalieExistsInLdap() {
        SupannPerson estelle = ldapTemplate.findByDn(
                newLdapName("uid=nathalie"),
                SupannPerson.class);
        assertThat(estelle).isNotNull();
        assertThat(estelle.getGivenName()).containsExactly("Nathalie");
        assertThat(estelle.getSn()).containsExactly("GESTIONNAIRE");
        assertThat(estelle.getSupannEtuId()).isNull();
        assertThat(estelle.getEduPersonPrimaryAffiliation()).isEqualTo("employee");
        assertThat(estelle.getEduPersonAffiliation()).contains("employee");
        assertThat(estelle.getMail()).isEqualTo("nathalie.gestionnaire@univ.fr");
    }

    @Test void testEstelleExistsInLdap() {
        SupannPerson estelle = ldapTemplate.findByDn(
                newLdapName("uid=estelle"),
                SupannPerson.class);
        assertThat(estelle).isNotNull();
        assertThat(estelle.getGivenName()).containsExactly("Estelle");
        assertThat(estelle.getSn()).containsExactly("ETUDIANTE");
        assertThat(estelle.getSupannEtuId()).isEqualTo("12345678");
        assertThat(estelle.getEduPersonPrimaryAffiliation()).isEqualTo("student");
        assertThat(estelle.getEduPersonAffiliation()).contains("student");
        assertThat(estelle.getMail()).isEqualTo("estelle.etudiante@etu.univ.fr");
        assertThat(estelle.getSupannAutreMail()).isEqualTo("estelle@example.org");
    }

}