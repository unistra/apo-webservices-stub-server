package fr.unistra.dnum.apogee.ws.export.cli;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.xml.ws.BindingProvider;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DebugWebServiceCallInterceptor implements MethodInterceptor {
    private final Logger logger;
    private final String endPointAddress;

    private DebugWebServiceCallInterceptor(Logger logger, String endPointAddress) {
        this.logger = logger;
        this.endPointAddress = endPointAddress;
    }

    @SuppressWarnings("unchecked")
    public static <I> I debugWSCall(I service) {
        Logger logger = getLogger(service.getClass());
        if (!logger.isDebugEnabled())
            return service;
        String endPointAddress = getEndPointAddress(service);
        ProxyFactory proxyFactory = new ProxyFactory(service);
        proxyFactory.addAdvice(new DebugWebServiceCallInterceptor(logger, endPointAddress));
        return (I) proxyFactory.getProxy();
    }

    private static String getEndPointAddress(Object service) {
        return service instanceof BindingProvider bp
                ? Objects.toString(bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY))
                : "<unknown>";
    }

    private static Logger getLogger(Class<?> serviceClass) {
        return LoggerFactory.getLogger(
                Arrays.stream(serviceClass.getInterfaces())
                        .filter(i -> i.isAnnotationPresent(WebService.class))
                        .findAny()
                        .orElse(serviceClass)
        );
    }

    @Override
    public Object invoke(MethodInvocation inv) throws Throwable {
        Method method = inv.getMethod();
        WebMethod webMethod = method.getAnnotation(WebMethod.class);
        if (webMethod != null && logger.isDebugEnabled())
            logger.debug("Appelle {}({}) → {}",
                    webMethod.operationName().isEmpty()
                            ? method.getName()
                            : webMethod.operationName(),
                    argumentsToString(method, inv.getArguments()),
                    endPointAddress
            );
        return inv.proceed();
    }

    private String argumentsToString(Method method, Object... arguments) {
        List<String> argumentsList = new ArrayList<>(arguments.length);
        Parameter[] parameters = method.getParameters();
        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            WebParam webParam = parameters[i].getAnnotation(WebParam.class);
            if (webParam != null && arguments[i] != null)
                argumentsList.add(webParam.name()+"="+ arguments[i]);
        }
        return String.join(",", argumentsList);
    }
}
