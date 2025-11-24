package fr.unistra.dnum.apogee.ws.server.stub.domaine;

import gouv.education.apogee.commun.client.ws.OffreFormationMetier.*;

import java.util.List;

/** Filtre pour la structure des enseignements (Offre de Formation)
 * @see OffreFormationMetierRepository */
public abstract class SEFilter {
    public static SEFilterBuilder builder() {
        return new SEFilterBuilder();
    }

    public boolean test(DiplomeDTO4 diplome) {
        return diplome.getListVersionDiplome().getItem().stream().anyMatch(this::test);
    }

    public boolean test(VersionDiplomeDTO4 versionDiplome) {
        return versionDiplome.getOffreFormation().getListEtape().getItem().stream().anyMatch(this::test);
    }

    public boolean test(EtapeDTO4 etape) {
        return etape.getListVersionEtape().getItem().stream().anyMatch(this::test);
    }

    public boolean test(VersionEtapeDTO42 versionEtape) {
        return test(versionEtape.getComposante());
    }

    public boolean test(ComposanteOrganisatriceDTO composante) {
        return true;
    }

    public DiplomeDTO4 filter(DiplomeDTO4 diplome) {
        filterVdis(diplome.getListVersionDiplome().getItem());
        return diplome;
    }

    public void filterVdis(List<VersionDiplomeDTO4> versions) {
        versions.removeIf(vdi -> !test(vdi));
        versions.forEach(vdi -> filterEtapes(vdi.getOffreFormation().getListEtape().getItem()));
    }

    public void filterEtapes(List<EtapeDTO4> etps) {
        etps.removeIf(etp -> !test(etp));
        etps.forEach(etp -> filterVet(etp.getListVersionEtape().getItem()));
    }

    public void filterVet(List<VersionEtapeDTO42> vets) {
        vets.removeIf(vet -> !test(vet));
    }

    public static SEFilter and(SEFilter lhs, SEFilter rhs) {
        return new AndSEFilter(lhs, rhs);
    }

    public static final class AndSEFilter extends SEFilter {
        private final SEFilter lhs;
        private final SEFilter rhs;

        public AndSEFilter(SEFilter lhs, SEFilter rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public boolean test(ComposanteOrganisatriceDTO composante) {
            return lhs.test(composante) && rhs.test(composante);
        }

        @Override
        public boolean test(DiplomeDTO4 diplome) {
            return lhs.test(diplome) && rhs.test(diplome);
        }

        @Override
        public boolean test(EtapeDTO4 etape) {
            return lhs.test(etape) && rhs.test(etape);
        }

        @Override
        public boolean test(VersionDiplomeDTO4 versionDiplome) {
            return lhs.test(versionDiplome) && rhs.test(versionDiplome);
        }

        @Override
        public boolean test(VersionEtapeDTO42 versionEtape) {
            return lhs.test(versionEtape) && rhs.test(versionEtape);
        }

    }
}
