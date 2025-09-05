package validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for password strength validation
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordStrengthValidator.class)
public @interface PasswordStrength {
    
    String message() default "パスワードが弱すぎます。より安全なパスワードを設定してください";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}