package fr.unistra.dnum.apogee.ws.server.stub.presentation.ws;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.ComposanteMetierRepository;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.RegimeInscriptionMetierRepository;
import gouv.education.apogee.commun.client.ws.ReferentielMetier.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.yaml.snakeyaml.Yaml;

import java.util.List;

@Endpoint
public class ReferentielMetierEndpoint {
    private static final Log log = LogFactory.getLog(ReferentielMetierEndpoint.class);
    private static final String NAMESPACE_URI = "gouv.education.apogee.commun.servicesmetiers.ReferentielMetier_21062012";
    private final ComposanteMetierRepository composanteRepository;
    private final RegimeInscriptionMetierRepository regimeInscriptionRepository;


    public ReferentielMetierEndpoint(ComposanteMetierRepository composanteRepository, RegimeInscriptionMetierRepository regimeInscriptionRepository) {
        this.composanteRepository = composanteRepository;
        this.regimeInscriptionRepository = regimeInscriptionRepository;
    }

    /** @see ReferentielMetierServiceInterface#recupererComposanteV2(String, String) */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "recupererComposante_v2")
    @ResponsePayload
    public RecupererComposanteV2Response recupererComposanteV2(@RequestPayload RecupererComposanteV2 request)
            throws WebBaseException_Exception {
        String codCmp = request.getCodCmp();
        log.info("Request for ReferentielMetier.recupererComposanteV2("+codCmp+","+request.getSens()+")");
        RecupererComposanteV2Response response = new RecupererComposanteV2Response();
        List<ComposanteDTO3> liste = response.getRecupererComposanteV2Return();

        composanteRepository.liste()
                        .filter(cmp -> codCmp == null || cmp.getCodCmp().equals(codCmp))
                        .forEach(liste::add);

        return response;
    }

    /** @see ReferentielMetierServiceInterface#recupererRegimeInscription(String, String)  */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "recupererRegimeInscription")
    @ResponsePayload
    public RecupererRegimeInscriptionResponse recupererRegimeInscription(@RequestPayload RecupererRegimeInscription request) {
        String codRegIns = request.getCodRegIns();
        String temoinEnService = request.getTemoinEnService();
        log.info("Request for AdministratifMetier.recupererRegimeInscription("+codRegIns+","+temoinEnService+")");
        RecupererRegimeInscriptionResponse response = new RecupererRegimeInscriptionResponse();
        List<RegimeInscDTO> ris = response.getRecupererRegimeInscriptionReturn();

        if (codRegIns != null)
            regimeInscriptionRepository.get(codRegIns).ifPresent(ris::add);
        else
            regimeInscriptionRepository.liste().forEach(ris::add);

        return response;
    }

}
