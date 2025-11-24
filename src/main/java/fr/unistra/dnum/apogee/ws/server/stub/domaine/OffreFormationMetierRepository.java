package fr.unistra.dnum.apogee.ws.server.stub.domaine;

import gouv.education.apogee.commun.client.ws.OffreFormationMetier.*;

import java.util.stream.Stream;

public interface OffreFormationMetierRepository {

    Stream<DiplomeDTO4> liste(SEFilter OFFilter);

}
