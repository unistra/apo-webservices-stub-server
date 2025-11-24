package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmEtpDTO3;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.CoordonneesDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.InfoAdmEtuDTO4;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Determine if a specific property key=value should be ignored by {@link Representer} */
public class IgnoredProperties {
    private static final Representer rawRepresenter = new Representer(new DumperOptions());
    private final Map<Class<?>, Set<String>> ouiNonProperties;

    public IgnoredProperties(Map<Class<?>,Set<String>> ouiNonProperties) {
        this.ouiNonProperties = ouiNonProperties;
    }

    /** filter unwanted properties :
     * <ul>
     *     <li>{@code null} values</li>
     *     <li>{@code N} value on O/N properties {@link #isOuiNonProperty}</li>
     *     <li>ldap property if it's value is the default one {@link Person#defaultLdap(InfoAdmEtuDTO4, CoordonneesDTO2[], InsAdmEtpDTO3[])}</li>
     * </ul> */
    public boolean isIgnored(Object javaBean, Property property, Object propertyValue, Tag customTag) {
        // ignore null values
        if (propertyValue == null)
            return true;

        // ignore empty collections
        if (propertyValue instanceof Collection<?> collection && collection.isEmpty())
            return true;

        // ignore "O"/"N" property if value is "N"
        if (isOuiNonProperty(javaBean.getClass(), property.getName())
                && propertyValue instanceof String value && "N".equalsIgnoreCase(value))
            return true;

        // ignore ldap property if value is the default one
        if ("ldap".equals(property.getName())
                && javaBean instanceof Person person
                && NodeComparator.INSTANCE.compare(
                    rawRepresenter.represent(person.ldap()),
                    rawRepresenter.represent(Person.defaultLdap(person.infoAdm(), person.coordonnees(), person.etapes()))
                ) == 0)
            return true;

        // otherwise do not ignore
        return false;
    }

    /** filter unwanted results */
    public boolean isIgnored(Property property, NodeTuple nodeTuple) {
        // ignore empty objects
        return isEmptyMapping(nodeTuple.getValueNode());
    }

    private boolean isOuiNonProperty(Class<?> javaBeanClass, String propertyName) {
        return ouiNonProperties.getOrDefault(javaBeanClass, Set.of()).contains(propertyName);
    }

    private boolean isEmptyMapping(Node valueNode) {
        return valueNode instanceof MappingNode mappingNode && mappingNode.getValue().isEmpty();
    }
}
