package fr.unistra.dnum.apogee.ws.server.stub.presentation.ws;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.EtudiantMetierRepository;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Endpoint
public class AdministratifMetierEndpoint {
    private static final Log log = LogFactory.getLog(AdministratifMetierEndpoint.class);
    private static final String NAMESPACE_URI = "gouv.education.apogee.commun.servicesmetiers.AdministratifMetier";
    private final EtudiantMetierRepository repository;

    public AdministratifMetierEndpoint(EtudiantMetierRepository repository) {
        this.repository = repository;
    }

    /** @see AdministratifMetierServiceInterface#recupererAnneesIa(String, String) */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "recupererAnneesIa")
    @ResponsePayload
    public RecupererAnneesIaResponse recupererAnneesIa(@RequestPayload RecupererAnneesIa request) {
        String codEtu = request.getCodEtu();
        String etatInscriptionIA = request.getEtatInscriptionIA();
        log.info("Request for AdministratifMetier.recupererAnneesIa("+codEtu+","+etatInscriptionIA+")");
        RecupererAnneesIaResponse response = new RecupererAnneesIaResponse();
        List<String> anneesIa = response.getRecupererAnneesIaReturn();

        repository.recuperer(codEtu).stream()
                .map(Person::ias).flatMap(Arrays::stream)
                .filter(IAA.etatIAA(etatInscriptionIA))
                .map(InsAdmAnuDTO2::getAnneeIAA)
                .distinct().sorted()
                .forEach(anneesIa::add);

        return response;
    }

    /** @see AdministratifMetierServiceInterface#recupererIAAnnuellesV2(String, String, String)  */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "recupererIAAnnuelles_v2")
    @ResponsePayload
    public RecupererIAAnnuellesResponseV2 recupererIAAnnuellesResponseV2(@RequestPayload RecupererIAAnnuellesV2 request) {
        String codEtu = request.getCodEtu();
        String annee = request.getAnnee();
        String etatIAA = request.getEtatIAA();
        log.info("Request for AdministratifMetier.recupererIAAnnuelles("+ String.join(",", codEtu, annee, etatIAA) +")");
        RecupererIAAnnuellesResponseV2 response = new RecupererIAAnnuellesResponseV2();
        List<InsAdmAnuDTO2> iaAnnuelles = response.getRecupererIAAnnuellesReturnV2();

        repository.recuperer(codEtu).stream()
                .map(Person::ias).flatMap(Arrays::stream)
                .filter(IAA.anneeIAA(annee).and(IAA.etatIAA(etatIAA)))
                .forEach(iaAnnuelles::add);

        return response;
    }

    private static class IAA {
        private static Predicate<InsAdmAnuDTO2> anneeIAA(String annee) {
            return ia -> annee != null && annee.equals(ia.getAnneeIAA());
        }
        private static Predicate<InsAdmAnuDTO2> etatIAA(String etatIAA) {
            return ia -> etatIAA.equals(ia.getEtatIaa() != null ? ia.getEtatIaa().getCodeEtatIAA() : null);
        }
    }

    /** @see AdministratifMetierServiceInterface#recupererIAEtapesV3(String, String, String, String)  */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "recupererIAEtapes_v3")
    @ResponsePayload
    public RecupererIAEtapesV3Response recupererIAEtapesV3(@RequestPayload RecupererIAEtapesV3 request) {
        String codEtu = request.getCodEtu();
        String annee = request.getAnnee();
        String etatIAA = request.getEtatIAA();
        String etatIAE = request.getEtatIAE();
        log.info("Request for AdministratifMetier.recupererIAEtapes("+ String.join(",", codEtu, annee, etatIAA, etatIAE) +")");
        RecupererIAEtapesV3Response response = new RecupererIAEtapesV3Response();
        List<InsAdmEtpDTO3> iaes = response.getRecupererIAEtapesV3Return();

        repository.recuperer(codEtu).stream()
                .map(Person::etapes).flatMap(Arrays::stream)
                .filter(IAE.anneeIAE(annee)
                        .and(IAE.etatIAA(etatIAA))
                        .and(IAE.etatIAE(etatIAE)))
                .forEach(iaes::add);

        return response;
    }

    private static class IAE {
        private static Predicate<InsAdmEtpDTO3> anneeIAE(String annee) {
            return iae -> annee != null && annee.equals(iae.getAnneeIAE());
        }
        private static Predicate<InsAdmEtpDTO3> etatIAA(String etatIAA) {
            return iae -> etatIAA.equals(iae.getEtatIaa() != null ? iae.getEtatIaa().getCodeEtatIAA() : null);
        }
        private static Predicate<InsAdmEtpDTO3> etatIAE(String etatIAE) {
            return iae -> etatIAE.equals(iae.getEtatIae() != null ? iae.getEtatIae().getCodeEtatIAE() : null);
        }
    }

}
