package models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import play.data.validation.Constraints;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

/**
 * User entity managed by Ebean
 */
@Entity
public class User extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @Constraints.MaxLength(255)
    private String username;

    @Constraints.Required
    @Constraints.Email
    @Constraints.MaxLength(255)
    private String email;

    @Constraints.Required
    @Constraints.MinLength(6)
    private String password;

    @OneToMany(mappedBy = "user")
    private List<Crop> crops;

    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.setPassword(password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    public List<Crop> getCrops() {
        return crops;
    }

    public void setCrops(List<Crop> crops) {
        this.crops = crops;
    }
}