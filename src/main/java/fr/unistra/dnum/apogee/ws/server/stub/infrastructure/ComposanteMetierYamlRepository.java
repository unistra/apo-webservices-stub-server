package fr.unistra.dnum.apogee.ws.server.stub.infrastructure;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.ComposanteMetierRepository;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.ComposanteDTO;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmEtpDTO3;
import gouv.education.apogee.commun.client.ws.ReferentielMetier.ComposanteDTO3;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Repository;
import org.springframework.util.function.SingletonSupplier;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/** Repository for {@link ComposanteDTO3} pre-filled from {@link DataSet}'s entries */
@Repository
public class ComposanteMetierYamlRepository implements ComposanteMetierRepository {
    private static final ComposanteMapper MAPPER = Mappers.getMapper(ComposanteMapper.class);
    private final SingletonSupplier<Map<String,ComposanteDTO3>> composantes;

    public ComposanteMetierYamlRepository(DataSet dataSet) {
        composantes = SingletonSupplier.of(() -> init(dataSet));
    }

    protected static Map<String, ComposanteDTO3> init(DataSet dataSet) {
        Map<String,ComposanteDTO3> composantes = new TreeMap<>();
        for (Person person : dataSet)
            for (InsAdmEtpDTO3 etape : person.etapes()) {
                ComposanteDTO3 composante = MAPPER.toComposanteDTO3(etape.getComposante());
                composantes.compute(composante.getCodCmp(), (codCmp, existing) -> MAPPER.merge(existing, composante));
            }
        return composantes;
    }

    @Override
    public Stream<ComposanteDTO3> liste() {
        return composantes.get().values().stream();
    }

    @Mapper
    interface ComposanteMapper {
        @Mapping(target = "codCmp", source = "codComposante")
        @Mapping(target = "libCmp", source = "libComposante")
        @Mapping(target = "libWebCmp", source = "libComposante")
        @Mapping(target = "temEnSveCmp", constant = "O")
        @Mapping(target = "temElcCmp", constant = "O")
        ComposanteDTO3 toComposanteDTO3(ComposanteDTO iaComposante);

        @AfterMapping
        default void afterMapping(@MappingTarget ComposanteDTO3 target) {
            if (target.getLibCmp() == null) {
                target.setLibCmp(target.getCodCmp());
                target.setLibWebCmp(target.getCodCmp());
            }
        }

        default ComposanteDTO3 merge(ComposanteDTO3 existing, ComposanteDTO3 newComposante) {
            return existing == null || existing.getLibCmp().equals(existing.getCodCmp())
                    ? newComposante : existing;
        }
    }

}
