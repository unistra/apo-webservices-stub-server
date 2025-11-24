package fr.unistra.dnum.apogee.ws.server.stub.domaine;

import gouv.education.apogee.commun.client.ws.ReferentielMetier.RegimeInscDTO;

import java.util.Optional;
import java.util.stream.Stream;

public interface RegimeInscriptionMetierRepository {
    Stream<RegimeInscDTO> liste();
    Optional<RegimeInscDTO> get(String codeRi);
}
