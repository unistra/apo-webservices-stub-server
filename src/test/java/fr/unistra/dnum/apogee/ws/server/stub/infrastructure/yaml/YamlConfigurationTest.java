package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.yaml.snakeyaml.Yaml;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.TimeZone;

import static fr.unistra.dnum.apogee.ws.test.AssertUtil.tableau;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = YamlConfiguration.class)
class YamlConfigurationTest {
    public static final TimeZone restoreTimeZone = TimeZone.getDefault();
    @BeforeAll
    static void setTimeZoneToParis() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
    }
    @AfterAll
    static void restoreTimeZone() {
        TimeZone.setDefault(restoreTimeZone);
    }

    @Autowired Yaml yaml;

    @Test
    void testDump() {
        InfoAdmEtuDTO4 infoAdm = new InfoAdmEtuDTO4();
        infoAdm.setNumEtu(12345678);
        infoAdm.setNomPatronymique("ETUDIANTE");
        infoAdm.setPrenom1("Estelle");
        infoAdm.setDateNaissance(LocalDateTime.of(1991,10,9,8,7,6));
        infoAdm.setListeBacs(new TableauIndBacDTO2());
        IndBacDTO2 indBac = new IndBacDTO2();
        indBac.setCodBac("S");
        indBac.setLibelleBac("S-Scientifique");
        infoAdm.getListeBacs().getItem().add(indBac);
        Person person = new Person.PersonBuilder()
                .infoAdm(infoAdm)
                .build();
        String serialized = yaml.dump(person);
        assertThat(serialized)
                .contains("numEtu: 12345678")
                .contains("nomPatronymique: ETUDIANTE")
                .contains("prenom1: Estelle")
                .contains("dateNaissance: '1991-10-09T08:07:06")
                .contains("codBac: S")
                .contains("libelleBac: S-Scientifique")
        ;
    }

    @Test
    void testLoad() {
        Person estelle = yaml.loadAs("""
                infoAdm:
                  numEtu: 12345678
                  nomPatronymique: ETUDIANTE
                  prenom1: Estelle
                  dateNaissance: 1991-10-09T08:07:06
                  listeBacs:
                    - codBac: S
                      libelleBac: S-Scientifique
                """, Person.class);
        assertThat(estelle)
                .isNotNull()
                .extracting(Person::infoAdm)
                .satisfies(
                    infoAdm -> assertThat(infoAdm.getNumEtu()).isEqualTo(12345678),
                    infoAdm -> assertThat(infoAdm.getNomPatronymique()).isEqualTo("ETUDIANTE"),
                    infoAdm -> assertThat(infoAdm.getPrenom1()).isEqualTo("Estelle"),
                    infoAdm -> assertThat(infoAdm.getDateNaissance())
                            .hasYear(1991).hasMonth(Month.OCTOBER).hasDayOfMonth(9)
                            .hasHour(8).hasMinute(7).hasSecond(6),
                    infoAdm -> assertThat(infoAdm).extracting(InfoAdmEtuDTO4::getListeBacs,tableau(TableauIndBacDTO2::getItem))
                            .singleElement()
                            .satisfies(
                                bac -> assertThat(bac.getCodBac()).isEqualTo("S"),
                                bac -> assertThat(bac.getLibelleBac()).isEqualTo("S-Scientifique")
                            )
                );
    }

    @Test
    void testSerializeDedupAnchor() {
        InfoAdmEtuDTO4 infoAdm = new InfoAdmEtuDTO4();
        DepartementDTO hautRhin = new DepartementDTO();
        hautRhin.setCodeDept("068");
        hautRhin.setLibDept("HAUT RHIN");
        infoAdm.setDepartementNaissance(hautRhin);
        IndBacDTO2 bac = new IndBacDTO2();
        DepartementCourtDTO hautRhinCourt = new DepartementCourtDTO();
        bac.setDepartementBac(hautRhinCourt);
        hautRhinCourt.setCodeDept("068");
        hautRhinCourt.setLibDept("HAUT RHIN");
        infoAdm.setListeBacs(new TableauIndBacDTO2());
        infoAdm.getListeBacs().getItem().add(bac);

        String dump = yaml.dumpAsMap(infoAdm);
        assertThat(dump)
                .contains(
                        ": &HAUT_RHIN",
                        ": *HAUT_RHIN"
                );
    }

    @Test
    void testDeserializeDedupAnchor() {
        InfoAdmEtuDTO4 infoAdm = yaml.loadAs("""
                listeBacs:
                - departementBac: &HAUT_RHIN
                    codeDept: '068'
                    libDept: HAUT RHIN
                departementNaissance: *HAUT_RHIN
                """, InfoAdmEtuDTO4.class);

        assertThat(infoAdm.getDepartementNaissance())
                .extracting(DepartementDTO::getCodeDept,DepartementDTO::getLibDept)
                .containsExactly("068","HAUT RHIN");
        assertThat(infoAdm.getListeBacs().getItem().getFirst().getDepartementBac())
                .extracting(DepartementCourtDTO::getCodeDept,DepartementCourtDTO::getLibDept)
                .containsExactly("068","HAUT RHIN");
    }

    @Test
    void testSerializeDedupMerge() {
        CoordonneesDTO2 coord = new CoordonneesDTO2();
        {
            AdresseDTO2 annu = new AdresseDTO2();
            CommuneDTO2 laSaline = new CommuneDTO2();
            laSaline.setCodePostal("97422");
            laSaline.setCodeInsee("97415");
            laSaline.setNomCommune("Saint-Paul");
            laSaline.setLibAch("LA SALINE");
            annu.setCommune(laSaline);
            coord.setAdresseAnnuelle(annu);
            AdresseDTO2 fixe = new AdresseDTO2();
            CommuneDTO2 laPossession = new CommuneDTO2();
            laPossession.setCodePostal("97419");
            laPossession.setCodeInsee("97415");
            laPossession.setNomCommune("Saint-Paul");
            laPossession.setLibAch("LA POSSESSION");
            fixe.setCommune(laPossession);
            coord.setAdresseFixe(fixe);
        }

        String dump = yaml.dumpAsMap(coord);
        assertThat(dump)
                .contains(
                        ": &SaintPaul",
                        "<<: *SaintPaul",
                        "codePostal: '97422'", "libAch: LA SALINE",
                        "codePostal: '97419'", "libAch: LA POSSESSION"
                )
                .containsOnlyOnce("codeInsee: '97415'")
                .containsOnlyOnce("nomCommune: Saint-Paul");
    }

    @Test
    void testDeserializeDedupMerge() {
        CoordonneesDTO2 coord = yaml.loadAs("""
                adresseAnnuelle:
                  commune: &SaintPaul
                    codeInsee: '97415'
                    codePostal: '97422'
                    nomCommune: Saint-Paul
                    libAch: LA SALINE
                adresseFixe:
                  commune:
                    <<: *SaintPaul
                    codePostal: '97419'
                    libAch: LA POSSESSION
                """, CoordonneesDTO2.class);

        assertThat(coord.getAdresseAnnuelle().getCommune()).extracting(
                        CommuneDTO2::getCodeInsee,
                        CommuneDTO2::getCodePostal,
                        CommuneDTO2::getNomCommune,
                        CommuneDTO2::getLibAch
                )
                .containsExactly("97415","97422","Saint-Paul","LA SALINE");
        assertThat(coord.getAdresseFixe().getCommune()).extracting(
                        CommuneDTO2::getCodeInsee,
                        CommuneDTO2::getCodePostal,
                        CommuneDTO2::getNomCommune,
                        CommuneDTO2::getLibAch
                )
                .containsExactly("97415","97419","Saint-Paul","LA POSSESSION");
    }

}