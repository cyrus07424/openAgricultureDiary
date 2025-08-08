package models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import play.data.validation.Constraints;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * User entity managed by Ebean
 */
@Entity
@Table(name = "app_user")
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

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expires")
    private LocalDateTime resetTokenExpires;

    @Column(name = "is_admin")
    private Boolean isAdmin = false;

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

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpires() {
        return resetTokenExpires;
    }

    public void setResetTokenExpires(LocalDateTime resetTokenExpires) {
        this.resetTokenExpires = resetTokenExpires;
    }

    /**
     * Generate a password reset token that expires in 24 hours
     */
    public void generateResetToken() {
        this.resetToken = UUID.randomUUID().toString();
        this.resetTokenExpires = LocalDateTime.now().plusHours(24);
    }

    /**
     * Clear the reset token
     */
    public void clearResetToken() {
        this.resetToken = null;
        this.resetTokenExpires = null;
    }

    /**
     * Check if the reset token is valid and not expired
     */
    public boolean isResetTokenValid(String token) {
        return this.resetToken != null && 
               this.resetToken.equals(token) && 
               this.resetTokenExpires != null && 
               this.resetTokenExpires.isAfter(LocalDateTime.now());
    }

    public Boolean getIsAdmin() {
        return isAdmin != null ? isAdmin : false;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return getIsAdmin();
    }
}