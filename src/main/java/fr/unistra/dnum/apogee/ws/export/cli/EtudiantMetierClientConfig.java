package fr.unistra.dnum.apogee.ws.export.cli;

import gouv.education.apogee.commun.client.ws.AdministratifMetier.AdministratifMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.AdministratifMetierServiceInterfaceService;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.EtudiantMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.EtudiantMetierServiceInterfaceService;
import gouv.education.apogee.commun.client.ws.OffreFormationMetier.OffreFormationMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.OffreFormationMetier.OffreFormationMetierServiceInterfaceService;
import gouv.education.apogee.commun.client.ws.ReferentielMetier.ReferentielMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.ReferentielMetier.ReferentielMetierServiceInterfaceService;
import jakarta.jws.WebService;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.spi.Provider;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Configuration
@EnableConfigurationProperties(ApoWebServicesConfigurationProperties.class)
public class EtudiantMetierClientConfig {

    private final ApoWebServicesConfigurationProperties apows;
    public EtudiantMetierClientConfig(ApoWebServicesConfigurationProperties apows) {
        this.apows = apows;
    }

    public static <V> V tryRethrow(TryRethrow<V,? extends Exception> mayThrow) {
        try {
            return mayThrow.mayThrow();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public EtudiantMetierServiceInterface etudiantMetierServiceInterface() throws Exception {
        return createService(EtudiantMetierServiceInterfaceService.class);
    }

    @Bean
    public AdministratifMetierServiceInterface administratifMetierServiceInterface() throws Exception {
        return createService(AdministratifMetierServiceInterfaceService.class);
    }

    @Bean
    public ReferentielMetierServiceInterface referentielMetierServiceInterface() throws Exception {
        return createService(ReferentielMetierServiceInterfaceService.class);
    }


    @Bean
    public OffreFormationMetierServiceInterface offreFormationMetierServiceInterface() throws Exception {
        return createService(OffreFormationMetierServiceInterfaceService.class);
    }

    @SuppressWarnings("unchecked")
    private <S extends Service,T> T createService(Class<S> serviceClass) throws MalformedURLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Method method : serviceClass.getDeclaredMethods())
            if (method.isAnnotationPresent(WebEndpoint.class))
                return createService(
                        method.getAnnotation(WebEndpoint.class).name(),
                        serviceClass,
                        (Class<T>) method.getReturnType()
                );
        throw new IllegalStateException("Not a valid WS Service Class (@WebEndpoint not found)");
    }

    private <S extends Service,T> T createService(String endpointName, Class<S> serviceClass, Class<T> interfaceClass) throws MalformedURLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        S service = serviceClass.getDeclaredConstructor(URL.class)
                .newInstance(wsdlLocation(serviceClass));
        W3CEndpointReference endpointReference = Provider.provider().createW3CEndpointReference(
                endpointUrl(endpointName).toString(),
                webServiceQName(interfaceClass),
                webServiceClientQName(serviceClass),
                null,null, service.getWSDLDocumentLocation().toString(),
                null, null, null
        );
        T serviceInterface = service.getPort(endpointReference, interfaceClass);
        if (hasText(apows.getUsername())) {
            Map<String, Object> requestContext = ((BindingProvider) serviceInterface).getRequestContext();
            requestContext.put(BindingProvider.USERNAME_PROPERTY, apows.getUsername());
            requestContext.put(BindingProvider.PASSWORD_PROPERTY, apows.getPassword());
        }
        return serviceInterface;
    }

    private QName webServiceQName(Class<?> interfaceClass) {
        WebService webService = interfaceClass.getAnnotation(WebService.class);
        return new QName(webService.targetNamespace(), webService.name());
    }

    private QName webServiceClientQName(Class<? extends Service> serviceClass) {
        WebServiceClient webServiceClient = serviceClass.getAnnotation(WebServiceClient.class);
        return new QName(webServiceClient.targetNamespace(), webServiceClient.name());
    }

    private URL wsdlLocation(Class<? extends Service> serviceClass) {
        WebServiceClient webServiceClient = serviceClass.getAnnotation(WebServiceClient.class);
        return EtudiantMetierClientConfig.class.getResource(webServiceClient.wsdlLocation());
    }

    private URL endpointUrl(String endpointName) throws MalformedURLException {
        return UriComponentsBuilder.fromUri(apows.getUri())
                .path("/"+endpointName)
                .build().toUri()
                .toURL();
    }

    public interface TryRethrow<V,E extends Exception> {
        V mayThrow() throws E;
    }
}

