package models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import play.data.format.Formats;
import play.data.validation.Constraints;

import java.util.Date;

/**
 * Crop entity managed by Ebean
 */
@Entity 
public class Crop extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    private String name;
    
    @Formats.DateTime(pattern="yyyy-MM-dd")
    private Date introduced;
    
    @Formats.DateTime(pattern="yyyy-MM-dd")
    private Date discontinued;
    
    @ManyToOne
    private Company company;

    public void update(Crop newCropData) {
        setName(newCropData.getName());
        setCompany(newCropData.getCompany());
        setDiscontinued(newCropData.getDiscontinued());
        setIntroduced(newCropData.getIntroduced());
        update();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getIntroduced() {
        return introduced;
    }

    public void setIntroduced(Date introduced) {
        this.introduced = introduced;
    }

    public Date getDiscontinued() {
        return discontinued;
    }

    public void setDiscontinued(Date discontinued) {
        this.discontinued = discontinued;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}

