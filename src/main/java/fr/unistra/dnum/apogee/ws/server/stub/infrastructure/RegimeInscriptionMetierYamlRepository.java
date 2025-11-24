package fr.unistra.dnum.apogee.ws.server.stub.infrastructure;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.RegimeInscriptionMetierRepository;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmAnuDTO2;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmEtpDTO3;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.RegimeInsDTO;
import gouv.education.apogee.commun.client.ws.ReferentielMetier.RegimeInscDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Repository;
import org.springframework.util.function.SingletonSupplier;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.stream.Stream;

/** Repository for {@link RegimeInscDTO} pre-filled with fixed entries */
@Repository
public class RegimeInscriptionMetierYamlRepository implements RegimeInscriptionMetierRepository {
    private static final List<RegimeInscDTO> RIS = List.of(new Yaml().loadAs("""
        - codRegIns: '1'
          libRegIns: Formation initiale hors apprentissage
          licRegIns: Initiale
          temEnSve: O
        - codRegIns: '2'
          libRegIns: "Formation continue diplômante financée"
          licRegIns: "Continue F"
          temEnSve: N
        - codRegIns: '3'
          libRegIns: Reprise études non financée sans conv
          licRegIns: Rep.non Fi
          temEnSve: O
        - codRegIns: '4'
          libRegIns: Contrat apprentissage
          licRegIns: Apprentiss
          temEnSve: O
        - codRegIns: '5'
          libRegIns: Formation continue hors contrat prof
          licRegIns: FC hors CP
          temEnSve: O
        - codRegIns: '6'
          libRegIns: Contrat de professionnalisation
          licRegIns: FC ContPro
          temEnSve: O
        """, RegimeInscDTO[].class));

    @Override
    public Stream<RegimeInscDTO> liste() {
        return RIS.stream();
    }

    @Override
    public Optional<RegimeInscDTO> get(String codeRi) {
        return liste()
                .filter(ri -> codeRi.equals(ri.getCodRegIns()))
                .findAny();
    }

}
