package fr.unistra.dnum.apogee.ws.server.stub.config;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;

@ConfigurationProperties(prefix = "dataset")
public class DatasetConfigurationProperties {
    private static final Log log = LogFactory.getLog(DatasetConfigurationProperties.class);

    @NotNull
    private Resource[] files;

    public void setDirectory(Path directory) throws IOException {
        URI resolved = directory.resolve("**/*.yml").toUri();
        setFiles(new PathMatchingResourcePatternResolver().getResources(resolved.toString()));
    }

    public void setFiles(Resource[] files) {
        log.info("Using dataset from " + Arrays.toString(files));
        this.files = files;
    }

    public Resource[] resources(ResourcePatternResolver resolver) throws IOException {
        if (files == null) throw new IllegalStateException("No dataset.files nor dataset.directory specified");

        //PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //return resolver.getResources(files);
        return files;
    }

}
