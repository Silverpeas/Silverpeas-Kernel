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

import jakarta.inject.Singleton;
import org.silverpeas.kernel.annotation.Cacheable;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.cache.model.SimpleCache;
import org.silverpeas.kernel.cache.service.InMemoryCache;
import org.silverpeas.kernel.exception.ExpectationViolationException;
import org.silverpeas.kernel.exception.MultipleCandidateException;
import org.silverpeas.kernel.exception.NotFoundException;

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A provider of objects whose life-cycle is managed by an underlying IoC by IoD container that must
 * satisfy the {@link BeanContainer} interface. This provider is just an access point to the beans
 * that are eligible for automatic life-cycle management. The election of the beans is usually done
 * by a mechanism of registration of their classes in the bean container which is left to the
 * implementation of the IoC by IoD solution.
 * <p>
 * Usually, the dependencies between beans managed by the container are resolved during their
 * instantiation and this mechanism is done transparently by the IoC/IoD solution itself.
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
 * Anyway, to improve lightly the performances, a cache mechanism is used to store single beans of
 * singletons and of any classes annotated with {@link Cacheable}, avoiding then to invoke
 * explicitly the underlying bean container. It is when such an asked bean isn't found in the cache,
 * the container is involved to get it. In order to avoid excessive memory usage, the beans in the
 * cache are stored as soft references, meaning they will be collected by the garbage collector and
 * specifically before any out-of-memory situations.
 * </p>
 *
 * @author mmoquillon
 * @implNote The {@link BeanContainer} interface is a wrapper of the underlying IoC subsystem
 * (Inversion of Control) used to manage the life-cycle of some objects as well as the resolution of
 * their dependencies. (The IoC subsystem is built upon an IoD (Injection of Dependencies)
 * mechanism.) The bind between the {@link BeanContainer} interface and its implementation is
 * performed by the Java SPI (Java Service Provider Interface). Only the first available bean
 * container implementation is loaded by the {@code ManagedBeanProvider} class.
 * <p>
 * This managed bean provider manages a cache to access more quickly managed objects of singletons
 * and of classes annotated with {@link Cacheable}. With this mechanism, several asks the underlying
 * bean container for a given bean can be avoided. For doing, to figure out a bean is the single
 * eligible to be cached, this provider walks across the annotations tree of the bean until to find
 * either the @{@link Singleton} or the {@link Cacheable} annotation. This is the default behavior.
 * </p>
 */
public final class ManagedBeanProvider {

  private static final ManagedBeanProvider instance = new ManagedBeanProvider();

  static final String CACHE_KEY_PREFIX = "ManagedBeanProvider:CacheKey:";
  private final SimpleCache cache = new InMemoryCache();
  private final BeanContainer currentContainer;
  private final CacheableDetector detector = new CacheableDetector();

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
   * asking it to the bean container. If the implementation is a singleton, and it's not yet cached,
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
    return findManagedBean(cacheKey(type, qualifiers), () -> findBeanByType(type, qualifiers));
  }

  /**
   * Gets an object of the single type implementation that is qualified with the specified name. The
   * bean is first looked for in the cache of the current thead before asking it to the bean
   * container. If the implementation is a singleton, and it's not yet cached, then the bean is put
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
  @NonNull
  public <T> T getManagedBean(String name) {
    return findManagedBean(CACHE_KEY_PREFIX + name, () -> findBeanByName(name));
  }

  /**
   * Gets an instance of each implementation of the specified type and satisfying the given
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

  SimpleCache getCache() {
    return this.cache;
  }

  BeanContainer beanContainer() {
    return currentContainer;
  }

  private <T> @NonNull T findManagedBean(String cacheKey, Supplier<T> finder) {
    T bean;
    var cachedBean = Objects.requireNonNullElseGet(getCache().get(cacheKey, SoftReference.class),
        () -> new SoftReference<>(null));
    //noinspection unchecked
    bean = (T) cachedBean.get();
    if (bean == null) {
      bean = finder.get();
    }
    if (isCacheable(bean)) {
      cache.put(cacheKey, new SoftReference<>(bean));
    }
    return bean;
  }

  private <T> @NonNull T findBeanByType(Class<T> type, Annotation[] qualifiers) {
    return beanContainer()
        .getBeanByType(type, qualifiers)
        .orElseThrow(() -> {
          String q = qualifiers.length > 0 ? " and qualifiers " +
              Stream.of(qualifiers)
                  .map(a -> a.getClass().getName())
                  .collect(Collectors.joining(", ")) : "";
          return new NotFoundException("No such bean satisfying type " + type.getName() + q);
        });
  }

  @SuppressWarnings("unchecked")
  private <T> @NonNull T findBeanByName(String name) {
    return beanContainer()
        .getBeanByName(name)
        .map(b -> (T) b)
        .orElseThrow(() -> new NotFoundException("No such bean named " + name));
  }

  private static <T> String cacheKey(Class<T> type, Annotation[] qualifiers) {
    final StringBuilder cacheKey = new StringBuilder(CACHE_KEY_PREFIX + type.getName());
    for (final Annotation qualifier : qualifiers) {
      cacheKey.append(":").append(qualifier.annotationType().getName());
    }
    return cacheKey.toString();
  }

  private boolean isCacheable(Object bean) {
    return detector.test(bean.getClass());
  }

  /**
   * A predicate to detect the objects of a given class are eligible to the caching. For doing, the
   * class has to be annotated directly or by transitivity (trough another annotation) with either
   * the {@link Singleton} annotation or with the {@link org.silverpeas.kernel.annotation.Cacheable}
   * annotation. It is used  by the {@link ManagedBeanProvider} to figure out whether a bean
   * obtained from the bean container has to be cached.
   */
  private static class CacheableDetector implements Predicate<Class<?>> {

    /**
     * Walks recursively across all the annotations of the specified class to find out if one is
     * {@link Singleton} or {@link org.silverpeas.kernel.annotation.Cacheable}. Indeed, the class of
     * a managed bean can be indirectly annotated with such annotations by one of its other
     * annotation (and this recursively). The walk across the annotations tree is only performed for
     * the Silverpeas annotations. Nevertheless, we expect to find this annotation (if any) no more
     * than one level of the annotations annotated themselves.
     *
     * @param aClass the class of a managed bean.
     * @return true if the bean class is either directly or by transitivity annotated with
     * {@link Singleton} or {@link org.silverpeas.kernel.annotation.Cacheable}.
     */
    @Override
    public boolean test(Class<?> aClass) {
      if (aClass.isAnnotationPresent(Singleton.class) ||
          aClass.isAnnotationPresent(Cacheable.class)) {
        return true;
      }
      return isOneAnnotatedSingletonOrCacheable(aClass.getAnnotations());
    }

    private boolean isSingletonOrCacheableAnnotated(Class<? extends Annotation> annotationType) {
      if (annotationType.isAnnotationPresent(Singleton.class) ||
          annotationType.isAnnotationPresent(Cacheable.class)) {
        return true;
      }
      if (!annotationType.getName().startsWith("org.silverpeas")) {
        return false;
      }
      return isOneAnnotatedSingletonOrCacheable(annotationType.getAnnotations());
    }

    private boolean isOneAnnotatedSingletonOrCacheable(Annotation[] annotations) {
      for (Annotation annotation : annotations) {
        if (isSingletonOrCacheableAnnotated(annotation.annotationType())) {
          return true;
        }
      }
      return false;
    }
  }
}
