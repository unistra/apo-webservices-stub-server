package fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.SupannPerson;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmAnuDTO2;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmEtpDTO3;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.CoordonneesDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.InfoAdmEtuDTO4;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.introspector.PropertySubstitute;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describe {@link Person} type for (de)serializer
 * (as it's an immutable {@link Record} with a mutable builder class)
 * @see Person
 * @see Person.PersonBuilder
 * @see org.yaml.snakeyaml.constructor.Constructor#addTypeDescription(TypeDescription) 
 * @see org.yaml.snakeyaml.representer.Representer#addTypeDescription(TypeDescription) 
 */
public class PersonTypeDescription extends TypeDescription {
    public PersonTypeDescription() {
        super(Person.class, Tag.MAP, Person.PersonBuilder.class);
        substituteProperty("codInd", Integer.class, Person::codInd, Person.PersonBuilder::codInd);
        substituteProperty("ldap", SupannPerson.class, Person::ldap, Person.PersonBuilder::ldap);
        substituteProperty("infoAdm", InfoAdmEtuDTO4.class, Person::infoAdm, Person.PersonBuilder::infoAdm);
        substituteProperty("coordonnees", CoordonneesDTO2[].class, Person::coordonnees, Person.PersonBuilder::coordonnees, CoordonneesDTO2.class);
        substituteProperty("ias", InsAdmAnuDTO2[].class,Person::ias, Person.PersonBuilder::ias, InsAdmAnuDTO2.class);
        substituteProperty("etapes", InsAdmEtpDTO3[].class,Person::etapes, Person.PersonBuilder::etapes, InsAdmEtpDTO3.class);
    }

    public <T> void substituteProperty(String pName, Class<T> pType, Function<Person,T> getter, BiConsumer<Person.PersonBuilder,T> setter, Class<?>... argParams) {
        substituteProperty(new MethodPropertySubstitute<Person, Person.PersonBuilder,T>(pName, pType, getter, setter, argParams));
    }

    /** use mutable {@link Person.PersonBuilder} instead of immutable {@link Person} */
    @Override
    public Object newInstance(Node node) {
        return Person.builder();
    }

    /** finalize with Builder's terminal operation
     * @see Person.PersonBuilder#build() */
    @Override
    public Object finalizeConstruction(Object obj) {
        return ((Person.PersonBuilder) obj).build();
    }

    /** {@link PropertySubstitute} with provided read and write functions */
    private static class MethodPropertySubstitute<P,B,T> extends PropertySubstitute {

        private final Function<P, T> read;
        private final BiConsumer<B, T> write;

        public MethodPropertySubstitute(String name, Class<T> type, Function<P,T> read, BiConsumer<B,T> write, Class<?>... params) {
            super(name, type, null, null, params);
            this.read = read;
            this.write = write;
        }

        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public Object get(Object object) {
            return read.apply((P) object);
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public void set(Object object, Object value) throws Exception {
            write.accept((B) object, (T) value);
        }
    }

}
