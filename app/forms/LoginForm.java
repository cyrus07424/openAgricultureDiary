package forms;

import play.data.validation.Constraints;

public class LoginForm {
    
    @Constraints.Required
    @Constraints.Email
    private String email;
    
    @Constraints.Required
    private String password;
    
    public LoginForm() {
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}