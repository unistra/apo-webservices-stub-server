package fr.unistra.dnum.apogee.ws.export.cli;

import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;
import fr.unistra.dnum.apogee.ws.server.stub.infrastructure.yaml.YamlConfiguration;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.AdministratifMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmAnuDTO2;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmEtpDTO3;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.WebBaseException_Exception;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.CoordonneesDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.EtudiantDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.EtudiantMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.InfoAdmEtuDTO4;
import gouv.education.apogee.commun.client.ws.OffreFormationMetier.OffreFormationMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.ReferentielMetier.ReferentielMetierServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static fr.unistra.dnum.apogee.ws.export.cli.DebugWebServiceCallInterceptor.debugWSCall;
import static java.util.Optional.ofNullable;

// exemple:
// annee=2025&diplome=FPG2G,320&etape=FPA151,320&etape=FPB251,322&etape=FPB951,336&etape=FPBA52,339
// annee=2025&etape=FPA151,320&etape=FPB251,322&etape=FPB951,336&etape=FPBA52,339

@SpringBootApplication
@Import({
        YamlConfiguration.class,
})
public class ExportDataSetCLI implements ApplicationRunner {
    private final Logger log = LoggerFactory.getLogger(ExportDataSetCLI.class);

    private final EtudiantMetierServiceInterface etudiantService;
    private final AdministratifMetierServiceInterface administratifService;
    private final Yaml yaml;
    private final EtudiantCritereDTOBuilder etudiantCritereDTOBuilder;

    public ExportDataSetCLI(
            EtudiantMetierServiceInterface etudiantService,
            AdministratifMetierServiceInterface administratifService,
            EtudiantCritereDTOBuilder etudiantCritereDTOBuilder,
            Yaml yaml) {
        this.etudiantService = debugWSCall(etudiantService);
        this.administratifService = debugWSCall(administratifService);
        this.etudiantCritereDTOBuilder = etudiantCritereDTOBuilder;
        this.yaml = yaml;
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ExportDataSetCLI.class);
        application.setAdditionalProfiles("export");
        application.run(args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path dataset = getDatasetOption(args);
        List<String> arguments = args.getNonOptionArgs();
        List<Person> personnes = new LinkedList<>();
        Set<String> seen = new TreeSet<>();
        if (arguments.isEmpty())
            throw new IllegalArgumentException("Aucun code étudiant à exporter vers "+dataset.toAbsolutePath());
        for (String argument : arguments)
            (argument.contains("=")
                    ? etudiantService.recupererListeEtudiants(etudiantCritereDTOBuilder.toFilter(argument)).stream()
                        .map(EtudiantDTO2::getCodEtu)
                    : Stream.of(argument))
                        .filter(codEtu -> !seen.contains(codEtu))
                        .map(this::findEtudiant)
                        .filter(Objects::nonNull)
                        .peek(p -> seen.add(p.getEtudiant().getCodEtu()))
                        .forEach(personnes::add);
        Path etudiantFile = dataset.resolve("export.yml");
        log.info("Exporte {} personnes vers {}", personnes.size(), etudiantFile);
        if (dataset.toFile().mkdirs())
            log.info("Répertoire {} créé", dataset);
        yaml.dump(personnes, new FileWriter(etudiantFile.toFile()));
    }


    private Person findEtudiant(String codEtu) {
        try {
            log.info("Récupère les informations pour {}", codEtu);
            InfoAdmEtuDTO4 infoAdmEtu = etudiantService.recupererInfosAdmEtuV4(codEtu);
            List<String> anneesIa = administratifService.recupererAnneesIa(codEtu, null);
            List<CoordonneesDTO2> coordonnees = new LinkedList<>();
            List<InsAdmAnuDTO2> ias = new LinkedList<>();
            List<InsAdmEtpDTO3> etapes = new LinkedList<>();
            for (String anneeIa : new TreeSet<>(anneesIa)) {
                coordonnees.add(etudiantService.recupererAdressesEtudiantV2(codEtu, anneeIa, null));
                ias.addAll(administratifService.recupererIAAnnuellesV2(codEtu, anneeIa, null));
                etapes.addAll(administratifService.recupererIAEtapesV3(codEtu, anneeIa, null, null));
            }
            return Person.builder()
                    .codInd(infoAdmEtu.getNumEtu())
                    .infoAdm(infoAdmEtu)
                    .coordonnees(coordonnees)
                    .ias(ias)
                    .etapes(etapes)
                    .build();
        } catch (Exception exception) {
            log.error("Erreur pour "+codEtu+" : "+ getMessage(exception), exception);
            return null;
        }
    }

    private static String getMessage(Exception exception) {
        return switch (exception) {
            case WebBaseException_Exception we1 -> we1.getFaultInfo().getLastErrorMsg();
            case gouv.education.apogee.commun.client.ws.EtudiantMetier.WebBaseException_Exception we2-> we2.getFaultInfo().getLastErrorMsg();
            default -> exception.getMessage();
        };
    }

    private static Path getDatasetOption(ApplicationArguments args) {
        return Path.of(
                ofNullable(args.getOptionValues("dataset"))
                        .orElseGet(List::of)
                .stream().findFirst()
                .orElse("dataset")
        );
    }

}
