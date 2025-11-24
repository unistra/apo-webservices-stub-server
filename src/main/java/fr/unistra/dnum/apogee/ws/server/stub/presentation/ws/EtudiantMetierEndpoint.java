package fr.unistra.dnum.apogee.ws.server.stub.presentation.ws;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.EtudiantMetierRepository;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.EtudiantPredicateBuilder;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static fr.unistra.dnum.apogee.ws.server.stub.presentation.PresentationUtils.toPrettyString;

@Endpoint
public class EtudiantMetierEndpoint {
    private static final Log log = LogFactory.getLog(EtudiantMetierEndpoint.class);
    private static final String NAMESPACE_URI = "gouv.education.apogee.commun.servicesmetiers.EtudiantMetier";

    private final EtudiantMetierRepository repository;
    private final EtudiantMapper mapper;

    public EtudiantMetierEndpoint(EtudiantMetierRepository repository, EtudiantMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** @see EtudiantMetierServiceInterface#recupererIdentifiantsEtudiantV2(String, String, String, String, String, String, String, String, String) */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "recupererIdentifiantsEtudiant_v2")
    @ResponsePayload
    public RecupererIdentifiantsEtudiantV2Response recupererIdentifiantsEtudiantV2(
            @RequestPayload RecupererIdentifiantsEtudiantV2 request) {
        log.info("Request for EtudiantMetier.recupererIdentifiantsEtudiantV2("+toPrettyString(request)+")");
        IdentifiantsEtudiantDTO2 identifiantsEtudiant =
                repository.recuperer(matcher(request))
                        .map(Person::getIdentifiants)
                        .orElseThrow(() -> new NoSuchElementException(
                                "Étudiant correspondant à "+ toPrettyString(request)+" non trouvé"));
        RecupererIdentifiantsEtudiantV2Response response = new RecupererIdentifiantsEtudiantV2Response();
        response.setRecupererIdentifiantsEtudiantV2Return(identifiantsEtudiant);
        return response;
    }

    static Predicate<Person> matcher(RecupererIdentifiantsEtudiantV2 filter) {
        return EtudiantPredicateBuilder.builder()
                .matchCodEtu(filter.getCodEtu())
                .matchCodInd(filter.getCodInd())
                .matchNumINE(filter.getNumINE())
                .matchNumBoursier(filter.getNumBoursier())
                .matchNom(filter.getNom())
                .matchPrenom(filter.getPrenom())
                .matchDateNaiss(filter.getDateNaiss())
                .matchTemoinRecupAnnu(filter.getTemoinRecupAnnu())
                .matchCodOPI(filter.getCodOPI())
                .build();
    }

    /** @see EtudiantMetierServiceInterface#recupererInfosAdmEtuV4(String)  */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "recupererInfosAdmEtu_v4")
    @ResponsePayload
    public RecupererInfosAdmEtuV4Response recupererInfosAdmEtuV4(
            @RequestPayload RecupererInfosAdmEtuV4 request) {
        log.info("Request for EtudiantMetier.recupererInfosAdmEtuV4("+request.getCodEtu()+")");
        InfoAdmEtuDTO4 infoAdm = repository.recuperer(request.getCodEtu())
                .map(Person::infoAdm)
                .orElseThrow(() -> new NoSuchElementException(
                        "Étudiant correspondant à codEtu="+request.getCodEtu()+" non trouvé"));

        RecupererInfosAdmEtuV4Response response = new RecupererInfosAdmEtuV4Response();
        response.setRecupererInfosAdmEtuV4Return(infoAdm);
        return response;
    }

    /** @see EtudiantMetierServiceInterface#recupererInfosAdmEtuV3(String) */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "recupererInfosAdmEtu_v3")
    @ResponsePayload
    public RecupererInfosAdmEtuV3Response recupererInfosAdmEtuV3(
            @RequestPayload RecupererInfosAdmEtuV3 request) {
        log.info("Request for EtudiantMetier.recupererInfosAdmEtuV3("+request.getCodEtu()+")");
        InfoAdmEtuDTO3 infoAdm =  repository.recuperer(request.getCodEtu())
                .map(Person::infoAdm)
                .map(mapper::infoAdmEtuDTO3)
                .orElseThrow(() -> new NoSuchElementException(
                        "Étudiant correspondant à codEtu="+request.getCodEtu()+" non trouvé"));

        RecupererInfosAdmEtuV3Response response = new RecupererInfosAdmEtuV3Response();
        response.setRecupererInfosAdmEtuV3Return(infoAdm);
        return response;
    }

    /** @see EtudiantMetierServiceInterface#recupererAdressesEtudiantV2(String, String, String)  */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "recupererAdressesEtudiant_v2")
    @ResponsePayload
    public RecupererAdressesEtudiantV2Response recupererAdressesEtudiantV2(
            @RequestPayload RecupererAdressesEtudiantV2 request) {
        log.info("Request for EtudiantMetier.recupererAdressesEtudiantV2("+toPrettyString(request)+")");
        String codEtu = request.getCodEtu();
        String annee = request.getAnnee();

        CoordonneesDTO2 adresses = repository.recuperer(codEtu).stream()
                .map(Person::coordonnees).flatMap(coordonnees(annee))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(
                        "Étudiant correspondant à codEtu="+codEtu+" non trouvé"));

        RecupererAdressesEtudiantV2Response response = new RecupererAdressesEtudiantV2Response();
        response.setRecupererAdressesEtudiantReturn(adresses);
        return response;
    }
    private static Function<CoordonneesDTO2[],Stream<CoordonneesDTO2>> coordonnees(String annee) {
        return annee != null
                ? coordonnees -> Arrays.stream(coordonnees)
                    .filter(coordonnee -> annee.equals(coordonnee.getAnnee()))
                : coordonnees -> Arrays.stream(coordonnees)
                    .max(Comparator.comparing(CoordonneesDTO2::getAnnee))
                    .stream();
    }

    /** @see EtudiantMetierServiceInterface#recupererListeEtudiants(EtudiantCritereDTO) */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "recupererListeEtudiants")
    @ResponsePayload
    public RecupererListeEtudiantsResponse recupererListeEtudiants(
            @RequestPayload RecupererListeEtudiants request) {
        log.info("Request for EtudiantMetier.recupererListeEtudiants("+toPrettyString(request)+")");
        RecupererListeEtudiantsResponse response = new RecupererListeEtudiantsResponse();
        repository.liste(matcher(request.getParametres()))
                .map(Person::getEtudiant)
                .forEach(response.getEtudiant()::add);
        return response;
    }

    static Predicate<Person> matcher(EtudiantCritereDTO filter) {
        return EtudiantPredicateBuilder.builder()
            .matchAnnee(filter.getAnnee())
            .matchDiplome(get(filter.getListDiplomes(), TableauDiplomes::getItem))
            .matchEtape(get(filter.getListEtapes(), TableauEtapes::getItem))
            .matchComposante(filter.getListComposante())
            .build();
    }

    private static <T,U> List<U> get(T t, Function<T,List<U>> getter) {
        List<U> list = t != null ? getter.apply(t) : List.of();
        return list != null ? list : List.of();
    }

}
