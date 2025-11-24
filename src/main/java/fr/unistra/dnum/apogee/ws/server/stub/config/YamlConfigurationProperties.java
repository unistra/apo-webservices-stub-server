package fr.unistra.dnum.apogee.ws.server.stub.config;

import fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml.TypedAnchorGenerator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@ConfigurationProperties(prefix = "yaml")
public class YamlConfigurationProperties {
    private final LoaderOptions loaderOptions = new LoaderOptions();
    private final DumperOptions dumperOptions = new DumperOptions();
    private Map<Class<?>, TypedAnchorGenerator.PropertyBasedAnchorGenerator> anchorProperties = new IdentityHashMap<>();
    private Map<Class<?>, Set<String>> ouiNonProperties = new IdentityHashMap<>();

    public LoaderOptions getLoaderOptions() {
        return loaderOptions;
    }

    public DumperOptions getDumperOptions() {
        return this.dumperOptions;
    }

    public void setAnchorProperties(Map<Class<?>, TypedAnchorGenerator.PropertyBasedAnchorGenerator> anchorProperties) {
        this.anchorProperties = anchorProperties;
    }

    public Map<Class<?>, TypedAnchorGenerator.PropertyBasedAnchorGenerator> getAnchorProperties() {
        return anchorProperties;
    }

    public Map<Class<?>, Set<String>> getOuiNonProperties() {
        return ouiNonProperties;
    }

    public void setOuiNonProperties(Map<Class<?>, Set<String>> ouiNonProperties) {
        this.ouiNonProperties = ouiNonProperties;
    }

}
