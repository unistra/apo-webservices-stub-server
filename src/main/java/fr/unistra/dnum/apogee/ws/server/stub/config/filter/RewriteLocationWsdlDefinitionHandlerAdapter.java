package fr.unistra.dnum.apogee.ws.server.stub.config.filter;

import fr.unistra.dnum.apogee.ws.server.stub.config.ApoWebServiceStubConfig;
import fr.unistra.dnum.apogee.ws.server.stub.config.WSDLStaticConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.http.WsdlDefinitionHandlerAdapter;

import static fr.unistra.dnum.apogee.ws.server.stub.config.filter.WSDLLocationRewriteFilter.basename;

@Component("wsdlDefinitionHandlerAdapter")
public class RewriteLocationWsdlDefinitionHandlerAdapter extends WsdlDefinitionHandlerAdapter {

    @Override
    protected String transformLocation(String location, HttpServletRequest request) {
        String xForwardedProto = request.getHeader("X-Forwarded-Proto");
        String xForwardedHost = request.getHeader("X-Forwarded-Host");
        String xForwardedPort = request.getHeader("X-Forwarded-Port");

        String scheme = (StringUtils.hasText(xForwardedProto)) ? xForwardedProto : request.getScheme();
        String serverName = (StringUtils.hasText(xForwardedHost)) ? xForwardedHost : request.getServerName();
        int serverPort = (StringUtils.hasText(xForwardedPort)) ? Integer.parseInt(xForwardedPort)
                : request.getServerPort();

        StringBuilder url = new StringBuilder(scheme);
        url.append("://").append(serverName);
        boolean serverHasColonAfterAt = serverName.indexOf("@") < serverName.indexOf(":");
        if (!serverHasColonAfterAt) {
            url.append(':').append(serverPort);
        }
        if (location.startsWith("/")) {
            // a relative path, prepend the context path
            url.append(request.getContextPath()).append(location);
            return url.toString();
        }
        else {
            int idx = location.indexOf("://");
            if (idx != -1) {
                // a full url
                idx = location.indexOf('/', idx + 3);
                if (idx != -1) {
                    String path = location.substring(idx);
                    String endpoint = basename(path);
                    if (request.getRequestURI().endsWith(endpoint+".wsdl"))
                        url.append(ApoWebServiceStubConfig.SERVICES_URI).append(endpoint);
                    else
                        url.append(path);
                    return url.toString();
                }
            }
        }
        // relative path, return relative to WSDLStaticConfig.WSDL_BASE_URI
        return WSDLStaticConfig.WSDL_BASE_URI + location;
    }

}
