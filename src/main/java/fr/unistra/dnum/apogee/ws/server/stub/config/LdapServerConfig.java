package fr.unistra.dnum.apogee.ws.server.stub.config;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.sdk.*;
import com.unboundid.util.MinimalLogFormatter;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.SupannPerson;
import fr.unistra.dnum.apogee.ws.server.stub.infrastructure.DataSet;
import fr.unistra.dnum.apogee.ws.server.stub.presentation.ldap.DirectoryServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.ldap.LdapProperties;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.util.ReflectionUtils;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.text.MessageFormat;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.StreamHandler;
import java.util.stream.Stream;

@Configuration
@Import({ DirectoryServer.class, LdapAutoConfiguration.class })
public class LdapServerConfig {
    static final Log log = LogFactory.getLog(LdapServerConfig.class);
    public static final String ATTR_USER_PASSWORD = "userPassword";

    @Bean
    public DirectoryServer.Configurer accessLogRequestHandler() {
        final StreamHandler handler = new StreamHandler(System.out,
                new MinimalLogFormatter(null, false, false, true));
        handler.setLevel(Level.INFO);
        return config -> config.setAccessLogHandler(handler);
    }

    @Bean("populateDataSet")
    public InitializingBean populateDataSet(DataSet dataSet, LdapTemplate ldapTemplate) {
        Deque<Name> created = new LinkedList<>();
        return () -> {
            while (!created.isEmpty()) ldapTemplate.unbind(created.pop());
            dataSet.forEach(person -> created.push(populateSupannPerson(ldapTemplate, person.ldap())));
        };
    }

    private static Name populateSupannPerson(LdapTemplate ldapTemplate, SupannPerson supannPerson) {
        ldapTemplate.create(supannPerson);
        log.info(MessageFormat.format("Created LDAP entry : {0}", supannPerson.getDn()));
        return supannPerson.getDn();
    }

    @Bean("baseDNs")
    public Consumer<InMemoryDirectoryServer> baseDNs(
            EmbeddedLdapProperties embeddedProps,
            LdapProperties props) {
        List<String> baseDNs = Stream.concat(
                    embeddedProps.getBaseDn().stream(),
                    Stream.of(props.getBase())
                ).distinct()
                .toList();
        return ldap -> baseDNs.forEach(dn -> addIfNotExists(ldap, dn, "objectClass", "top"));
    }

    @ConditionalOnProperty(name = "spring.ldap.embedded.credential.username")
    @ConditionalOnProperty(name = "spring.ldap.embedded.credential.password")
    @DependsOn("baseDNs")
    @Bean
    public Consumer<InMemoryDirectoryServer> createBindUser(EmbeddedLdapProperties props) {
        String username = props.getCredential().getUsername();
        String password = props.getCredential().getPassword();
        String cn = getUsernameFromDN(username);
        return ldap -> addIfNotExists(ldap, username,
                "objectClass", "Person",
                "sn", cn, // required by Person
                "cn", cn, // required by Person
                "givenName", cn, // required by ESUP-SiScol
                ATTR_USER_PASSWORD, password
        );
    }

    private static String getUsernameFromDN(String value) {
        try {
            LdapName dn = new LdapName(value);
            Rdn attributes = dn.getRdn(dn.size() - 1);
            return attributes.getValue().toString();
        } catch (InvalidNameException ne) {
            return value;
        }
    }

    private static void addIfNotExists(LDAPInterface ldap, String dn, String... attributes) {
        try {
            Entry entry = entry(dn, attributes);
            ldap.add(entry);
            if (entry.hasAttribute(ATTR_USER_PASSWORD))
                entry.setAttribute(ATTR_USER_PASSWORD, "******");
            log.info("Created LDAP entry : \n" + entry.toLDIFString());
        } catch (LDAPException e) {
            if (e.getResultCode() != ResultCode.ENTRY_ALREADY_EXISTS)
                ReflectionUtils.rethrowRuntimeException(e);
        }
    }

    private static Entry entry(String dn, String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0)
            throw new IllegalArgumentException("Odd number of key-value pairs");
        Stream.Builder<Attribute> builder = Stream.builder();
        for (int i = 0; i < keyValuePairs.length; i+=2)
            builder.add(new Attribute(keyValuePairs[i],keyValuePairs[i+1]));
        return new Entry(dn, builder.build().toArray(Attribute[]::new));
    }
}
