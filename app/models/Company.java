package models;

import jakarta.persistence.Entity;
import play.data.validation.Constraints;



/**
 * Company entity managed by Ebean
 */
@Entity 
public class Company extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

