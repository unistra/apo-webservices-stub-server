package fr.unistra.dnum.apogee.ws.server.stub.domaine;

import gouv.education.apogee.commun.client.ws.ReferentielMetier.ComposanteDTO3;

import java.util.stream.Stream;

public interface ComposanteMetierRepository {
    Stream<ComposanteDTO3> liste();
}
