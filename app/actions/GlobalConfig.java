package actions;

import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that makes GoogleTagManager and LegalLinksConfiguration globally available
 * in controllers and views without explicit injection or passing.
 */
@With(GlobalConfigAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalConfig {
}