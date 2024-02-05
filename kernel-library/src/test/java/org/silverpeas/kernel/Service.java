package org.silverpeas.kernel;

import org.silverpeas.kernel.annotation.Managed;

import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to qualify a bean as being a functional service to be managed by the IoC container.
 * Such a functional service must be a singleton.
 * @author mmoquillon
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Managed
@Scope
@Singleton
public @interface Service {
}
