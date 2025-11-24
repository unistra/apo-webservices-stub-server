package fr.unistra.dnum.apogee.ws.server.stub.domaine;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;


public interface EtudiantMetierRepository {

    default Optional<Person> recuperer(Predicate<Person> filter) {
        return liste(filter).findAny();
    }

    Stream<Person> liste(Predicate<Person> matcher);

    default Optional<Person> recuperer(String codEtu) {
        return recuperer(matcher(codEtu));
    }

    static Predicate<Person> matcher(String codEtu) {
        return EtudiantPredicateBuilder.builder()
                .matchCodEtu(codEtu).build();
    }

}
