package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import gouv.education.apogee.commun.client.ws.utils.DateAdapter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;

import java.time.LocalDateTime;

public class LocalDateTimeTypeDescription extends TypeDescription implements Represent {
    public LocalDateTimeTypeDescription() {
        super(LocalDateTime.class, Tag.STR);
    }

    @Override
    public Object newInstance(Node node) {
        return DateAdapter.parseDateTime(((ScalarNode) node).getValue()) ;
    }

    @Override
    public Node representData(Object data) {
        String value = DateAdapter.printDateTime((LocalDateTime) data);
        return new ScalarNode(getTag(), value, null, null, DumperOptions.ScalarStyle.PLAIN);
    }

}
