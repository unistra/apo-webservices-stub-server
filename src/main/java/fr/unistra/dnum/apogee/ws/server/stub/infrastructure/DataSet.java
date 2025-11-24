package fr.unistra.dnum.apogee.ws.server.stub.infrastructure;

import fr.unistra.dnum.apogee.ws.server.stub.config.DatasetConfigurationProperties;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml.YamlConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Repository;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Repository
@Import(YamlConfiguration.class)
@EnableConfigurationProperties(DatasetConfigurationProperties.class)
public class DataSet implements Iterable<Person>, InitializingBean, AutoCloseable {
    private static final Log log = LogFactory.getLog(DataSet.class);

    private final ResourcePatternResolver resolver;
    private final DatasetConfigurationProperties resources;
    private final Yaml yaml;
    private List<Person> people = List.of();
    private Deque<List<Person>> saved = new LinkedList<>();

    DataSet(ResourcePatternResolver resolver, DatasetConfigurationProperties resources, Yaml yaml) {
        this.resolver = resolver;
        this.resources = resources;
        this.yaml = yaml;
    }

    @Override
    public Iterator<Person> iterator() {
        return people.iterator();
    }

    @Override
    public void forEach(Consumer<? super Person> action) {
        people.forEach(action);
    }

    @Override
    public Spliterator<Person> spliterator() {
        return people.spliterator();
    }

    public Stream<Person> stream() {
        return people.stream();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        load(resources.resources(resolver)) ;
    }

    public List<Person> load(Resource... resources) throws IllegalArgumentException, IOException {
        saved.push(people);
        people = new LinkedList<>();
        List<IllegalArgumentException> errors = new LinkedList<>();
        for (final Resource resource : resources)
            try (InputStream inputStream = resource.getInputStream()) {
                log.info("Parsing "+ resource.getDescription());
                Collections.addAll(people,
                        yaml.loadAs(new UnicodeReader(inputStream), Person[].class));
            } catch (Exception e) {
                errors.add(new IllegalArgumentException("Error parsing "+ resource.getDescription(),e));
            }
        throwIfNotEmpty(errors, () -> new IllegalArgumentException("Errors parsing dataset"));
        return people;
    }

    private static <E extends Throwable> void throwIfNotEmpty(List<? extends E> errors, Supplier<E> multiple) throws E {
        if (errors.isEmpty()) return;
        if (errors.size() == 1) { throw errors.getFirst(); }
        E error = multiple.get();
        error.setStackTrace(new StackTraceElement[0]);
        for (E e : errors) error.addSuppressed(e);
        throw error;
    }

    @Override
    public void close() throws Exception {
        people = saved.isEmpty() ? List.of() : saved.pop();
    }

}
