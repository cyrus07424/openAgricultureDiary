package validators;

import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;
import services.PasswordStrengthService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for password strength using nbvcxz
 */
public class PasswordStrengthValidator extends Validator<String> implements ConstraintValidator<PasswordStrength, String> {
    
    private final PasswordStrengthService passwordStrengthService;
    
    @Inject
    public PasswordStrengthValidator(PasswordStrengthService passwordStrengthService) {
        this.passwordStrengthService = passwordStrengthService;
    }
    
    @Override
    public boolean isValid(String password) {
        if (password == null || password.trim().isEmpty()) {
            return true; // Let @Required handle null/empty validation
        }
        
        return passwordStrengthService.isPasswordStrong(password);
    }
    
    @Override
    public Tuple<String, Object[]> getErrorMessageKey() {
        return new Tuple<>("error.password.weak", new Object[]{});
    }
    
    @Override
    public void initialize(PasswordStrength constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return isValid(password);
    }
}