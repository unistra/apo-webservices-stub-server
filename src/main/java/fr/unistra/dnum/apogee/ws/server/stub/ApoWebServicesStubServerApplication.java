package fr.unistra.dnum.apogee.ws.server.stub;

import fr.unistra.dnum.apogee.ws.export.cli.ExportDataSetCLI;
import fr.unistra.dnum.apogee.ws.server.stub.config.DatasetConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = DatasetConfigurationProperties.class)
public class ApoWebServicesStubServerApplication {

    public static void main(String[] args) {
        if (hasExportCommand(args))
            ExportDataSetCLI.main(args);
        else
            SpringApplication.run(ApoWebServicesStubServerApplication.class,args);
    }

    private static boolean hasExportCommand(String... args) {
        for (String arg : args)
            if (arg.equals("export"))
                return true;
            else if (!arg.startsWith("--"))
                break;
        return false;
    }

}
