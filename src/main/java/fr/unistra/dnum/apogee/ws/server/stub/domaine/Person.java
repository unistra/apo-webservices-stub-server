package fr.unistra.dnum.apogee.ws.server.stub.domaine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.*;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.*;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.PaysDTO;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static fr.unistra.dnum.apogee.ws.server.stub.domaine.Constantes.*;
import static fr.unistra.dnum.apogee.ws.server.stub.domaine.DomainUtils.*;

public record Person(
        int codInd,
        @Nonnull SupannPerson ldap,
        @Nullable InfoAdmEtuDTO4 infoAdm,
        @Nonnull CoordonneesDTO2[] coordonnees,
        @Nonnull InsAdmAnuDTO2[] ias,
        @Nonnull InsAdmEtpDTO3[] etapes
) {
    private static final PersonMapper MAPPER = Mappers.getMapper(PersonMapper.class);

    public static PersonBuilder builder() {
        return new PersonBuilder();
    }

    public static SupannPerson defaultLdap(InfoAdmEtuDTO4 infoAdm, CoordonneesDTO2[] coordonnees, InsAdmEtpDTO3[] etapes) {
        SupannPerson ldap = new SupannPerson();
        last(CoordonneesDTO2::getAnnee, coordonnees)
                .ifPresent(ldap::merge);
        if (infoAdm != null) ldap
                .merge(infoAdm)
                .merge(lasts(InsAdmEtpDTO3::getAnneeIAE, etapes));
        ldap.defaults();
        return ldap;
    }

    public boolean isEtudiant() {
        return infoAdm != null;
    }

    @JsonIgnore
    public IdentifiantsEtudiantDTO2 getIdentifiants() {
        return MAPPER.toIdentifiants(this);
    }
    @JsonIgnore
    public EtudiantDTO2 getEtudiant() {
        return MAPPER.toEtudiant(this);
    }

    @JsonIgnore
    public CoordonneesDTO2 getCoordonnee() {
        return last(CoordonneesDTO2::getAnnee,coordonnees).orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Person person) && codInd() == person.codInd();
    }

    @Override
    public int hashCode() {
        return codInd() > -1 ? Objects.hashCode(codInd()) : Objects.hashCode(ldap().getUid());
    }

    @Override
    public String toString() {
        return ldap().getUid() + (isEtudiant() ? "("+infoAdm.getNumEtu()+")" : "");
    }

    @Mapper
    interface PersonMapper {

        @Mapping(target = "codEtu", source = "infoAdm.numEtu")
        @Mapping(target = "codInd", source = "codInd")
        @Mapping(target = "numeroINE", source = "infoAdm.numeroINE")
        @Mapping(target = "loginAnnuaire", source = "coordonnee.loginAnnuaire")
        @Mapping(target = "emailAnnuaire", source = "coordonnee.emailAnnuaire")
        @Mapping(target = "numBoursier", source = "infoAdm.numBoursier")
        IdentifiantsEtudiantDTO2 toIdentifiants(Person person);

        @Mapping(target = "codEtu", source = "infoAdm.numEtu")
        @Mapping(target = "nom", source = "infoAdm.nomPatronymique")
        @Mapping(target = "prenom", source = "infoAdm.prenom1")
        @Mapping(target = "dateNaissance", source = "infoAdm.dateNaissance")
        @Mapping(target = "numeroIne", source = "infoAdm.numeroINE")
        EtudiantDTO2 toEtudiant(Person person);

    }

    public static class PersonBuilder {
        private int codInd = -1;
        private SupannPerson ldap = null;
        private InfoAdmEtuDTO4 infoAdm = null;
        private CoordonneesDTO2[] coordonnees = new CoordonneesDTO2[0];
        private InsAdmAnuDTO2[] ias = new InsAdmAnuDTO2[0];
        private InsAdmEtpDTO3[] etapes = new InsAdmEtpDTO3[0];

        public PersonBuilder codInd(int codInd) {
            this.codInd = codInd;
            return this;
        }

        public PersonBuilder ldap(SupannPerson ldap) {
            this.ldap = ldap;
            return this;
        }

        public PersonBuilder infoAdm(InfoAdmEtuDTO4 infoAdm) {
            this.infoAdm = infoAdm;
            return this;
        }

        public PersonBuilder ias(List<InsAdmAnuDTO2> ias) {
            return ias(ias.toArray(InsAdmAnuDTO2[]::new));
        }
        public PersonBuilder ias(InsAdmAnuDTO2... ias) {
            this.ias = ias;
            return this;
        }

        public PersonBuilder coordonnees(List<CoordonneesDTO2> coordonnees) {
            return coordonnees(coordonnees.toArray(CoordonneesDTO2[]::new));
        }
        public PersonBuilder coordonnees(CoordonneesDTO2... coordonnees) {
            this.coordonnees = coordonnees;
            return this;
        }

        public PersonBuilder etapes(List<InsAdmEtpDTO3> etapes) {
            return etapes(etapes.toArray(InsAdmEtpDTO3[]::new));
        }
        public PersonBuilder etapes(InsAdmEtpDTO3... etapes) {
            this.etapes = etapes;
            return this;
        }

        public Person build() {
            defaults();
            validate();
            return new Person(codInd, ldap, infoAdm, coordonnees, ias, etapes);
        }

        private void validate() {
            // sn obligatoire (requis par LDAP Person et ESUP-Stage)
            Objects.requireNonNull(ldap.getSn(), "ldap.sn est obligatoire");

            // givenName obligatoire (requis par ESUP-SiScol)
            // c.f. https://github.com/EsupPortail/esup-siscol/blob/2.1.2/src/main/java/org/esupportail/referentiel/ldap/services/PersonServiceMapperMethod.java#L238
            Objects.requireNonNull(ldap.getGivenName(), "ldap.givenName est obligatoire");

            for (InsAdmEtpDTO3 etape : etapes) {
                Objects.requireNonNull(etape.getEtape(), "etape.etape est obligatoire");
                Objects.requireNonNull(etape.getComposante(), "etape.composante est obligatoire");
            }

        }

        private void defaults() {
            if (ldap == null)
                ldap = defaultLdap(infoAdm, coordonnees, etapes);
            noNullNumEtuAndCodInd();
            for (CoordonneesDTO2 coordonnee : coordonnees)
                defaults(coordonnee);
            for (InsAdmAnuDTO2 iaa : ias)
                defaults(iaa);
            for (InsAdmEtpDTO3 etape : etapes)
                defaults(etape);
        }

        private static void defaults(InsAdmAnuDTO2 ia) {
            if (ia.getDateIAA() == null)
                ia.setDateIAA(LocalDateTime.now());
            if (ia.getAnneeIAA() == null)
                ia.setAnneeIAA(anneeU(ia.getDateIAA()));
            if (ia.getEtatIaa() == null)
                ia.setEtatIaa(IAA_EN_COURS);
        }

        private static void defaults(InsAdmEtpDTO3 etape) {
            if (etape.getDateIAE() == null)
                etape.setDateIAE(LocalDateTime.now());
            if (etape.getAnneeIAE() == null)
                etape.setAnneeIAE(anneeU(etape.getDateIAE()));
            if (etape.getEtatIaa() == null)
                etape.setEtatIaa(IAA_EN_COURS);
            if (etape.getEtatIae() == null)
                etape.setEtatIae(IAE_EN_COURS);
            if (etape.getCodeInscriptionPayee() == null)
                etape.setCodeInscriptionPayee(INSCRIPTION_PAYEE);
            if (etape.getComposante() == null)
                etape.setComposante(new ComposanteDTO());
            if (etape.getCge() == null)
                etape.setCge(new CgeDTO());
            if (etape.getProfilEtudiant() == null)
                etape.setProfilEtudiant(PROFIL_NORMAL);
        }

        private void noNullNumEtuAndCodInd() {
            if (infoAdm != null) {
                if (codInd == -1)
                    codInd = Objects.requireNonNull(infoAdm.getNumEtu(), "infoAdm.numEtu est obligatoire");
                else if (infoAdm.getNumEtu() == null)
                    infoAdm.setNumEtu(codInd);
            } else if (codInd == -1)
                codInd = -Objects.hashCode(ldap.getUid());
        }

        private static void defaults(CoordonneesDTO2 coordonnee) {
            if (coordonnee.getAnnee() == null)
                coordonnee.setAnnee(anneeU(LocalDateTime.now()));
            coordonnee.setAdresseFixe(requireNotNull(coordonnee.getAdresseFixe()));
            coordonnee.setAdresseAnnuelle(requireNotNull(coordonnee.getAdresseAnnuelle()));
        }

        private static AdresseDTO2 requireNotNull(AdresseDTO2 adresse) {
            if (adresse == null) adresse = new AdresseDTO2();
            if (adresse.getPays() == null) adresse.setPays(new PaysDTO());
            if (adresse.getCommune() == null) adresse.setCommune(new CommuneDTO2());
            return adresse;
        }

        public static String anneeU(LocalDateTime date) {
            return date.getMonth().getValue() < 7
                    ? Integer.toString(date.getYear() - 1)
                    : Integer.toString(date.getYear());
        }
    }
}
