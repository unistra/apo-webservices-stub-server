package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.*;

import java.util.*;

import static fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml.NodeComparator.compareNodes;

/**
 * Identify dupplicated {@link Node} to create {@link AnchorNode}
 * or merge mappings ({@link Tag#MERGE}
 * @see AnchorNode
 * @see org.yaml.snakeyaml.util.MergeUtils#flatten(MappingNode)  
 */
public class Dedupplicate {
    protected final Map<Node, AnchorNode> representedNodes;

    public Dedupplicate(Comparator<Node> nodeComparator) {
        representedNodes = new TreeMap<>(nodeComparator);
    }

    /** @return a merge {@link MappingNode} equivalent (ie a single merge key to the given {@link AnchorNode}) */
    static MappingNode merge(AnchorNode anchorNode) {
        MappingNode merge = new MappingNode(Tag.MAP, true,
                List.of(newMergeTuple(anchorNode)),
                anchorNode.getStartMark(), anchorNode.getEndMark(),
                getNodeFlowStyle(anchorNode.getRealNode()));
        merge.setType(anchorNode.getRealNode().getType());
        merge.setMerged(true);
        return merge;
    }

    static Node unwrap(Node node) {
        return singleMergeAnchor(node)
                .map(AnchorNode::getRealNode)
                .orElse(node);
    }

    /** @return a {@link NodeTuple} where {@link #merge(AnchorNode)} has been reverted if possible */
    static NodeTuple simplify(NodeTuple nodeTuple) {
        return singleMergeAnchor(nodeTuple.getValueNode())
                .map(anchorNode -> new NodeTuple(nodeTuple.getKeyNode(),anchorNode))
                .orElse(nodeTuple);
//                nodeTuple.getValueNode() instanceof MappingAnchorNode node ? new NodeTuple(nodeTuple.getKeyNode(), node.unwrap()) : nodeTuple;
    }

    /** @return the original {@link AnchorNode} from {@link #merge(AnchorNode)} if possible */
    static Optional<AnchorNode> singleMergeAnchor(Node node) {
        if (node instanceof AnchorNode anchorNode)
            return Optional.of(anchorNode);
        if (node instanceof MappingNode mappingNode
            && mappingNode.getValue().size() == 1
            && mappingNode.isMerged()) {
            NodeTuple first = mappingNode.getValue().getFirst();
            if (first.getKeyNode().getTag().equals(Tag.MERGE)
                && first.getValueNode() instanceof AnchorNode anchorNode)
                return Optional.of(anchorNode);
        }
        return Optional.empty();
    }

    /** @return a merge {@link NodeTuple} to the given {@link AnchorNode} */
    static NodeTuple newMergeTuple(AnchorNode anchor) {
        return new NodeTuple(
                new ScalarNode(Tag.MERGE, true, "<<", null, null, DumperOptions.ScalarStyle.PLAIN),
                anchor
        );
    }

    /** @return guessed {@link DumperOptions.FlowStyle} from node */
    private static DumperOptions.FlowStyle getNodeFlowStyle(Node node) {
        return switch (node) {
            case AnchorNode anchorNode -> getNodeFlowStyle(anchorNode.getRealNode());
            case MappingNode mappingNode -> mappingNode.getFlowStyle();
            case SequenceNode sequenceNode -> sequenceNode.getFlowStyle();
            default -> DumperOptions.FlowStyle.AUTO;
        };
    }

    static MappingNode composeMergedMapping(AnchorNode anchor, MappingNode original, MappingNode dupplicated) {
        List<NodeTuple> mergedTuples = new LinkedList<>();
        mergedTuples.add(newMergeTuple(anchor));
        Map<Node,Node> originalMap = new TreeMap<>(NodeComparator.INSTANCE);
        for (NodeTuple originalTuple : original.getValue())
            originalMap.put(originalTuple.getKeyNode(),originalTuple.getValueNode());
        for (NodeTuple dupplicatedTuple : dupplicated.getValue())
            if (! originalMap.containsKey(dupplicatedTuple.getKeyNode())
            || compareNodes(originalMap.get(dupplicatedTuple.getKeyNode()), dupplicatedTuple.getValueNode()) != 0 )
                mergedTuples.add(dupplicatedTuple);
        MappingNode mergedNode = new MappingNode(dupplicated.getTag(), true, mergedTuples,
                dupplicated.getStartMark(), dupplicated.getEndMark(), dupplicated.getFlowStyle());
        mergedNode.setType(dupplicated.getType());
        mergedNode.setMerged(true);
        return mergedNode;
    }

    public Node process(Node node) {
        if (isDupplicate(node))
            return dedupplicateFrom(representedNodes.get(node), node);
        seen(node);
        return node;
    }

    public void clear() {
        representedNodes.clear();
    }

    protected boolean isDupplicate(Node node) {
        return !(node instanceof AnchorNode)
                && representedNodes.containsKey(node);
    }

    protected void seen(Node node) {
        if (node instanceof MappingNode mappingNode && mappingNode.getValue().size() > 1)
            representedNodes.put(node, new AnchorNode(node));
        if (node instanceof SequenceNode sequenceNode &&  sequenceNode.getValue().size() > 1)
            representedNodes.put(node, new AnchorNode(node));
    }

    protected Node dedupplicateFrom(AnchorNode original, Node dupplicated) {
        if (compareNodes(original.getRealNode(),dupplicated) != 0
                && original.getRealNode() instanceof MappingNode originalMap
                && dupplicated instanceof MappingNode dupplicatedMap)
            return composeMergedMapping(original, originalMap,dupplicatedMap);
        else
            return original;
    }

}
