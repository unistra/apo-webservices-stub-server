package fr.unistra.dnum.apogee.ws.server.stub.presentation.ldap;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.ldif.LDIFException;
import com.unboundid.ldif.LDIFReader;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.ldap.LdapProperties;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapAutoConfiguration;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

@AutoConfiguration
@EnableAutoConfiguration(exclude = { EmbeddedLdapAutoConfiguration.class })
@EnableConfigurationProperties({ LdapProperties.class, EmbeddedLdapProperties.class })
public class DirectoryServer implements DisposableBean {

    private static final String PROPERTY_SOURCE_NAME = "ldap.ports";
    private InMemoryDirectoryServer server;

    @FunctionalInterface
    public interface Configurer {
        void configure(InMemoryDirectoryServerConfig config) throws LDAPException;
    }

    @Bean
    public InMemoryDirectoryServerConfig directoryServerConfig(
            EmbeddedLdapProperties embeddedProperties,
            List<InMemoryListenerConfig> listenerConfig,
            List<Schema> schemas,
            List<Configurer> configCustomizers
    ) throws LDAPException {
        String[] baseDn = StringUtils.toStringArray(embeddedProperties.getBaseDn());
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(baseDn);

        config.setListenerConfigs(listenerConfig);

        if (!embeddedProperties.getValidation().isEnabled())
            config.setSchema(null);
        else if (! schemas.isEmpty())
            config.setSchema(Schema.mergeSchemas(Stream.concat(
                    Stream.of(Schema.getDefaultStandardSchema()),
                    schemas.stream()
            ).toArray(Schema[]::new)));

        for (Configurer customizer : configCustomizers)
            customizer.configure(config);

        return config;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.ldap.embedded.credential.username")
    @ConditionalOnProperty(name = "spring.ldap.embedded.credential.password")
    public Configurer addBindCredential(EmbeddedLdapProperties embeddedProperties) {
        return config -> config.addAdditionalBindCredentials(
                embeddedProperties.getCredential().getUsername(),
                embeddedProperties.getCredential().getPassword()
        );
    }

    @Bean
    @ConditionalOnMissingBean(InMemoryListenerConfig.class)
    public InMemoryListenerConfig directoryServerListenerConfig(EmbeddedLdapProperties embeddedProperties) throws LDAPException {
        return InMemoryListenerConfig.createLDAPConfig("LDAP", embeddedProperties.getPort());
    }

    @Bean
    @ConditionalOnBooleanProperty("spring.ldap.embedded.validation.enabled")
    @ConditionalOnProperty(name = "spring.ldap.embedded.validation.schema")
    public Schema schema(EmbeddedLdapProperties embeddedProperties) throws IOException, LDIFException {
        Resource schema = embeddedProperties.getValidation().getSchema();
        try {
            return Schema.getSchema(schema.getFile());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load schema " + schema.getDescription(), ex);
        }
    }

    @Bean
    public InMemoryDirectoryServer directoryServer(
            InMemoryDirectoryServerConfig config,
            List<Consumer<InMemoryDirectoryServer>> preStartCustomizers,
            ApplicationContext applicationContext) throws LDAPException {
        this.server = new InMemoryDirectoryServer(config);
        for (Consumer<InMemoryDirectoryServer> customizer : preStartCustomizers)
            customizer.accept(this.server);
        this.server.startListening();
        setPortProperty(applicationContext, this.server.getListenPort());
        return this.server;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.ldap.embedded.ldif")
    public Consumer<InMemoryDirectoryServer> importLdif(EmbeddedLdapProperties embeddedProperties, ApplicationContext applicationContext) {
        String location = embeddedProperties.getLdif();
        return server -> {
            try {
                Resource resource = applicationContext.getResource(location);
                if (resource.exists()) {
                    try (InputStream inputStream = resource.getInputStream()) {
                        server.importFromLDIF(true, new LDIFReader(inputStream));
                    }
                }
            }
            catch (Exception ex) {
                throw new IllegalStateException("Unable to load LDIF " + location, ex);
            }
        };
    }

    private void setPortProperty(ApplicationContext context, int port) {
        if (context instanceof ConfigurableApplicationContext configurableContext) {
            MutablePropertySources sources = configurableContext.getEnvironment().getPropertySources();
            getLdapPorts(sources).put("local.ldap.port", port);
        }
        if (context.getParent() != null) {
            setPortProperty(context.getParent(), port);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getLdapPorts(MutablePropertySources sources) {
        PropertySource<?> propertySource = sources.get(PROPERTY_SOURCE_NAME);
        if (propertySource == null) {
            propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, new HashMap<>());
            sources.addFirst(propertySource);
        }
        return (Map<String, Object>) propertySource.getSource();
    }

    @Override
    public void destroy() throws Exception {
        if (this.server != null) {
            this.server.shutDown(true);
        }
    }

}
