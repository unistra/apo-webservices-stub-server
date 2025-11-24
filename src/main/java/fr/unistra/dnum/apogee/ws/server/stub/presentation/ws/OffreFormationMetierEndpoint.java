package fr.unistra.dnum.apogee.ws.server.stub.presentation.ws;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.OffreFormationMetierRepository;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.SEFilter;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.SEFilterBuilder;
import gouv.education.apogee.commun.client.ws.OffreFormationMetier.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;

import static fr.unistra.dnum.apogee.ws.server.stub.presentation.PresentationUtils.toPrettyString;

@Endpoint
public class OffreFormationMetierEndpoint {
    private static final Log log = LogFactory.getLog(OffreFormationMetierEndpoint.class);
    private static final String NAMESPACE_URI = "gouv.education.apogee.commun.servicesmetiers.OffreFormationMetier";

    private final OffreFormationMetierRepository diplomeRepository;

    public OffreFormationMetierEndpoint(OffreFormationMetierRepository diplomeRepository) {
        this.diplomeRepository = diplomeRepository;
    }

    public static SEFilter matcher(SECritereDTO2 seCritere) throws WebBaseException_Exception {
        return new SEFilterBuilder()
                .codAnu(seCritere.getCodAnu())
                .codComposanteVdi(seCritere.getCodComposanteVdi())
                .codDip(obligatoire(seCritere.getCodDip(),"coddiplome"))
                .codElp(obligatoire(seCritere.getCodElp(),"codelp"))
                .codEtp(obligatoire(seCritere.getCodEtp(),"codetape"))
                .codNatureDip(seCritere.getCodNatureDip())
                .codNatureElp(seCritere.getCodNatureElp())
                .codTypDip(seCritere.getCodTypDip())
                .codVrsVdi(obligatoire(seCritere.getCodVrsVdi(),"codversiondiplome"))
                .codVrsVet(obligatoire(seCritere.getCodVrsVet(),"codetape"))
                .temOuvertRecrutement(seCritere.getTemOuvertRecrutement())
                .build();
    }

    public static <T> T obligatoire(T obj, String param) throws WebBaseException_Exception {
        if (obj == null || obj instanceof String str && str.isBlank())
            throw new WebBaseException_Exception("technical.parameter.noncoherentinput."+param+"obligatoire",null);
        else
            return obj;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "recupererSE_v4")
    @ResponsePayload
    public RecupererSEV4Response recupererSEV4(@RequestPayload RecupererSEV4 request) throws WebBaseException_Exception {
        SECritereDTO2 seCritere = request.getSeCritere();
        log.info("Request for OffreFormationMetierEndpoint.recupererSEV4("+toPrettyString(seCritere)+")");
        RecupererSEV4Response response = new RecupererSEV4Response();

        List<DiplomeDTO4> diplomes = response.getRecupererSEV4Return();
        diplomeRepository.liste(matcher(seCritere))
                .forEach(diplomes::add);

        return response;
    }

}
