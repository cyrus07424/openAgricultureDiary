package forms;

import play.data.validation.Constraints;

public class ResetPasswordForm {
    
    @Constraints.Required
    private String token;
    
    @Constraints.Required
    @Constraints.MinLength(6)
    private String password;
    
    @Constraints.Required
    private String confirmPassword;
    
    public ResetPasswordForm() {
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
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