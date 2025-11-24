package fr.unistra.dnum.apogee.ws.server.stub.infrastructure;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.EtudiantMetierRepository;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Repository;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Repository
@Import({DataSet.class})
public class EtudiantMetierYamlRepository implements EtudiantMetierRepository {
    private final DataSet dataSet;

    EtudiantMetierYamlRepository(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public Stream<Person> liste(Predicate<Person> filter) {
        return dataSet.stream().filter(filter);
    }

}
