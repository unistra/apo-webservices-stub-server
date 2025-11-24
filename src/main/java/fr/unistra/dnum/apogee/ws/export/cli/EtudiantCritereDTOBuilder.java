package fr.unistra.dnum.apogee.ws.export.cli;

import gouv.education.apogee.commun.client.ws.EtudiantMetier.EtudiantCritereDTO;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.EtudiantCritereListeDTO;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.TableauDiplomes;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.TableauEtapes;
import gouv.education.apogee.commun.client.ws.OffreFormationMetier.*;
import org.springframework.stereotype.Component;

import java.util.*;

import static fr.unistra.dnum.apogee.ws.export.cli.EtudiantMetierClientConfig.tryRethrow;

@Component
public class EtudiantCritereDTOBuilder {
    public static final String AUCUN = "aucun";
    public static final String TOUS = "tous";
    private final OffreFormationMetierServiceInterface offreFormationService;

    public EtudiantCritereDTOBuilder(OffreFormationMetierServiceInterface offreFormationService) {
        this.offreFormationService = offreFormationService;
    }

    public EtudiantCritereDTO toFilter(String query) {
        return this.new Builder().toFilter(query.split("&")).build();
    }
//
//    List<DiplomeDTO4> getDiplomes() {
//        if (diplomes != null)
//            return diplomes;
//        SECritereDTO2 param = new SECritereDTO2();
//
//        // Retrait du filtre sur l'annee pour permettre de rattacher les codes etape des
//        // annees autres que celle en cours
//        param.setTemOuvertRecrutement("O");
//        param.setCodEtp("tous");
//        param.setCodVrsVet("tous");
//        param.setCodDip("aucun");
//        param.setCodVrsVdi("aucun");
//        param.setCodElp("aucun");
//        diplomes = tryRethrow(() -> offreFormationService.recupererSEV4(param));
//        return diplomes;
//    }

    private List<DiplomeDTO4> getDiplomes(String codeEtape, Collection<String> versionsDEtape) {
        if (versionsDEtape == null || versionsDEtape.isEmpty())
            versionsDEtape = List.of(TOUS);

        List<DiplomeDTO4> diplomes = new LinkedList<>();
        for (String versionDEtape : versionsDEtape) {
            SECritereDTO2 param = new SECritereDTO2();
            param.setTemOuvertRecrutement("O"); // ???
            param.setCodEtp(codeEtape);
            param.setCodVrsVet(versionDEtape);
//            param.setCodDip(AUCUN);
//            param.setCodVrsVdi(AUCUN);
            param.setCodDip(TOUS);
            param.setCodVrsVdi(TOUS);
            param.setCodElp(TOUS);
            diplomes.addAll(tryRethrow(() -> offreFormationService.recupererSEV4(param)));
        }
        return diplomes;
    }

    private class Builder {
        private String annee = null;
        private final Map<String,Set<String>> diplomes = new TreeMap<>();
        private final Map<String,Set<String>> etapes = new TreeMap<>();
        private final Set<String> centreGestion = new TreeSet<>();
        private final Set<String> composante = new TreeSet<>();

//        private final Set<EtudiantCritereListeDTO> diplomes = new TreeSet<>(Comparator.comparing(EtudiantCritereListeDTO::getCode));
//        private final Set<EtudiantCritereListeDTO> etapes = new TreeSet<>(Comparator.comparing(EtudiantCritereListeDTO::getCode));

        public Builder annee(String annee) {
            this.annee = annee;
            return this;
        }

        public Builder diplome(String codeEtVersions) {
            List<String> liste = List.of(codeEtVersions.split(","));
            return diplome(liste.getFirst(), liste.subList(1, liste.size()));
        }
        public Builder diplome(String codeDiplome, String... versionsDiplome) {
            return diplome(codeDiplome, List.of(versionsDiplome));
        }
        public Builder diplome(String codeDiplome, List<String> versionsDiplome) {
            diplomes.computeIfAbsent(codeDiplome, k -> new TreeSet<>()).addAll(versionsDiplome);
            return this;
        }

        public Builder etape(String codeEtVersions) {
            List<String> liste = List.of(codeEtVersions.split(","));
            return etape(liste.getFirst(), liste.subList(1, liste.size()));
        }
        public Builder etape(String codeEtape, String... versionsEtape) {
            return etape(codeEtape, List.of(versionsEtape));
        }
        public Builder etape(String codeEtape, List<String> versionsEtape) {
            etapes.computeIfAbsent(codeEtape, k -> new TreeSet<>()).addAll(versionsEtape);
            return this;
        }

        public Builder toFilter(String... queries) {
            for (String critere : queries) {
                String[] keyValue = critere.split("=",2);
                String key = keyValue[0];
                if (keyValue.length < 2)
                    throw new IllegalArgumentException("valeur requise pour "+key+"=");
                String value = keyValue[1];
                switch (key) {
                    case "annee" -> annee(value);
                    case "diplome" -> diplome(value);
                    case "etape" -> etape(value);
                    case "centre", "centreGestion" -> centreGestion.add(value);
                    case "composante" -> composante.add(value);
                    default -> throw new IllegalArgumentException("filtre "+key+" inconnu");
                }
            }

            return this;
        }

        public EtudiantCritereDTO build() {
//            if (filtre.getListDiplomes().getItem().isEmpty()) {
//                EtudiantCritereListeDTO critere = new EtudiantCritereListeDTO();
//                critere.setCode(AUCUN);
//                filtre.getListDiplomes().getItem().add(critere);
//            }

            etapes.forEach((codeEtape, versionsEtape) -> {
                for (DiplomeDTO4 diplome : getDiplomes(codeEtape, versionsEtape))
                    if (diplome.getCodDip() != null && diplome.getListVersionDiplome() != null)
                        for (VersionDiplomeDTO4 v : diplome.getListVersionDiplome().getItem())
                            diplome(diplome.getCodDip(),v.getCodVrsVdi().toString());
            });

            EtudiantCritereDTO filtre = new EtudiantCritereDTO();
            filtre.setListDiplomes(new TableauDiplomes());
            filtre.getListDiplomes().getItem();
            filtre.setListEtapes(new TableauEtapes());
            filtre.getListEtapes().getItem();

            filtre.setAnnee(annee);
            setCodeEtVersions(filtre.getListDiplomes().getItem(), diplomes);
            setCodeEtVersions(filtre.getListEtapes().getItem(), etapes);
            filtre.getListCentreGestion().addAll(centreGestion);
            filtre.getListComposante().addAll(composante);

            return filtre;
        }

        private void setCodeEtVersions(List<EtudiantCritereListeDTO> items, Map<String, Set<String>> codeEtVersions) {
            for (String codeDiplome : codeEtVersions.keySet()) {
                EtudiantCritereListeDTO critereListeDTO = new EtudiantCritereListeDTO();
                critereListeDTO.setCode(codeDiplome);
                critereListeDTO.getListVersion().addAll(codeEtVersions.get(codeDiplome));
                items.add(critereListeDTO);
            }
        }

        private static void add(List<EtudiantCritereListeDTO> items, String codeVersions) {
            List<String> cAndV = List.of(codeVersions.split(","));
            String code = cAndV.getFirst();
            if (cAndV.size() < 2)
                throw new IllegalArgumentException("version(s) requise pour " + code);
            add(items, code, cAndV.subList(1, cAndV.size()));
        }

        private static void add(List<EtudiantCritereListeDTO> items, String code, List<String> versions) {
            for (EtudiantCritereListeDTO item : items)
                if (code.equals(item.getCode())) {
                    item.getListVersion().addAll(versions);
                    return;
                }
            EtudiantCritereListeDTO item = new EtudiantCritereListeDTO();
            item.setCode(code);
            item.getListVersion().addAll(versions);
            items.add(item);
        }

    }

}
