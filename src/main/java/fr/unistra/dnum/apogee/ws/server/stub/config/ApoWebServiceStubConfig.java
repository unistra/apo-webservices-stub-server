package fr.unistra.dnum.apogee.ws.server.stub.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurer;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

@EnableWs
@Configuration
public class ApoWebServiceStubConfig implements WsConfigurer {
    public static final String SERVICES_URI = "/services/";

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> servletRegistrationBean(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, SERVICES_URI + "*");
    }

    private static SimpleWsdl11Definition getWsdlDefinition(String wsdl) {
        return new SimpleWsdl11Definition(new ClassPathResource(wsdl));
    }

    @Bean(name="EtudiantMetier")
    public SimpleWsdl11Definition etudiantMetierServiceInterface() {
        return getWsdlDefinition("/wsdl/EtudiantMetier_Impl.wsdl");
    }

    @Bean(name="AdministratifMetier")
    public SimpleWsdl11Definition administratifMetierServiceInterface() {
        return getWsdlDefinition("/wsdl/AdministratifMetier_Impl.wsdl");
    }

    @Bean(name="PedagogiqueMetier")
    public SimpleWsdl11Definition pedagogiqueMetierServiceInterface() {
        return getWsdlDefinition("/wsdl/PedagogiqueMetier_Impl.wsdl");
    }

    @Bean(name="GeographieMetier")
    public SimpleWsdl11Definition geographieMetierServiceInterface() {
        return getWsdlDefinition("/wsdl/GeographieMetier_01082016_Impl.wsdl");
    }

    @Bean(name="ReferentielMetier")
    public SimpleWsdl11Definition referentielMetierServiceInterface() {
        return getWsdlDefinition("/wsdl/ReferentielMetier_21062012_Impl.wsdl");
    }

    @Bean(name="OffreFormationMetier")
    public SimpleWsdl11Definition offreFormationMetierServiceInterface() {
        return getWsdlDefinition("/wsdl/OffreFormationMetier_Impl.wsdl");
    }

}
