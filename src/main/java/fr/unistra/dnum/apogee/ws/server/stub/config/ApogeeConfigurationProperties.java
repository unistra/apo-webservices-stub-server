package fr.unistra.dnum.apogee.ws.server.stub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.apogee")
public class ApogeeConfigurationProperties {
    private String universityCode;
    private String universityLibelle;

    public String getUniversityCode() {
        return universityCode;
    }

    public void setUniversityCode(String universityCode) {
        this.universityCode = universityCode;
    }

    public String getUniversityLibelle() {
        return universityLibelle;
    }

    public void setUniversityLibelle(String universityLibelle) {
        this.universityLibelle = universityLibelle;
    }
}
