/*
 * Copyright (C) 2000 - 2023 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.kernel.test;

import org.silverpeas.kernel.ManagedBeanProvider;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.test.util.Reflections;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

/**
 * A resolver of dependencies on managed beans in a given object itself managed by the underlying IoC container. This
 * class can be extended to provide a custom dependency resolution for specific IoD system like Spring DI or CDI. By
 * default, it provides a simple resolution mechanism in which when a dependency isn't found in the
 * {@link org.silverpeas.kernel.BeanContainer} then it is mocked. The new implementation will be loaded by the Java SPI
 * when a dependency resolver will be demanded.
 *
 * @author mmoquillon
 */
public class DependencyResolver {

  private static DependencyResolver instance;

  /**
   * Gets an instance of a dependency resolver. If at least one custom dependency resolver is found by the Java SPI, the
   * first one will be got and returned.
   *
   * @return a {@link DependencyResolver} object.
   */
  public static synchronized DependencyResolver get() {
    if (instance == null) {
      instance = ServiceLoader.load(DependencyResolver.class)
          .findFirst()
          .orElseGet(DependencyResolver::new);
    }
    return instance;
  }

  private final ManagedBeanProvider beanProvider = ManagedBeanProvider.getInstance();

  /**
   * Resolves the dependencies on others managed bean of the specified object. The dependencies on others managed beans
   * are figured out by the annotation {@link Inject} applied on the fields of the bean. The resolution is performed as
   * follow: for each field annotated with {@link Inject}:
   * <ul>
   *   <li>invoke the {@link DependencyResolver#resolveCustomDependency(Field, Annotation...)} method for custom
   *   dependency resolution.</li>
   *   <li>if no bean is found by the custom dependency resolver, then invoke the
   *   {@link DependencyResolver#resolveDependency(Field, Annotation...)} default method.</li>
   * </ul>
   *
   * @param bean the bean for which the dependencies on others managed bean have to be resolved.
   */
  public final void resolve(final Object bean) {
    Reflections.loopInheritance(bean.getClass(), typeToLookup -> {
      Field[] beanFields = typeToLookup.getDeclaredFields();
      Stream.of(beanFields)
          .filter(f -> f.isAnnotationPresent(Inject.class))
          .forEach(f -> {
            Annotation[] qualifiers = getQualifiers(f);
            Optional<Object> resolved = resolveCustomDependency(f, qualifiers);
            Object dependency = resolved.orElseGet(() -> resolveDependency(f, qualifiers));
            setDependency(bean, f, dependency);
          });
    });
  }

  private static void setDependency(Object bean, Field f, Object dependency) {
    f.trySetAccessible();
    try {
      f.set(bean, dependency);
    } catch (IllegalAccessException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Applies a custom dependency resolution mechanism. Does nothing by default.
   * @param dependency the dependency to resolve. The field shouldn't be valued by the method.
   * @param qualifiers a set of qualifiers the managed bean to look for has to satisfy.
   * @return optionally a bean responding successfully to the expectations. By default, nothing.
   */
  protected Optional<Object> resolveCustomDependency(@NonNull Field dependency, @NonNull Annotation... qualifiers) {
    Objects.requireNonNull(dependency);
    Objects.requireNonNull(qualifiers);
    return Optional.empty();
  }

  /**
   * Applies the default dependency resolution mechanism. It looks for a bean in the IoC container that satisfies the
   * type of the field and the specified qualifiers on the field. If no such a bean is found, then the field is mocked.
   * @param dependency the dependency to resolve. The field shouldn't be valued by the method.
   * @param qualifiers a set of qualifiers the managed bean to look for has to satisfy.
   * @return either the managed bean that satisfies the expectations or a mock.
   */
  protected Object resolveDependency(@NonNull Field dependency, @NonNull Annotation... qualifiers) {
    Objects.requireNonNull(dependency);
    Objects.requireNonNull(qualifiers);
    Class<?> dependencyType = dependency.getType();
    try {
      Object bean;
      if (dependencyType.isAnnotationPresent(Singleton.class)) {
        bean = beanProvider.getSingleInstance(dependencyType, qualifiers);
      } else {
        bean = beanProvider.getManagedBean(dependencyType, qualifiers);
      }
      return bean;
    } catch (IllegalStateException e) {
      return mock(dependencyType);
    }
  }

  private Annotation[] getQualifiers(Field dependency) {
    return Reflections.getDeclaredAnnotations(dependency, Qualifier.class);
  }

}
