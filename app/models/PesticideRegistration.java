package models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import play.data.validation.Constraints;

/**
 * Pesticide registration entity managed by Ebean
 */
@Entity
@Table(name = "pesticide_registration")
public class PesticideRegistration extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @Constraints.MaxLength(255)
    @Column(name = "registration_number")
    private String registrationNumber;

    @Constraints.MaxLength(255)
    @Column(name = "usage")
    private String usage;

    @Constraints.MaxLength(255)
    @Column(name = "pesticide_type")
    private String pesticideType;

    @Constraints.MaxLength(255)
    @Column(name = "pesticide_name")
    private String pesticideName;

    @Constraints.MaxLength(255)
    @Column(name = "abbreviation")
    private String abbreviation;

    @Constraints.MaxLength(255)
    @Column(name = "crop_name")
    private String cropName;

    @Constraints.MaxLength(255)
    @Column(name = "application_location")
    private String applicationLocation;

    @Constraints.MaxLength(500)
    @Column(name = "target_pest_disease")
    private String targetPestDisease;

    @Constraints.MaxLength(255)
    @Column(name = "purpose")
    private String purpose;

    @Constraints.MaxLength(500)
    @Column(name = "dilution_amount")
    private String dilutionAmount;

    public PesticideRegistration() {
    }

    public PesticideRegistration(String registrationNumber, String usage, String pesticideType, 
                               String pesticideName, String abbreviation, String cropName,
                               String applicationLocation, String targetPestDisease, 
                               String purpose, String dilutionAmount) {
        this.registrationNumber = registrationNumber;
        this.usage = usage;
        this.pesticideType = pesticideType;
        this.pesticideName = pesticideName;
        this.abbreviation = abbreviation;
        this.cropName = cropName;
        this.applicationLocation = applicationLocation;
        this.targetPestDisease = targetPestDisease;
        this.purpose = purpose;
        this.dilutionAmount = dilutionAmount;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getPesticideType() {
        return pesticideType;
    }

    public void setPesticideType(String pesticideType) {
        this.pesticideType = pesticideType;
    }

    public String getPesticideName() {
        return pesticideName;
    }

    public void setPesticideName(String pesticideName) {
        this.pesticideName = pesticideName;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getApplicationLocation() {
        return applicationLocation;
    }

    public void setApplicationLocation(String applicationLocation) {
        this.applicationLocation = applicationLocation;
    }

    public String getTargetPestDisease() {
        return targetPestDisease;
    }

    public void setTargetPestDisease(String targetPestDisease) {
        this.targetPestDisease = targetPestDisease;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDilutionAmount() {
        return dilutionAmount;
    }

    public void setDilutionAmount(String dilutionAmount) {
        this.dilutionAmount = dilutionAmount;
    }
}