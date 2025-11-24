package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import org.yaml.snakeyaml.nodes.*;

import java.util.*;
import java.util.stream.Collectors;

/** Deeply compare {@link Node} */
public class NodeComparator implements Comparator<Node> {
    public static final NodeComparator INSTANCE = new NodeComparator();
    public static int compareNodes(Node lhs, Node rhs) {
        return INSTANCE.compare(lhs, rhs);
    }

    /**
     * @see java.util.Comparator#compare(Object, Object)
     */
    public int compare(Node lhs, Node rhs) {
        if (lhs instanceof AnchorNode lhsAnchor) lhs = lhsAnchor.getRealNode();
        if (rhs instanceof AnchorNode rhsAnchor) rhs = rhsAnchor.getRealNode();
        if (lhs == rhs) return 0;
        else if (lhs == null) return -1;
        else if (rhs == null) return 1;
        else if (lhs.getNodeId() != rhs.getNodeId())
            return lhs.getNodeId().ordinal() - rhs.getNodeId().ordinal();
        else if (lhs instanceof ScalarNode lhsScalar && rhs instanceof ScalarNode rhsScalar)
            return compareScalars(lhsScalar, rhsScalar);
        else if (lhs instanceof SequenceNode lhsSeq && rhs instanceof SequenceNode rhsSeq)
            return compareSequences(lhsSeq, rhsSeq);
        else if (lhs instanceof MappingNode lhsMap && rhs instanceof MappingNode rhsMap)
            return compareMappings(lhsMap, rhsMap);
        int cmp = System.identityHashCode(lhs) - System.identityHashCode(rhs);
        return cmp != 0 ? cmp : -1;
    }

    /** @see java.util.Comparator#compare(Object, Object) */
    public int compareScalars(ScalarNode lhs, ScalarNode rhs) {
        return lhs.getScalarStyle() == rhs.getScalarStyle()
                ? lhs.getValue().compareTo(rhs.getValue())
                : lhs.getScalarStyle().ordinal() - rhs.getScalarStyle().ordinal();
    }

    /** @see java.util.Comparator#compare(Object, Object) */
    public int compareSequences(SequenceNode lhsSeq, SequenceNode rhsSeq) {
        if (lhsSeq == rhsSeq) return 0;
        ListIterator<Node> lhs = lhsSeq.getValue().listIterator();
        ListIterator<Node> rhs = rhsSeq.getValue().listIterator();
        while (lhs.hasNext() && rhs.hasNext()) {
            int cmp = compare(lhs.next(), rhs.next());
            if (cmp != 0)
                return cmp;
        }
        if (lhs.hasNext())
            return 1;
        else if (rhs.hasNext())
            return -1;
        else
            return 0;
    }

    /**
     * @see java.util.Comparator#compare(Object, Object)
     */
    public int compareMappings(MappingNode lhsMap, MappingNode rhsMap) {
        if (lhsMap == rhsMap) return 0;
        var lhs = toMap(lhsMap.getValue()).entrySet().iterator();
        var rhs = toMap(rhsMap.getValue()).entrySet().iterator();
        while (lhs.hasNext() && rhs.hasNext()) {
            Map.Entry<Node, Node> lhsEntry = lhs.next();
            Map.Entry<Node, Node> rhsEntry = rhs.next();
            int cmp = compare(lhsEntry.getKey(), rhsEntry.getKey());
            if (cmp != 0)
                return cmp;
            cmp = compareTuples(lhsEntry.getKey(), lhsEntry.getValue(), rhsEntry.getValue(), lhsMap, rhsMap);
            if (cmp != 0)
                return cmp;
        }
        if (lhs.hasNext())
            return 1;
        else if (rhs.hasNext())
            return -1;
        else
            return 0;
    }

    protected int compareTuples(Node key, Node lhs, Node rhs, MappingNode lhsParent, MappingNode rhsParent) {
        return compare(lhs, rhs);
    }

    private Map<Node, Node> toMap(List<NodeTuple> keyValues) {
        return keyValues.stream()
                .collect(Collectors.toMap(
                        NodeTuple::getKeyNode,
                        NodeTuple::getValueNode,
                        (node, node2) -> node,
                        () -> new TreeMap<>(this)
                ));
    }

    public static IgnoringPropertiesNodeComparator ignoringProperties(Class<?> type, String... properties) {
        return new IgnoringPropertiesNodeComparator()
                .ignore(type, properties);
    }

    /** Deeply compare {@link Node} but ignoring some properties */
    public static class IgnoringPropertiesNodeComparator extends NodeComparator {
        private final Map<String, Set<String>> ignoredProperties = new TreeMap<>();

        public IgnoringPropertiesNodeComparator ignore(Class<?> type, String... properties) {
            return ignore(type, Set.of(properties));
        }
        public IgnoringPropertiesNodeComparator ignore(Class<?> type, Set<String> properties) {
            ignoredProperties.computeIfAbsent(new Tag(type).getValue(), t -> new TreeSet<>()).addAll(properties);
            return this;
        }

        private boolean isIgnored(Node key, Tag tag) {
            return key instanceof ScalarNode scalarKey
                    && ignoredProperties.containsKey(tag.getValue())
                    && ignoredProperties.get(tag.getValue()).contains(scalarKey.getValue());
        }

        @Override
        protected int compareTuples(Node key, Node lhs, Node rhs, MappingNode lhsParent, MappingNode rhsParent) {
            return isIgnored(key, lhsParent.getTag())
                    ? 0
                    : super.compareTuples(key, lhs, rhs, lhsParent, rhsParent);
        }
    }
}
