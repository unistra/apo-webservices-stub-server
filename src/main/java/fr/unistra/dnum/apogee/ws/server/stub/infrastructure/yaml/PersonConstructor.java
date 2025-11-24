package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;

import java.util.IdentityHashMap;
import java.util.Map;

/** {@link org.yaml.snakeyaml.nodes.Node} tree → {@link fr.unistra.dnum.apogee.ws.server.stub.domaine.Person} */
public class PersonConstructor extends Constructor {
    private final Map<Node,Map<Class<?>,Object>> constructedObjects = new IdentityHashMap<>();
    public PersonConstructor(LoaderOptions loadingConfig) {
        super(Object.class, loadingConfig);
    }

    /**
     * Due to anchors to another java type,
     * {@link Constructor} may re-use an instance
     * of a wrong java type.
     * Fix that by checking actual type
     * @see Constructor#constructObject(Node)
     */
    @Override
    protected Object constructObject(Node node) {
        Object data = super.constructObject(node);
        // check actual type (may have been re-used with anchors on a different type)
        if (!node.getType().isInstance(data))
            // bad things will happen → retry harder
            data = constructedObjects.computeIfAbsent(node, k -> new IdentityHashMap<>())
                    .computeIfAbsent(node.getType(), c -> reConstructObject(node));
        return data;
    }

    /** @see Constructor#constructObjectNoCheck(Node) */
    private Object reConstructObject(Node node) {
        Construct constructor = getConstructor(node);
        Object data = constructor.construct(node);
        data = finalizeConstruction(node, data);
        if (node.isTwoStepsConstruction()) {
            constructor.construct2ndStep(node, data);
        }
        return data;
    }

}
