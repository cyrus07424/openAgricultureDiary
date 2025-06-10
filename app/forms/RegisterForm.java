package forms;

import play.data.validation.Constraints;

public class RegisterForm {
    
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
    
    @Constraints.Required
    private String confirmPassword;
    
    public RegisterForm() {
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
        this.password = password;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
}