package fr.unistra.dnum.apogee.ws.server.stub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WSDLStaticConfig implements WebMvcConfigurer {
    public static final String WSDL_BASE_URI = "/wsdl/";
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler(WSDL_BASE_URI+"**")
            .addResourceLocations("classpath:/wsdl/");
    }
}
