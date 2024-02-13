/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.kernel;

import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.cache.service.ThreadCacheAccessor;
import org.silverpeas.kernel.exception.ExpectationViolationException;
import org.silverpeas.kernel.exception.MultipleCandidateException;
import org.silverpeas.kernel.exception.NotFoundException;
import org.silverpeas.kernel.util.Mutable;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A provider of objects whose the life-cycle is managed by an underlying IoC by IoD container that
 * must satisfy the {@link BeanContainer} interface. This provider is just an access point to the
 * beans for which their class has been eligible for life-cycle management and registered for this.
 * The election and the registration of the classes in the bean container are left to the
 * implementation of the IoC by IoD solution.
 * <p>
 * Usually, the dependencies between beans managed by the container are resolved during their
 * instantiation and this mechanism is usually done transparently by the IoC/IoD solution itself.
 * Nevertheless, some IoC by IoD solutions resolves them at compilation time instead of at runtime.
 * And some supports a lazy dependency resolution whereas others not. So, the resolution of the
 * dependencies on other managed objects is expected to be automatically performed by the container.
 * Nevertheless, some objects non managed by the container (like objects got from data source) can
 * require to access some of the beans in the container. This provider is for them; it allows them
 * to get the expected bean in the container.
 * </p>
 * <p>
 * For managed beans, according to the IoC system used, their dependency resolution should be left
 * to the underlying container because it is less expensive in time and CPU than using this provider
 * to access the dependencies. This is why this provider should be used only by unmanaged beans.
 * Anyway, to improve lightly the performance, the thread local cache is used to store single bean
 * of singleton (or singleton alike), avoiding then to invoke explicitly the underlying bean
 * container. It is when such a single instance isn't found in the cache the container is implied to
 * get it. The goal to use a thread cache (instead of an application one) is to balance the print in
 * memory and the fastest way to get such managed single beans; the performance is chosen in getting
 * the beans of singleton when asked several times along the same processing thread.
 * </p>
 *
 * @author mmoquillon
 * @implNote The {@link BeanContainer} interface is a wrapper of the underlying IoC subsystem
 * (Inversion of Control) used to manage the life-cycle of the some objects as well as the
 * resolution of their dependencies. (The IoC subsystem is built upon an IoD (Injection of
 * Dependencies) mechanism.) The bind between the {@link BeanContainer} interface and its
 * implementation is performed by the Java SPI (Java Service Provider Interface). Only the first
 * available bean container implementation is loaded by the {@code ManagedBeanProvider} class.
 * <p>
 * This managed bean provider manages a thread cache to access more fastly managed objects of
 * singletons. With this mechanism, several asks to the underlying bean container for a given single
 * bean can be avoided along the thread execution; the bean is fetched only one time within the
 * thread. For doing, to figure out a bean is the single instance of a singleton, this provider
 * walks across the annotations tree of the bean until to find or not the @{@link Singleton}
 * annotation. This is the default behavior. Nevertheless, it is possible to ask for the same
 * behavior with other {@link javax.inject.Scope} annotations. In this case, the side-effect of
 * caching a non singleton's bean is of the responsibility of the project. To extend the caching to
 * other {@link javax.inject.Scope} annotation, just implement the interface SingletonFinder
 * </p>
 */
public class ManagedBeanProvider {

  private static final ManagedBeanProvider instance = new ManagedBeanProvider();

  static final String CACHE_KEY_PREFIX = "ManagedBeanProvider:CacheKey:";
  private final ThreadCacheAccessor cacheAccessor = ThreadCacheAccessor.getInstance();
  private final BeanContainer currentContainer;
  private final SingletonDetector detector = new SingletonDetector();

  /**
   * Gets the single instance of this {@link ManagedBeanProvider} class.
   *
   * @return the {@link ManagedBeanProvider} single instance.
   */
  public static synchronized ManagedBeanProvider getInstance() {
    return instance;
  }

  private ManagedBeanProvider() {
    currentContainer = ServiceLoader.load(BeanContainer.class)
        .findFirst()
        .orElseThrow(() -> new SilverpeasRuntimeException(
            "No IoC container detected! At least one bean container should be available!"));
  }

  /**
   * Gets an instance of the single implementation matching the specified type and satisfying the
   * given qualifiers if any. The bean is first looked for in the cache of the current thead before
   * asking it to the bean container. If the implementation is a singleton and it's not yet cached,
   * then the bean is put in the cache of the current thread for further retrieval.
   *
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return the bean satisfying the expected type and, if any, the expected qualifiers.
   * @throws NotFoundException if no bean of the specified type and with the specified qualifiers
   * can be found.
   * @throws MultipleCandidateException if there is more than one bean matching the given type and
   * qualifiers as there is an ambiguous decision in selecting the bean to return.
   * @throws ExpectationViolationException if the expectations of the underlying container on the
   * bean or on the qualifiers aren't fulfilled.
   * @throws IllegalStateException if the underlying container isn't in a valid state when asking a
   * managed bean.
   * @see BeanContainer#getBeanByType(Class, Annotation...)
   */
  @NonNull
  public <T> T getManagedBean(Class<T> type, Annotation... qualifiers) {
    Mutable<T> bean = Mutable.empty();
    T cachedBean = cacheAccessor.getCache().computeIfAbsent(cacheKey(type, qualifiers), type,
        () -> {
          T foundBean = beanContainer()
              .getBeanByType(type, qualifiers)
              .orElseThrow(() -> {
                String q = qualifiers.length > 0 ? " and qualifiers " +
                    Stream.of(qualifiers)
                        .map(a -> a.getClass().getName())
                        .collect(Collectors.joining(", ")) : "";
                return new NotFoundException("No such bean satisfying type " + type.getName() + q);
              });
          bean.set(foundBean);
          return isSingleton(foundBean) ? foundBean : null;
        });
    return cachedBean == null ? bean.get() : cachedBean;
  }

  /**
   * Gets an object of the single type implementation that is qualified with the specified name. The
   * bean is first looked for in the cache of the current thead before asking it to the bean
   * container. If the implementation is a singleton and it's not yet cached, then the bean is put
   * in the cache of the current thread for further retrieval.
   *
   * @param name the unique name identifying the bean in the container.
   * @param <T> the type of the bean to return.
   * @return the bean matching the specified name.
   * @throws NotFoundException if no bean can be found with the specified name.
   * @throws MultipleCandidateException if there is more than one bean with the given name as the
   * name must be unique for each bean.
   * @throws ExpectationViolationException if the expectations of the underlying container on the
   * bean aren't fulfilled.
   * @throws IllegalStateException if the underlying container isn't in a valid state when asking a
   * managed bean.
   * @see BeanContainer#getBeanByName(String)
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public <T> T getManagedBean(String name) {
    Mutable<T> bean = Mutable.empty();
    T cachedBean = (T) cacheAccessor.getCache().computeIfAbsent(CACHE_KEY_PREFIX + name,
        Object.class, () -> {
          T foundBean = beanContainer()
              .getBeanByName(name)
              .map(b -> (T) b)
              .orElseThrow(() -> new NotFoundException("No such bean named " + name));
          bean.set(foundBean);
          return isSingleton(foundBean) ? foundBean : null;
        });
    return cachedBean == null ? bean.get() : cachedBean;
  }

  /**
   * Gets an instance of each implementations of the specified type and satisfying the given
   * qualifiers if any.
   *
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return a set of beans satisfying the expected type and, if any, the expected qualifiers, or an
   * empty set otherwise.
   * @throws ExpectationViolationException if the expectations of the underlying container on the
   * beans or on the qualifiers aren't fulfilled.
   * @throws IllegalStateException if the underlying container isn't in a valid state when asking a
   * managed bean.
   * @see BeanContainer#getAllBeansByType(Class, Annotation...)
   */
  public <T> Set<T> getAllManagedBeans(Class<T> type, Annotation... qualifiers) {
    return beanContainer().getAllBeansByType(type, qualifiers);
  }

  protected BeanContainer beanContainer() {
    return currentContainer;
  }

  private static <T> String cacheKey(Class<T> type, Annotation[] qualifiers) {
    final StringBuilder cacheKey = new StringBuilder(CACHE_KEY_PREFIX + type.getName());
    for (final Annotation qualifier : qualifiers) {
      cacheKey.append(":").append(qualifier.annotationType().getName());
    }
    return cacheKey.toString();
  }

  private boolean isSingleton(Object bean) {
    return detector.test(bean.getClass());
  }

  /**
   * A predicate to detect a given class is annotated directly or by transitivity (through another
   * annotation) with the {@link Singleton} annotation. It is used  by the
   * {@link ManagedBeanProvider} to figure out whether a bean obtained from the bean container is
   * eligible to the caching.
   */
  private static class SingletonDetector implements Predicate<Class<?>> {

    /**
     * Walks recursively across all the annotations of the specified class to find if one is a
     * {@link Singleton} scope. Indeed, the class of a managed bean can be indirectly annotated with
     * such an annotation by one of its other annotation (and this recursively). The walk across the
     * annotations tree is only performed for the Silverpeas annotations. Nevertheless, we expect to
     * find this annotation (if any) no more than one level of the annotations annotated
     * themselves.
     *
     * @param aClass the type of a managed bean.
     * @return true if the bean class is either directly annotated with the {@link Singleton} scope
     * or if one of its Silverpeas custom annotations is itself recursively annotated with this
     * annotation.
     */
    @Override
    public boolean test(Class<?> aClass) {
      if (aClass.isAnnotationPresent(Singleton.class)) {
        return true;
      }
      return isOneAnnotatedSingleton(aClass.getAnnotations());
    }

    private boolean isSingletonAnnotated(Class<? extends Annotation> annotationType) {
      if (annotationType.isAnnotationPresent(Singleton.class)) {
        return true;
      }
      if (!annotationType.getName().startsWith("org.silverpeas")) {
        return false;
      }
      return isOneAnnotatedSingleton(annotationType.getAnnotations());
    }

    private boolean isOneAnnotatedSingleton(Annotation[] annotations) {
      for (Annotation annotation : annotations) {
        if (isSingletonAnnotated(annotation.annotationType())) {
          return true;
        }
      }
      return false;
    }
  }
}
