package fr.unistra.dnum.apogee.ws.server.stub.infrastructure;

import fr.unistra.dnum.apogee.ws.server.stub.config.ApogeeConfigurationProperties;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.CgeDTO;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.ComposanteDTO;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmEtpDTO3;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.RegimeInsDTO;
import gouv.education.apogee.commun.client.ws.OffreFormationMetier.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
@EnableConfigurationProperties(ApogeeConfigurationProperties.class)
public abstract class OffreFormationMapper {
    protected EtablissementHabiliteDTO etab;

    @Autowired
    public void setConfig(ApogeeConfigurationProperties config) {
        this.etab = toEtab(config);
    }

    @Mapping(target = "codEtb", source = "universityCode")
    @Mapping(target = "libEtbHab", source = "universityLibelle")
    @Mapping(target = "temEnSveEtb", constant = "O")
    abstract EtablissementHabiliteDTO toEtab(ApogeeConfigurationProperties config);

    @Mapping(target = "codDip", source = "diplome.codeDiplome")
    @Mapping(target = "libDip", source = "diplome.libLongDiplome")
    @Mapping(target = "listVersionDiplome", source = "iae")
    @Mapping(target = "temOuvertRecrutement", constant = "O")
    @Mapping(target = "temOuvertValidation", constant = "O")
    @Mapping(target = "cycle.codCycle", constant = "H")
    @Mapping(target = "cycle.libCycle", constant = "Sans objet - Hors cycle")
    @Mapping(target = "cycle.temEnSve", constant = "O")
    @Mapping(target = "etbHab", expression = "java(etab)" )
    public abstract DiplomeDTO4 toDiplome(InsAdmEtpDTO3 iae);

    TableauVersionDiplomeDTO4 toVersionsDiplome(InsAdmEtpDTO3 iae) {
        TableauVersionDiplomeDTO4 tableauVersionDiplomeDTO4 = new TableauVersionDiplomeDTO4();
        tableauVersionDiplomeDTO4.getItem().add(toVersionDiplome(iae));
        return tableauVersionDiplomeDTO4;
    }

    @Mapping(target = "codVrsVdi", source = "diplome.versionDiplome")
    @Mapping(target = "libCourtVdi", source = "diplome.libVdi")
    @Mapping(target = "libWebVdi", source = "diplome.libWebVdi")
    @Mapping(target = "temOffCom", constant = "N")
    @Mapping(target = "temOffDeloc", constant = "N")
    @Mapping(target = "offreFormation", source = "iae")
    abstract VersionDiplomeDTO4 toVersionDiplome(InsAdmEtpDTO3 iae);

    @Mapping(target = "listEtape", source = "iae")
    @Mapping(target = "temEnSveFinalite", constant = "N")
    @Mapping(target = "temEnSveMention", constant = "N")
    @Mapping(target = "temEnSveParcoursType", constant = "N")
    @Mapping(target = "temEnSveSpecialite", constant = "N")
    abstract OffreFormationDTO4 toOffreFormation(InsAdmEtpDTO3 iae);

    TableauEtapeDTO4 toEtapes(InsAdmEtpDTO3 iae) {
        TableauEtapeDTO4 etapes = new TableauEtapeDTO4();
        etapes.getItem().add(toEtape(iae));
        return etapes;
    }

    @Mapping(target = "codEtp", source = "etape.codeEtp")
    @Mapping(target = "libEtp", source = "etape.libLongEtp")
    @Mapping(target = "libCourtEtp", source = "diplome.libVdi")
    @Mapping(target = "listComposanteCentreGestion", source = "iae")
    @Mapping(target = "listVersionEtape", source = "iae")
    @Mapping(target = "temOuvertValidation", constant = "O")
    @Mapping(target = "temOuvertRecrutement", constant = "O")
    @Mapping(target = "temOuvDrtBrs", constant = "O")
    @Mapping(target = "temOuvertDrtBrs", constant = "O")
    abstract EtapeDTO4 toEtape(InsAdmEtpDTO3 iae);

    TableauComposanteCentreGestionDTO4 toCGEs(InsAdmEtpDTO3 iae) {
        TableauListeElementPedagogiDTO4 foo;
        TableauComposanteCentreGestionDTO4 cges = new TableauComposanteCentreGestionDTO4();
        if (iae.getCge() != null)
            cges.getItem().add(toCGE(iae.getCge(), iae.getComposante()));
        return cges;
    }

    @Mapping(target = "codComposante", source = "composante.codComposante")
    @Mapping(target = "libComposante", source = "composante.libComposante")
    @Mapping(target = "codCentreGestion", source = "cge.codeCGE")
    @Mapping(target = "libCentreGestion", source = "cge.libCGE")
    @Mapping(target = "temEnSveCentreGestion", constant = "O")
    @Mapping(target = "temEnSveComposante", constant = "O")
    abstract ComposanteCentreGestionDTO toCGE(CgeDTO cge, ComposanteDTO composante);

    TableauVersionEtapeDTO4 toVersionsEtape(InsAdmEtpDTO3 iae) {
        TableauVersionEtapeDTO4 vets = new TableauVersionEtapeDTO4();
        vets.getItem().add(toVersionEtape(iae));
        return vets;
    }

    @Mapping(target = "codVrsVet", source = "etape.versionEtp")
    @Mapping(target = "libWebVet", source = "etape.libWebVet")
    @Mapping(target = "composante", source = "composante")
    @Mapping(target = "listRegime", source = "regimeIns")
    @Mapping(target = "listListeElementPedagogi", expression = "java(new gouv.education.apogee.commun.client.ws.OffreFormationMetier.TableauListeElementPedagogiDTO4())")
    @Mapping(target = "temEnSveDuree", constant = "O")
    @Mapping(target = "temEnSveParcoursType", constant = "O")
    @Mapping(target = "temHebVet", constant = "O")
    @Mapping(target = "temTeleEns", constant = "O")
    abstract VersionEtapeDTO42 toVersionEtape(InsAdmEtpDTO3 iae);

    @Mapping(target = "codComposante", source = "codComposante")
    @Mapping(target = "libComposante", source = "libComposante")
    @Mapping(target = "temEnSve", constant = "O")
    abstract ComposanteOrganisatriceDTO toComposante(ComposanteDTO composante);

    TableauListeRegimeDTO toRegimes(RegimeInsDTO ri) {
        TableauListeRegimeDTO regimes = new TableauListeRegimeDTO();
        if (ri != null)
            regimes.getItem().add(toRegime(ri));
        return regimes;
    }

    @Mapping(target = "codRgi", source = "codRgi")
    @Mapping(target = "libRgi", source = "libRgi")
    @Mapping(target = "temEnSveRve", constant = "O")
    abstract RegimeDTO toRegime(RegimeInsDTO ri);

    public DiplomeDTO4 merge(DiplomeDTO4 known, DiplomeDTO4 diplome) {
        if (known == null) return diplome;

        mergeVersionsDiplome(known.getListVersionDiplome().getItem(), diplome.getListVersionDiplome().getItem());

        return known;
    }

    private void mergeVersionsDiplome(List<VersionDiplomeDTO4> knowns, List<VersionDiplomeDTO4> versions) {
        for (VersionDiplomeDTO4 version : versions) mergeVersionsDiplome(knowns, version);
    }

    private void mergeVersionsDiplome(List<VersionDiplomeDTO4> knowns, VersionDiplomeDTO4 version) {
        for (VersionDiplomeDTO4 known : knowns) {
            if (known.getCodVrsVdi().equals(version.getCodVrsVdi())) {
                mergeVersionDiplome(known, version);
                return;
            }
        }
        knowns.add(version);
    }

    private void mergeVersionDiplome(VersionDiplomeDTO4 known, VersionDiplomeDTO4 version) {
        mergeEtapes(known.getOffreFormation().getListEtape().getItem(), version.getOffreFormation().getListEtape().getItem());
    }

    private void mergeEtapes(List<EtapeDTO4> knowns, List<EtapeDTO4> etapes) {
        for (EtapeDTO4 etape : etapes)
            mergeEtapes(knowns, etape);
    }

    private void mergeEtapes(List<EtapeDTO4> knowns, EtapeDTO4 etape) {
        for (EtapeDTO4 known : knowns)
            if (known.getCodEtp().equals(etape.getCodEtp())) {
                mergeEtape(known, etape);
                return;
            }
        knowns.add(etape);
    }

    private void mergeEtape(EtapeDTO4 known, EtapeDTO4 etape) {
        mergeVets(known.getListVersionEtape().getItem(), etape.getListVersionEtape().getItem());
        mergeCGEs(known.getListComposanteCentreGestion().getItem(),etape.getListComposanteCentreGestion().getItem());
    }

    private void mergeCGEs(List<ComposanteCentreGestionDTO> knowns, List<ComposanteCentreGestionDTO> cges) {
        for (ComposanteCentreGestionDTO cge : cges)
            mergeCGEs(knowns,cge);
    }

    private void mergeCGEs(List<ComposanteCentreGestionDTO> knowns, ComposanteCentreGestionDTO cge) {
        if (cge == null || cge.getCodCentreGestion() == null) return;
        for (ComposanteCentreGestionDTO known : knowns)
            if(cge.getCodCentreGestion().equals(known.getCodCentreGestion())) {
                mergeCGE(known,cge);
                return;
            }
        knowns.add(cge);
    }

    private void mergeCGE(ComposanteCentreGestionDTO known, ComposanteCentreGestionDTO cge) {

    }

    private void mergeVets(List<VersionEtapeDTO42> knowns, List<VersionEtapeDTO42> vets) {
        for (VersionEtapeDTO42 vet : vets)
            mergeVets(knowns, vet);
    }

    private void mergeVets(List<VersionEtapeDTO42> knowns, VersionEtapeDTO42 vet) {
        for (VersionEtapeDTO42 known : knowns)
            if (known.getCodVrsVet().equals(vet.getCodVrsVet())) {
                mergeVet(known, vet);
                return;
            }
        knowns.add(vet);
    }

    private void mergeVet(VersionEtapeDTO42 known, VersionEtapeDTO42 vet) {
        mergeRIs(known.getListRegime().getItem(),vet.getListRegime().getItem());
    }

    private void mergeRIs(List<RegimeDTO> knowns, List<RegimeDTO> ris) {
        for (RegimeDTO ri : ris)
            mergeRIs(knowns,ri);
    }

    private void mergeRIs(List<RegimeDTO> knowns, RegimeDTO ri) {
        for (RegimeDTO known : knowns)
            if (known.getCodRgi().equals(ri.getCodRgi())) {
                mergeRIs(known,ri);
                return;
            }
        knowns.add(ri);
    }

    private void mergeRIs(RegimeDTO known, RegimeDTO ri) {
    }

}
