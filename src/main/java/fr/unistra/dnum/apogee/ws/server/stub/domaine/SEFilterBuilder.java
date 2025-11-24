package fr.unistra.dnum.apogee.ws.server.stub.domaine;

import gouv.education.apogee.commun.client.ws.OffreFormationMetier.*;

import java.util.LinkedList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

public class SEFilterBuilder {
    public static final SEFilter TRUE = new SEFilter() {};
    private final List<SEFilter> p = new LinkedList<>();
    private static final String TOUS = "tous";
    private static final String AUCUN = "aucun";

    public SEFilterBuilder codAnu(String codAnu) {
        if (!hasText(codAnu)) return this;
        // ignore
        return this;
    }

    public SEFilterBuilder codComposanteVdi(String codComposanteVdi) {
        if (!hasText(codComposanteVdi)) return this;
        p.add(new SEFilter() {
            @Override
            public boolean test(ComposanteOrganisatriceDTO composante) {
                return codComposanteVdi.equals(composante.getCodComposante());
            }
        });
        return this;
    }

    public SEFilterBuilder codDip(String codDip) {
        if (!hasText(codDip) || TOUS.equals(codDip) || AUCUN.equals(codDip)) return this;
        p.add(new SEFilter() {
            @Override
            public boolean test(DiplomeDTO4 diplome) {
                return codDip.equals(diplome.getCodDip());
            }
        });
        return this;
    }

    public SEFilterBuilder codElp(String codElp) {
        if (!hasText(codElp) || TOUS.equals(codElp) || AUCUN.equals(codElp)) return this;
        throw new UnsupportedOperationException("not supported yet.");
    }

    public SEFilterBuilder codEtp(String codEtp) {
        if (!hasText(codEtp) || TOUS.equals(codEtp) || AUCUN.equals(codEtp)) return this;
        p.add(new SEFilter() {
            @Override
            public boolean test(EtapeDTO4 etape) {
                return codEtp.equals(etape.getCodEtp());
            }
        });
        return this;
    }

    public SEFilterBuilder codNatureDip(String codNatureDip) {
        if (!hasText(codNatureDip)) return this;
        throw new UnsupportedOperationException("not supported yet.");
    }

    public SEFilterBuilder codNatureElp(String codNatureElp) {
        if (!hasText(codNatureElp)) return this;
        throw new UnsupportedOperationException("not supported yet.");
    }

    public SEFilterBuilder codTypDip(String codTypDip) {
        if (!hasText(codTypDip)) return this;
        throw new UnsupportedOperationException("not supported yet.");
    }

    public SEFilterBuilder codVrsVdi(String codVrsVdi) {
        if (!hasText(codVrsVdi) ||TOUS.equals(codVrsVdi) || AUCUN.equals(codVrsVdi)) return this;
        Integer vrsVdi = Integer.parseInt(codVrsVdi);
        p.add(new SEFilter() {
            @Override
            public boolean test(VersionDiplomeDTO4 versionDiplome) {
                return vrsVdi.equals(versionDiplome.getCodVrsVdi());
            }
        });
        return this;
    }

    public SEFilterBuilder codVrsVet(String codVrsVet) {
        if (!hasText(codVrsVet) || TOUS.equals(codVrsVet) || AUCUN.equals(codVrsVet)) return this;
        Integer vrsVet = Integer.parseInt(codVrsVet);
        p.add(new SEFilter() {
            @Override
            public boolean test(VersionEtapeDTO42 versionEtape) {
                return vrsVet.equals(versionEtape.getCodVrsVet());
            }
        });
        return this;
    }

    public SEFilterBuilder temOuvertRecrutement(String temOuvertRecrutement) {
        if (!hasText(temOuvertRecrutement)) return this;
        p.add(new SEFilter() {
            @Override
            public boolean test(EtapeDTO4 etape) {
                return temOuvertRecrutement.equals(etape.getTemOuvertRecrutement());
            }
        });
        return this;
    }

    public SEFilter build() {
        return p.stream()
                .reduce(SEFilter::and)
                .orElse(TRUE);
    }

}
