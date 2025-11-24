package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.serializer.AnchorGenerator;

import java.util.Map;
import java.util.Set;

import static fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml.Dedupplicate.merge;

/** {@link Person} → {@link Node} tree */
public class PersonRepresenter extends Representer {
    private final Dedupplicate dedupplicate;
    private final IgnoredProperties ignoredProperties;
    private final AnchorGenerator anchorGenerator;

    public PersonRepresenter(DumperOptions options, Dedupplicate dedupplicate, IgnoredProperties ignoredProperties) {
        super(options);
        this.dedupplicate = dedupplicate;
        this.ignoredProperties = ignoredProperties;
        this.anchorGenerator = options.getAnchorGenerator();
    }

    public void addRepresent(Class<?> type, Represent represent) {
        this.representers.put(type, represent);
    }

    @Override
    public Node represent(Object data) {
        try {
            return super.represent(data);
        } finally {
            dedupplicate.clear();
            if (anchorGenerator instanceof TypedAnchorGenerator typedAnchorGenerator) typedAnchorGenerator.close();
        }
    }

    @Override
    protected Node representMapping(Tag tag, Map<?, ?> mapping, DumperOptions.FlowStyle flowStyle) {
        return dedupplicate.process(super.representMapping(tag, mapping, flowStyle));
    }

    @Override
    protected Node representSequence(Tag tag, Iterable<?> sequence, DumperOptions.FlowStyle flowStyle) {
        return dedupplicate.process(super.representSequence(tag, sequence, flowStyle));
    }

    @Override
    protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
        MappingNode node = super.representJavaBean(properties, javaBean);
        if (node.getType() == Object.class)
            node.setType(javaBean.getClass());
        return switch (dedupplicate.process(node)) {
            case MappingNode mappingNode -> mappingNode;
            case AnchorNode anchorNode -> merge(anchorNode); // wrap
            default -> node;
        };
    }

    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
        if (ignoredProperties.isIgnored(javaBean,property,propertyValue,customTag))
            return null;

        NodeTuple nodeTuple = Dedupplicate.simplify(super.representJavaBeanProperty(javaBean, property, propertyValue, customTag));

        if (ignoredProperties.isIgnored(property, nodeTuple))
            return null;
        if (nodeTuple.getValueNode() instanceof MappingNode mappingNode && mappingNode.getValue().isEmpty())
            return null; // skip empty object value

        return nodeTuple;
    }


}
