package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import fr.unistra.dnum.apogee.ws.server.stub.config.YamlConfigurationProperties;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.serializer.AnchorGenerator;

import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Determine anchor names
 * based on known property(ies) values
 * for known types
 * @see AnchorGenerator
 */
public class TypedAnchorGenerator implements AnchorGenerator, AutoCloseable {
    private final Map<Class<?>, PropertyBasedAnchorGenerator> prefixes;
    private final Map<String, AtomicInteger> anchors = new TreeMap<>();

    public TypedAnchorGenerator(Map<Class<?>, PropertyBasedAnchorGenerator> prefixes) {
        this.prefixes = new IdentityHashMap<>(prefixes.size());
        this.prefixes.putAll(prefixes);
    }

    @Override
    public void close() {
        anchors.clear();
    }

    /**
     * Use a bean property to generate an anchor name
     * (and eventually a prefix)
     * <p>
     * Also store properties that can be merged
     * into merged nodes when finding duplicates
     * @see YamlConfigurationProperties#getAnchorProperties()
     * @see YamlConfiguration#dedupplicate(YamlConfigurationProperties)
     */
    public static class PropertyBasedAnchorGenerator implements AnchorGenerator {
        private List<String> property = null;
        private String prefix = null;
        private Set<String> mergeableProperties = new TreeSet<>();

        public PropertyBasedAnchorGenerator() { }

        public PropertyBasedAnchorGenerator(String property) {
            this.property = List.of(property);
        }

        public List<String> getProperty() {
            return property;
        }

        public void setProperty(List<String> property) {
            this.property = property;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String nextAnchor(Node node) {
            return findProperties(getProperty(),node)
                    .map(p -> prefix == null ? p : prefix + "_" + p)
                    .orElse(prefix);
        }

        protected static Optional<String> findProperties(List<String> properties, Node node) {
            return properties != null ? properties.stream()
                    .flatMap(property -> findProperty(property,node).stream())
                    .reduce((l,r)-> String.join("_",l,r))
                    : Optional.empty();
        }

        protected static Optional<String> findProperty(String property, Node node) {
            if (property == null) return Optional.empty();
            String[] path = property.split("[.]");
            for (String prop : path) {
                node = findPropertyNode(prop,node).orElse(null);
                if (node == null) return Optional.empty();
            }
            if (node instanceof ScalarNode scalarNode)
                return Optional.of(scalarNode.getValue());
            return Optional.empty();
        }
        protected static Optional<Node> findPropertyNode(String property, Node node) {
            if (node instanceof AnchorNode anchorNode)
                node = anchorNode.getRealNode();
            if (node instanceof MappingNode mappingNode)
                for (NodeTuple nodeTuple : mappingNode.getValue())
                    if (nodeTuple.getKeyNode() instanceof ScalarNode keyNode
                            && property.equals(keyNode.getValue()))
                        return Optional.of(nodeTuple.getValueNode());
            return Optional.empty();
        }

        public Set<String> getMergeableProperties() {
            return mergeableProperties;
        }

        public void setMergeableProperties(Set<String> mergeableProperties) {
            this.mergeableProperties = mergeableProperties;
        }

    }

    /** @return a new anchor name */
    @Override
    public String nextAnchor(Node node) {
        if (node.getAnchor() != null) {
            // keep the anchor when it is set explicitly
            return node.getAnchor();
        }

        // unwarp inner anchor (weird ?)
        if (node instanceof AnchorNode anchorNode)
            node = anchorNode.getRealNode();

        Class<?> nodeType = node.getType();
        String prefix = (prefixes.containsKey(nodeType)
                    ? Optional.ofNullable(prefixes.get(nodeType).nextAnchor(node))
                    : Optional.<String>empty())
                .or(() -> defaultPrefix(nodeType))
                .map(TypedAnchorGenerator::unaccent)
                .map(TypedAnchorGenerator::longAcronym)
                .map(TypedAnchorGenerator::stripNonAlNum)
                .map(TypedAnchorGenerator::snakeCaseToCamelCase)
                .orElse("id");

        if (!anchors.containsKey(prefix)) {
            anchors.put(prefix, new AtomicInteger(0));
            return prefix;
        } else
            return prefix + "_" + anchors.get(prefix).incrementAndGet();
    }

    /** @return a default prefix based on class name if not java.lang */
    private Optional<String> defaultPrefix(Class<?> type) {
        if (!type.getName().startsWith("java.lang."))
            return Optional.of(type.getSimpleName());
        else
            return Optional.empty();
    }

    /** @return input stripped from its accents
     * (using Unicode normalization) */
    private static String unaccent(String input) {
        return input == null ? null : Normalizer.normalize(input, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "");
    }
    /** replace snake_case to camelCase when possible */
    private static String snakeCaseToCamelCase(String input) {
        return Pattern.compile("(?<=[a-z])_([a-zA-Z])")
                .matcher(input)
                .replaceAll(m -> m.group(1).toUpperCase());
    }
    /** replace A_B_C_D to ABCD when possible */
    public static String longAcronym(String input) {
        return Pattern.compile("(?<=(?:[.-]|^)[A-Z])[.-]([A-Z])(?=[.-]|$)")
                .matcher(input)
                .replaceAll(m -> m.group(1));
    }

    /** replace non alpha numeric characters */
    private static String stripNonAlNum(String p) {
        return p.replaceAll("[^a-zA-Z0-9]+", "_")
                .replaceAll("_+$", ""); // rtrim
    }

}
