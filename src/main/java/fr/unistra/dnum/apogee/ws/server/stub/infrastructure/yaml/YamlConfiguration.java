package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import fr.unistra.dnum.apogee.ws.server.stub.config.YamlConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Represent;

import java.util.List;

/** provides a {@link Yaml} Bean
 * suitable for {@link fr.unistra.dnum.apogee.ws.server.stub.domaine.Person}'s
 * (de)serialization
 * @see YamlConfigurationProperties */
@Configuration
@EnableConfigurationProperties(YamlConfigurationProperties.class)
public class YamlConfiguration {

    @Bean
    Yaml yaml(YamlConfigurationProperties config) {
        LoaderOptions loadingConfig = config.getLoaderOptions();
        DumperOptions dumperOptions = config.getDumperOptions();
        dumperOptions.setAnchorGenerator(new TypedAnchorGenerator(config.getAnchorProperties()));
        Constructor constructor = new PersonConstructor(loadingConfig);
        PersonRepresenter representer = new PersonRepresenter(
                dumperOptions,
                dedupplicate(config),
                ignoredProperties(config));
        for (TypeDescription typeDescription : typeDescriptions()) {
            constructor.addTypeDescription(typeDescription);
            representer.addTypeDescription(typeDescription);
            if (typeDescription instanceof Represent represent)
                representer.addRepresent(typeDescription.getType(), represent);
        }
        XmlBeanPropertyUtils propertyUtils = new XmlBeanPropertyUtils();
        constructor.setPropertyUtils(propertyUtils);
        representer.setPropertyUtils(propertyUtils);
        return new Yaml(
                constructor,
                representer,
                dumperOptions, loadingConfig
        );
    }

    List<TypeDescription> typeDescriptions() {
        return List.of(
                new PersonTypeDescription(),
                new LocalDateTimeTypeDescription(),
                new LocalDateTypeDescription()
        );
    }

    Dedupplicate dedupplicate(YamlConfigurationProperties config) {
        NodeComparator.IgnoringPropertiesNodeComparator ignoringPropertiesNodeComparator = new NodeComparator.IgnoringPropertiesNodeComparator();
        config.getAnchorProperties().forEach((type,anchor) -> ignoringPropertiesNodeComparator.ignore(type,anchor.getMergeableProperties()));
        return new Dedupplicate(ignoringPropertiesNodeComparator);
    }

    IgnoredProperties ignoredProperties(YamlConfigurationProperties config) {
        return new IgnoredProperties(config.getOuiNonProperties());
    }

}
