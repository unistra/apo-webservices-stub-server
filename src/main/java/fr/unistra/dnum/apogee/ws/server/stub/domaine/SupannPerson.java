package fr.unistra.dnum.apogee.ws.server.stub.domaine;

import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmEtpDTO3;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.CoordonneesDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.InfoAdmEtuDTO4;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.List;

import static fr.unistra.dnum.apogee.ws.server.stub.domaine.DomainUtils.*;
import static fr.unistra.dnum.apogee.ws.server.stub.domaine.DomainUtils.isEmpty;

@Entry(objectClasses = {
        "Person",
        "eduPerson",
        "inetOrgPerson",
        "supannPerson",
})
public class SupannPerson {

    @Id
    private Name dn;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/uid">Supann : uid</a> */
    @DnAttribute(value="uid", index=0)
    private String uid;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannAliasLogin">Supann : supannAliasLogin</a> */
    private String supannAliasLogin;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEtuId">Supann : supannEtuId</a> */
    private String supannEtuId;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/sn">Supann : sn</a> */
    private List<String> sn;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/mail">Supann : mail</a> */
    private String mail;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannAutreMail">Supann : supannAutreMail</a> */
    private String supannAutreMail;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/cn">Supann : cn</a> */
    private String cn;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/givenName">Supann : givenName</a> */
    private List<String> givenName;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/displayName">Supann : displayName</a> */
    private String displayName;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/eduPersonPrimaryAffiliation">Supann : eduPersonPrimaryAffiliation</a> */
    private String eduPersonPrimaryAffiliation;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/eduPersonAffiliation">Supann : eduPersonAffiliation</a> */
    private List<String> eduPersonAffiliation;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEntiteAffectation">Supann : supannEntiteAffectation</a> */
    private List<String> supannEntiteAffectation;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEntiteAffectationPrincipale">Supann : supannEntiteAffectationPrincipale</a> */
    private String supannEntiteAffectationPrincipale;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannCivilite">Supann : supannCivilite</a> */
    private String supannCivilite;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/telephoneNumber">Supann : telephoneNumber</a> */
    private String telephoneNumber;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEtuCursusAnnee">Supann : supannEtuCursusAnnee</a> */
    private List<String> supannEtuCursusAnnee;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/eduPersonOrgDN">Supann : eduPersonOrgDN</a> */
    private String eduPersonOrgDN;

    /** @see <a href="https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEtuEtape">Supann : supannEtuEtape</a> */
    private List<String> supannEtuEtape;


    private String supannEmpId;

    private String supannRefId;

    private String supannEtuAnneeInscription;

    public SupannPerson merge(CoordonneesDTO2 coordonnee) {
        if (coordonnee == null) return this;

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/uid
        if (isEmpty(this.getUid()))
            this.setUid(coordonnee.getLoginAnnuaire());

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/mail
        if (isEmpty(this.getMail()))
            this.setMail(coordonnee.getEmailAnnuaire());

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannAutreMail
        if (isEmpty(this.getSupannAutreMail()))
            this.setSupannAutreMail(coordonnee.getEmail());

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/telephoneNumber
        if (isEmpty(this.getTelephoneNumber()))
            this.setTelephoneNumber(coordonnee.getNumTelPortable());

        return this;
    }

    public SupannPerson merge(InfoAdmEtuDTO4 infoAdm) {
        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEtuId
        if (isEmpty(this.getSupannEtuId()))
            this.setSupannEtuId(infoAdm.getNumEtu().toString());

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/sn
        if (isEmpty(this.getSn()))
            this.setSn(toList(
                    infoAdm.getNomUsuel() != null ? infoAdm.getNomUsuel() : infoAdm.getNomPatronymique()
            ));

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/cn
        if (isEmpty(this.getCn()))
            this.setCn(unaccent(String.join(" ", toList(
                    infoAdm.getNomUsuel() != null ? infoAdm.getNomUsuel() : infoAdm.getNomPatronymique(),
                    infoAdm.getPrenom1()
            ))));

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/givenName
        if (isEmpty(this.getGivenName()))
            this.setGivenName(toList(
                    infoAdm.getPrenom1(),
                    infoAdm.getPrenom2(),
                    infoAdm.getPrenom3()
            ));

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/displayName
        if (isEmpty(this.getDisplayName()))
            this.setDisplayName(String.join(" ",toList(
                    infoAdm.getPrenom1(),
                    infoAdm.getNomUsuel() != null ? infoAdm.getNomUsuel() : infoAdm.getNomPatronymique()
            )));

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/eduPersonPrimaryAffiliation
        if (isEmpty(this.getEduPersonPrimaryAffiliation()))
            this.setEduPersonPrimaryAffiliation("student");

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/eduPersonAffiliation
        if (isEmpty(this.getEduPersonAffiliation()))
            this.setEduPersonAffiliation(List.of(this.getEduPersonPrimaryAffiliation(),"member"));

        /*
        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEntiteAffectationPrincipale
        if (!hasText(this.getSupannEntiteAffectationPrincipale()))
            this.setSupannEntiteAffectationPrincipale();

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannCivilite
        if (!hasText(this.getSupannCivilite()))
            this.setSupannCivilite(infoAdm.getSexe());

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEtuCursusAnnee
        if (!hasText(this.getSupannEtuCursusAnnee()))
            this.setSupannEtuCursusAnnee();

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/eduPersonOrgDN
        if (!hasText(this.getEduPersonOrgDN()))
            this.setEduPersonOrgDN();

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEtuEtape
        if (!hasText(this.getSupannEtuEtape()))
            this.setSupannEtuEtape();


        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEmpId
        if (!hasText(this.getSupannEmpId()))
            this.setSupannEmpId();

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannRefId
        if (!hasText(this.getSupannRefId()))
            this.setSupannRefId();

        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEtuAnneeInscription
        if (!hasText(this.getSupannEtuAnneeInscription()))
            this.setSupannEtuAnneeInscription();
        */

        return this;
    }

    public SupannPerson merge(InsAdmEtpDTO3... etapes) {
        /*
        // https://services.renater.fr/documentation/supann/courant/recommandations/attributs/supannEntiteAffectation
        if (!hasText(this.getSupannEntiteAffectation()))
            this.setSupannEntiteAffectation();

        */
        return this;
    }

    public void defaults() {
        if (isEmpty(this.getUid())) {
            if (!isEmpty(this.getDisplayName()))
                this.setUid(toUid(this.getDisplayName()));
            else if (!isEmpty(this.getCn()))
                this.setUid(toUid(this.getCn()));
            else if (!isEmpty(this.getSupannEtuId()))
                this.setUid(this.getSupannEtuId());
        }
    }

    private String toUid(String uid) {
        return uid.replaceAll("[^a-zA-Z0-9_-]", ".").toLowerCase();
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    /** @return {@link #displayName} */
    public String getDisplayName() {
        return displayName;
    }

    /** @param {@link #displayName} */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Name getDn() {
        return dn;
    }

    public void setDn(Name dn) {
        this.dn = dn;
    }

    public List<String> getEduPersonAffiliation() {
        return eduPersonAffiliation;
    }

    public void setEduPersonAffiliation(List<String> eduPersonAffiliation) {
        this.eduPersonAffiliation = eduPersonAffiliation;
    }

    public String getEduPersonOrgDN() {
        return eduPersonOrgDN;
    }

    public void setEduPersonOrgDN(String eduPersonOrgDN) {
        this.eduPersonOrgDN = eduPersonOrgDN;
    }

    public String getEduPersonPrimaryAffiliation() {
        return eduPersonPrimaryAffiliation;
    }

    public void setEduPersonPrimaryAffiliation(String eduPersonPrimaryAffiliation) {
        this.eduPersonPrimaryAffiliation = eduPersonPrimaryAffiliation;
    }

    public List<String> getGivenName() {
        return givenName;
    }

    public void setGivenName(String... givenName) {
        setGivenName(toList(givenName));
    }
    public void setGivenName(List<String> givenName) {
        this.givenName = givenName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public List<String> getSn() {
        return sn;
    }

    public void setSn(List<String> sn) {
        this.sn = sn;
    }

    public String getSupannAliasLogin() {
        return supannAliasLogin;
    }

    public void setSupannAliasLogin(String supannAliasLogin) {
        this.supannAliasLogin = supannAliasLogin;
    }

    public String getSupannAutreMail() {
        return supannAutreMail;
    }

    public void setSupannAutreMail(String supannAutreMail) {
        this.supannAutreMail = supannAutreMail;
    }

    public String getSupannCivilite() {
        return supannCivilite;
    }

    public void setSupannCivilite(String supannCivilite) {
        this.supannCivilite = supannCivilite;
    }

    public String getSupannEmpId() {
        return supannEmpId;
    }

    public void setSupannEmpId(String supannEmpId) {
        this.supannEmpId = supannEmpId;
    }

    public List<String> getSupannEntiteAffectation() {
        return supannEntiteAffectation;
    }

    public void setSupannEntiteAffectation(List<String> supannEntiteAffectation) {
        this.supannEntiteAffectation = supannEntiteAffectation;
    }

    public String getSupannEntiteAffectationPrincipale() {
        return supannEntiteAffectationPrincipale;
    }

    public void setSupannEntiteAffectationPrincipale(String supannEntiteAffectationPrincipale) {
        this.supannEntiteAffectationPrincipale = supannEntiteAffectationPrincipale;
    }

    public String getSupannEtuAnneeInscription() {
        return supannEtuAnneeInscription;
    }

    public void setSupannEtuAnneeInscription(String supannEtuAnneeInscription) {
        this.supannEtuAnneeInscription = supannEtuAnneeInscription;
    }

    public List<String> getSupannEtuCursusAnnee() {
        return supannEtuCursusAnnee;
    }

    public void setSupannEtuCursusAnnee(List<String> supannEtuCursusAnnee) {
        this.supannEtuCursusAnnee = supannEtuCursusAnnee;
    }

    public List<String> getSupannEtuEtape() {
        return supannEtuEtape;
    }

    public void setSupannEtuEtape(List<String> supannEtuEtape) {
        this.supannEtuEtape = supannEtuEtape;
    }

    public String getSupannEtuId() {
        return supannEtuId;
    }

    public void setSupannEtuId(String supannEtuId) {
        this.supannEtuId = supannEtuId;
    }

    public String getSupannRefId() {
        return supannRefId;
    }

    public void setSupannRefId(String supannRefId) {
        this.supannRefId = supannRefId;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
