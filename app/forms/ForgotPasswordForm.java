package forms;

import play.data.validation.Constraints;

public class ForgotPasswordForm {
    
    @Constraints.Required
    @Constraints.Email
    @Constraints.MaxLength(255)
    private String email;
    
    public ForgotPasswordForm() {
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}