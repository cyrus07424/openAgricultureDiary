package models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import play.data.validation.Constraints;

/**
 * Field (圃場) entity managed by Ebean
 */
@Entity 
public class Field extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    private String name;
    
    // Coordinates for rectangular area selection
    @Constraints.Required
    private Double northEastLatitude;
    
    @Constraints.Required
    private Double northEastLongitude;
    
    @Constraints.Required
    private Double southWestLatitude;
    
    @Constraints.Required
    private Double southWestLongitude;

    @ManyToOne
    private User user;

    public void update(Field newFieldData) {
        setName(newFieldData.getName());
        setNorthEastLatitude(newFieldData.getNorthEastLatitude());
        setNorthEastLongitude(newFieldData.getNorthEastLongitude());
        setSouthWestLatitude(newFieldData.getSouthWestLatitude());
        setSouthWestLongitude(newFieldData.getSouthWestLongitude());
        setUser(newFieldData.getUser());
        update();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getNorthEastLatitude() {
        return northEastLatitude;
    }

    public void setNorthEastLatitude(Double northEastLatitude) {
        this.northEastLatitude = northEastLatitude;
    }

    public Double getNorthEastLongitude() {
        return northEastLongitude;
    }

    public void setNorthEastLongitude(Double northEastLongitude) {
        this.northEastLongitude = northEastLongitude;
    }

    public Double getSouthWestLatitude() {
        return southWestLatitude;
    }

    public void setSouthWestLatitude(Double southWestLatitude) {
        this.southWestLatitude = southWestLatitude;
    }

    public Double getSouthWestLongitude() {
        return southWestLongitude;
    }

    public void setSouthWestLongitude(Double southWestLongitude) {
        this.southWestLongitude = southWestLongitude;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}