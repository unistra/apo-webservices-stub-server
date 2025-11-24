package fr.unistra.dnum.apogee.ws.server.stub.domaine;

import gouv.education.apogee.commun.client.ws.AdministratifMetier.*;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.EtudiantCritereListeDTO;
import gouv.education.apogee.commun.client.ws.utils.DateAdapter;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static org.springframework.util.StringUtils.hasText;

public class EtudiantPredicateBuilder {
    private final List<Predicate<Person>> and = new LinkedList<>();

    private EtudiantPredicateBuilder() { /*_*/ }

    public static EtudiantPredicateBuilder builder() {
        return new EtudiantPredicateBuilder();
    }

    public EtudiantPredicateBuilder matchCodEtu(String codEtu) {
        if (hasText(codEtu)) return matchCodEtu(Integer.parseInt(codEtu));
        else return this;
    }

    public EtudiantPredicateBuilder matchCodEtu(int codEtu) {
        and.add(etu -> codEtu == etu.infoAdm().getNumEtu());
        return this;
    }

    public EtudiantPredicateBuilder matchCodInd(String codInd) {
        if (hasText(codInd)) return matchCodInd(Integer.parseInt(codInd));
        else return this;
    }

    public EtudiantPredicateBuilder matchCodInd(int codInd) {
        and.add(etu -> codInd == etu.codInd());
        return this;
    }

    public EtudiantPredicateBuilder matchNumINE(String numINE) {
        if (hasText(numINE)) and.add(etu -> numINE.equals(etu.infoAdm().getNumeroINE()));
        return this;
    }

    public EtudiantPredicateBuilder matchNumBoursier(String numBoursier) {
        if (hasText(numBoursier)) and.add(etu -> numBoursier.equals(etu.infoAdm().getNumBoursier()));
        return this;
    }

    public EtudiantPredicateBuilder matchNom(String nom) {
        if (hasText(nom))
            and.add(etu -> nom.equals(etu.infoAdm().getNomPatronymique()) || nom.equals(etu.infoAdm().getNomUsuel()));
        return this;
    }

    public EtudiantPredicateBuilder matchPrenom(String prenom) {
        if (hasText(prenom)) and.add(etu -> prenom.equals(etu.infoAdm().getPrenom1()));
        return this;
    }

    public EtudiantPredicateBuilder matchDateNaiss(String dateNaiss) {
        if (hasText(dateNaiss)) {
            LocalDate expected = DateAdapter.parseDate(dateNaiss);
            and.add(
                    etu -> etu.infoAdm().getDateNaissance() != null
                            && expected.equals(etu.infoAdm().getDateNaissance().toLocalDate())
            );
        }
        return this;
    }

    public EtudiantPredicateBuilder matchTemoinRecupAnnu(String temoinRecupAnnu) {
        if (!hasText(temoinRecupAnnu)) return this;
        switch (temoinRecupAnnu) { // valeurs possible: TOUS N O
            case "TOUS":
                break;
            case "N":
                break;
            case "O":
                break;
            default:
                throw new IllegalArgumentException("Invalid value for temoinRecupAnnu: " + temoinRecupAnnu);
        }
        return this;
    }

    public EtudiantPredicateBuilder matchCodOPI(String codOPI) {
        if (hasText(codOPI)) {
            and.clear();
            and.add(etu -> false);
        }
        return this;
    }

    private <P extends Predicate<Person>> P computeIfAbsent(Class<P> predicateType) {
        return computeIfAbsent(predicateType, () -> newInstance(predicateType));
    }

    private <P> P newInstance(Class<P> predicateType) {
        try {
            return predicateType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                 | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private <P extends Predicate<Person>> P computeIfAbsent(Class<P> predicateType, Supplier<P> supplier) {
        return and.stream()
                .filter(predicateType::isInstance)
                .findAny()
                .map(predicateType::cast)
                .orElseGet(() -> {
                    P predicate = supplier.get();
                    and.add(predicate);
                    return predicate;
                });
    }

    public EtudiantPredicateBuilder matchAnnee(String... annees) {
        if (annees.length == 0) return this;
        AnneesMatcher anneesMatcher = computeIfAbsent(AnneesMatcher.class);
        for (String annee : annees)
            anneesMatcher.add(annee);
        return this;
    }

    private static class AnneesMatcher implements Predicate<Person> {
        private final Set<String> annees = new TreeSet<>();

        @Override
        public boolean test(Person person) {
            return concat(
                    stream(person.etapes()).map(InsAdmEtpDTO3::getAnneeIAE),
                    stream(person.ias()).map(InsAdmAnuDTO2::getAnneeIAA)
                ).anyMatch(annees::contains);
        }

        public void add(String annee) {
            annees.add(annee);
        }
    }

    public EtudiantPredicateBuilder matchDiplome(Iterable<EtudiantCritereListeDTO> diplomes) {
        if (diplomes == null || !diplomes.iterator().hasNext())
            return this;
        for (EtudiantCritereListeDTO critere : diplomes)
            for (String version : critere.getListVersion())
                matchDiplome(critere.getCode(), version);
        return this;
    }

    public EtudiantPredicateBuilder matchDiplome(String code, String version) {
        computeIfAbsent(DiplomesMatcher.class).add(code,version);
        return this;
    }

    private record CodeVersionDiplome(String code, String version)
            implements Predicate<DiplomeDTO> {
        public boolean test(DiplomeDTO diplome) {
            return code.equals(diplome.getCodeDiplome())
                    && version.equals(diplome.getVersionDiplome());
        }
    }

    private static class DiplomesMatcher implements Predicate<Person> {
        private final Set<CodeVersionDiplome> diplomes = new HashSet<>();

        @Override
        public boolean test(Person person) {
            return stream(person.etapes())
                    .map(InsAdmEtpDTO3::getDiplome)
                    .filter(Objects::nonNull)
                    .anyMatch(this::contains);
        }

        boolean contains(DiplomeDTO diplome) {
            return diplomes.stream()
                        .anyMatch(d -> d.test(diplome));
        }

        public void add(String code, String version) {
            diplomes.add(new CodeVersionDiplome(code,version));
        }
    }

    public EtudiantPredicateBuilder matchEtape(Iterable<EtudiantCritereListeDTO> etapes) {
        if (etapes == null || !etapes.iterator().hasNext())
            return this;
        for (EtudiantCritereListeDTO critere : etapes)
            for (String version : critere.getListVersion())
                matchEtape(critere.getCode(), version);
        return this;
    }

    public EtudiantPredicateBuilder matchEtape(String code, String version) {
        computeIfAbsent(EtapesMatcher.class).add(code,version);
        return this;
    }

    private record CodeVersionEtape(String code, String version)
            implements Predicate<InsAdmEtpDTO3> {
        public boolean test(InsAdmEtpDTO3 insEtape) {
            EtapeDTO etape = insEtape.getEtape();
            return etape != null
                    && code.equals(etape.getCodeEtp())
                    && version.equals(etape.getVersionEtp());
        }
    }

    private static class EtapesMatcher implements Predicate<Person> {
        private final Set<CodeVersionEtape> diplomes = new HashSet<>();

        @Override
        public boolean test(Person person) {
            return stream(person.etapes())
                    .anyMatch(this::contains);
        }

        boolean contains(InsAdmEtpDTO3 etape) {
            return diplomes.stream()
                    .anyMatch(m -> m.test(etape));
        }

        public void add(String code, String version) {
            diplomes.add(new CodeVersionEtape(code,version));
        }

    }

    public EtudiantPredicateBuilder matchComposante(Collection<String> composantes) {
        if (composantes == null || composantes.isEmpty())
            return this;
        else
            return matchComposante(composantes.toArray(String[]::new));
    }

    public EtudiantPredicateBuilder matchComposante(String... composantes) {
        if (composantes.length == 0) return this;
        ComposantesMatcher composantesMatcher = computeIfAbsent(ComposantesMatcher.class);
        for (String composante : composantes)
            composantesMatcher.add(composante);
        return this;
    }

    private static class ComposantesMatcher implements Predicate<Person> {
        private final Set<String> composantes = new TreeSet<>();

        @Override
        public boolean test(Person person) {
            return stream(person.etapes())
                    .map(InsAdmEtpDTO3::getComposante)
                    .filter(Objects::nonNull)
                    .map(ComposanteDTO::getCodComposante)
                    .anyMatch(composantes::contains);
        }

        public void add(String composante) {
            composantes.add(composante);
        }
    }

    public Predicate<Person> build() {
        return and.stream().reduce(Person::isEtudiant,Predicate::and);
    }

}
