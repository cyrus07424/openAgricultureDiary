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

    // Additional fields requested by user
    @Constraints.MaxLength(255)
    @Column(name = "spray_volume")
    private String sprayVolume;

    @Constraints.MaxLength(255)
    @Column(name = "usage_time")
    private String usageTime;

    @Constraints.MaxLength(255)
    @Column(name = "main_agent_usage_count")
    private String mainAgentUsageCount;

    @Constraints.MaxLength(255)
    @Column(name = "usage_method")
    private String usageMethod;

    @Constraints.MaxLength(255)
    @Column(name = "fumigation_time")
    private String fumigationTime;

    @Constraints.MaxLength(255)
    @Column(name = "fumigation_temperature")
    private String fumigationTemperature;

    @Constraints.MaxLength(255)
    @Column(name = "applicable_soil")
    private String applicableSoil;

    @Constraints.MaxLength(255)
    @Column(name = "applicable_zone_name")
    private String applicableZoneName;

    @Constraints.MaxLength(255)
    @Column(name = "applicable_pesticide_name")
    private String applicablePesticideName;

    @Constraints.MaxLength(255)
    @Column(name = "mixture_count")
    private String mixtureCount;

    @Constraints.MaxLength(255)
    @Column(name = "active_ingredient_1_total_usage")
    private String activeIngredient1TotalUsage;

    @Constraints.MaxLength(255)
    @Column(name = "active_ingredient_2_total_usage")
    private String activeIngredient2TotalUsage;

    @Constraints.MaxLength(255)
    @Column(name = "active_ingredient_3_total_usage")
    private String activeIngredient3TotalUsage;

    @Constraints.MaxLength(255)
    @Column(name = "active_ingredient_4_total_usage")
    private String activeIngredient4TotalUsage;

    @Constraints.MaxLength(255)
    @Column(name = "active_ingredient_5_total_usage")
    private String activeIngredient5TotalUsage;

    public PesticideRegistration() {
    }

    public PesticideRegistration(String registrationNumber, String usage, String pesticideType, 
                               String pesticideName, String abbreviation, String cropName,
                               String applicationLocation, String targetPestDisease, 
                               String purpose, String dilutionAmount,
                               String sprayVolume, String usageTime, String mainAgentUsageCount,
                               String usageMethod, String fumigationTime, String fumigationTemperature,
                               String applicableSoil, String applicableZoneName, String applicablePesticideName,
                               String mixtureCount, String activeIngredient1TotalUsage, String activeIngredient2TotalUsage,
                               String activeIngredient3TotalUsage, String activeIngredient4TotalUsage, String activeIngredient5TotalUsage) {
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
        this.sprayVolume = sprayVolume;
        this.usageTime = usageTime;
        this.mainAgentUsageCount = mainAgentUsageCount;
        this.usageMethod = usageMethod;
        this.fumigationTime = fumigationTime;
        this.fumigationTemperature = fumigationTemperature;
        this.applicableSoil = applicableSoil;
        this.applicableZoneName = applicableZoneName;
        this.applicablePesticideName = applicablePesticideName;
        this.mixtureCount = mixtureCount;
        this.activeIngredient1TotalUsage = activeIngredient1TotalUsage;
        this.activeIngredient2TotalUsage = activeIngredient2TotalUsage;
        this.activeIngredient3TotalUsage = activeIngredient3TotalUsage;
        this.activeIngredient4TotalUsage = activeIngredient4TotalUsage;
        this.activeIngredient5TotalUsage = activeIngredient5TotalUsage;
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

    public String getSprayVolume() {
        return sprayVolume;
    }

    public void setSprayVolume(String sprayVolume) {
        this.sprayVolume = sprayVolume;
    }

    public String getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(String usageTime) {
        this.usageTime = usageTime;
    }

    public String getMainAgentUsageCount() {
        return mainAgentUsageCount;
    }

    public void setMainAgentUsageCount(String mainAgentUsageCount) {
        this.mainAgentUsageCount = mainAgentUsageCount;
    }

    public String getUsageMethod() {
        return usageMethod;
    }

    public void setUsageMethod(String usageMethod) {
        this.usageMethod = usageMethod;
    }

    public String getFumigationTime() {
        return fumigationTime;
    }

    public void setFumigationTime(String fumigationTime) {
        this.fumigationTime = fumigationTime;
    }

    public String getFumigationTemperature() {
        return fumigationTemperature;
    }

    public void setFumigationTemperature(String fumigationTemperature) {
        this.fumigationTemperature = fumigationTemperature;
    }

    public String getApplicableSoil() {
        return applicableSoil;
    }

    public void setApplicableSoil(String applicableSoil) {
        this.applicableSoil = applicableSoil;
    }

    public String getApplicableZoneName() {
        return applicableZoneName;
    }

    public void setApplicableZoneName(String applicableZoneName) {
        this.applicableZoneName = applicableZoneName;
    }

    public String getApplicablePesticideName() {
        return applicablePesticideName;
    }

    public void setApplicablePesticideName(String applicablePesticideName) {
        this.applicablePesticideName = applicablePesticideName;
    }

    public String getMixtureCount() {
        return mixtureCount;
    }

    public void setMixtureCount(String mixtureCount) {
        this.mixtureCount = mixtureCount;
    }

    public String getActiveIngredient1TotalUsage() {
        return activeIngredient1TotalUsage;
    }

    public void setActiveIngredient1TotalUsage(String activeIngredient1TotalUsage) {
        this.activeIngredient1TotalUsage = activeIngredient1TotalUsage;
    }

    public String getActiveIngredient2TotalUsage() {
        return activeIngredient2TotalUsage;
    }

    public void setActiveIngredient2TotalUsage(String activeIngredient2TotalUsage) {
        this.activeIngredient2TotalUsage = activeIngredient2TotalUsage;
    }

    public String getActiveIngredient3TotalUsage() {
        return activeIngredient3TotalUsage;
    }

    public void setActiveIngredient3TotalUsage(String activeIngredient3TotalUsage) {
        this.activeIngredient3TotalUsage = activeIngredient3TotalUsage;
    }

    public String getActiveIngredient4TotalUsage() {
        return activeIngredient4TotalUsage;
    }

    public void setActiveIngredient4TotalUsage(String activeIngredient4TotalUsage) {
        this.activeIngredient4TotalUsage = activeIngredient4TotalUsage;
    }

    public String getActiveIngredient5TotalUsage() {
        return activeIngredient5TotalUsage;
    }

    public void setActiveIngredient5TotalUsage(String activeIngredient5TotalUsage) {
        this.activeIngredient5TotalUsage = activeIngredient5TotalUsage;
    }
}