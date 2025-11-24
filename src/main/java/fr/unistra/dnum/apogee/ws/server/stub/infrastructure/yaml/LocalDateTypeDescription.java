package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import gouv.education.apogee.commun.client.ws.utils.DateAdapter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;

import java.time.LocalDate;

public class LocalDateTypeDescription extends TypeDescription implements Represent {
    public LocalDateTypeDescription() {
        super(LocalDate.class, Tag.STR);
    }

    @Override
    public Object newInstance(Node node) {
        return DateAdapter.parseDate(((ScalarNode) node).getValue()) ;
    }

    @Override
    public Node representData(Object data) {
        String value = DateAdapter.printDate((LocalDate) data);
        return new ScalarNode(getTag(), value, null, null, DumperOptions.ScalarStyle.PLAIN);
    }

}
