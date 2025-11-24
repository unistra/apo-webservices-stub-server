package fr.unistra.dnum.apogee.ws.server.stub.config.filter;

import fr.unistra.dnum.apogee.ws.server.stub.config.ApoWebServiceStubConfig;
import fr.unistra.dnum.apogee.ws.server.stub.config.WSDLStaticConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WSDLLocationRewriteFilter extends GenericFilterBean {
    private final Map<String,String> endpointToWsdl;
    private final Map<String,String> wsdlToEntrypoint;

    WSDLLocationRewriteFilter(Map<String,SimpleWsdl11Definition> wsdls) {
        endpointToWsdl = new TreeMap<>();
        wsdlToEntrypoint = new TreeMap<>();
        wsdls.forEach((endpointName,wsdlDefinition) -> {
            String wsdl = basename(systemId(wsdlDefinition));
            endpointToWsdl.put(endpointName, wsdl);
            wsdlToEntrypoint.put(wsdl, endpointName);
        });
    }

    private static String systemId(WsdlDefinition wsdl) {
        return wsdl.getSource().getSystemId();
    }

    public static String basename(String systemId) {
        int last = systemId.lastIndexOf('/');
        return last < 0 || systemId.length() == 1
                ? systemId : systemId.substring(last+1);
    }

    private void sendRedirectTo(HttpServletResponse httpResponse, String uri) {
        httpResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        httpResponse.setHeader("Location", uri);
    }

    private void forwardTo(HttpServletRequest request, HttpServletResponse response, String uri)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher(uri);
        dispatcher.forward(request,response);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest
            && httpRequest.getMethod().equalsIgnoreCase("GET")
            && response instanceof HttpServletResponse httpResponse) {
            String requestURI = httpRequest.getRequestURI();

            // redirect /services/endpoint?wsdl → /wsdl/defintition.wsdl
            if (requestURI.startsWith(ApoWebServiceStubConfig.SERVICES_URI)
                && "wsdl".equals(httpRequest.getQueryString())) {
                String endpointName = basename(requestURI);
                if (endpointToWsdl.containsKey(endpointName)) {
                    sendRedirectTo(httpResponse, WSDLStaticConfig.WSDL_BASE_URI+endpointToWsdl.get(endpointName));
                    return;
                }
            }

            // resolve /wsdl/definition.wsdl using /services/endpoint.wsdl (for transform location)
            if (requestURI.startsWith(WSDLStaticConfig.WSDL_BASE_URI)) {
                String wsdl = basename(requestURI);
                if (wsdlToEntrypoint.containsKey(wsdl)) {
                    forwardTo(httpRequest, httpResponse,
                            ApoWebServiceStubConfig.SERVICES_URI+wsdlToEntrypoint.get(wsdl)+".wsdl");
                    return;
                }
            }

        }

        chain.doFilter(request, response);

    }

}
