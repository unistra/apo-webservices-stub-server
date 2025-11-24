package fr.unistra.dnum.apogee.ws.server.stub.infrastructure;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.OffreFormationMetierRepository;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.SEFilter;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmEtpDTO3;
import gouv.education.apogee.commun.client.ws.OffreFormationMetier.DiplomeDTO4;
import org.mapstruct.Mapper;
import org.mapstruct.control.DeepClone;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Repository;
import org.springframework.util.function.SingletonSupplier;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/** Repository for {@link DiplomeDTO4} pre-filled from {@link DataSet}'s entries
 * mapped using {@link OffreFormationMapper} */
@Repository
@Import({
        OffreFormationMapperImpl.class,
        DataSet.class,
})
public class OffreFormationMetierYamlRepository implements OffreFormationMetierRepository {
    private static final DiplomeClone CLONE = Mappers.getMapper(DiplomeClone.class);
    private final SingletonSupplier<Map<String, DiplomeDTO4>> diplomes;

    public OffreFormationMetierYamlRepository(OffreFormationMapper mapper, DataSet dataSet) {
        diplomes = SingletonSupplier.of(() -> init(mapper, dataSet));
    }

    protected static Map<String, DiplomeDTO4> init(OffreFormationMapper mapper, DataSet dataSet) {
        Map<String,DiplomeDTO4> diplomes = new TreeMap<>();
        for (Person person : dataSet)
            for (InsAdmEtpDTO3 etape : person.etapes()) {
                DiplomeDTO4 diplome = mapper.toDiplome(etape);
                if (diplome.getCodDip() == null) diplome.setCodDip("");
                diplomes.compute(diplome.getCodDip(), (codDip, existing) -> mapper.merge(existing, diplome));
            }
        return diplomes;
    }


    @Override
    public Stream<DiplomeDTO4> liste(SEFilter OFFilter) {
        return diplomes.get().values().stream()
                .filter(OFFilter::test)
                .map(CLONE::clone)
                .map(OFFilter::filter);
    }

    @Mapper(mappingControl = DeepClone.class)
    interface DiplomeClone {
        DiplomeDTO4 clone(DiplomeDTO4 diplome);
    }

}
