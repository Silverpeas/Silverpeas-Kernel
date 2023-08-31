/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.kernel.exception.NotFoundException;
import org.silverpeas.kernel.util.Mutable;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A provider of objects whose the life-cycle is managed by an underlying IoC/IoD container that
 * must satisfy the {@link BeanContainer} interface. This provider is just an access point to the
 * beans for which their class has been elective for life-cycle management and registered for this.
 * The election and the registration of the classes in the bean container are left to the
 * implementation of the IoC/IoD solution.
 * <p>
 * Usually, the dependencies between beans managed by the container are resolved during their
 * instantiation and this mechanism is usually done transparently by the IoC/IoD solution itself.
 * Nevertheless, some IoC/IoD solutions resolves them at compilation time instead of at runtime. And
 * some supports a lazy dependency resolution whereas others not. So, the resolution of the
 * dependencies on other managed objects is expected to be automatically performed by the container.
 * Nevertheless, some objects non managed by the container (like objects got from data source) can
 * require to access some of the beans in the container. This provider is for them; it allows them
 * to get the expected bean in the container.
 * </p>
 * <p>
 * For managed beans, according to the IoC system used, their dependency resolution should be left
 * to the underlying container because it is less expensive in time than using this provider to
 * access the dependencies. This is why this provider should be used only by unmanaged beans.
 * Anyway, to improve lightly the performance, the thread local cache is used to store single bean
 * of singleton, avoiding then to invoke explicitly the underlying bean container. It is when such a
 * single instance isn't found in the cache the container is implied to get it.
 * </p>
 *
 * @author mmoquillon
 * @implNote The {@link BeanContainer} interface is a wrapper of the underlying IoC subsystem
 * (Inversion of Control) used to manage the life-cycle of the some objects as well as the
 * resolution of their dependencies. (The IoC subsystem is built upon an IoD (Injection of
 * Dependencies) mechanism.) The bind between the {@link BeanContainer} interface and its
 * implementation is performed by the Java SPI (Java Service Provider Interface). Only the first
 * available bean container implementation is loaded by the {@code ManagedBeanProvider} class.
 * </p>
 */
public class ManagedBeanProvider {

  private static ManagedBeanProvider instance;

  static final String CACHE_KEY_PREFIX = "ManagedBeanProvider:CacheKey:";
  private final ThreadCacheAccessor cacheAccessor = ThreadCacheAccessor.getInstance();
  private final BeanContainer currentContainer;

  /**
   * Gets the single instance of this {@link ManagedBeanProvider} class.
   *
   * @return the {@link ManagedBeanProvider} single instance.
   */
  public static synchronized ManagedBeanProvider getInstance() {
    if (instance == null) {
      instance = new ManagedBeanProvider();
    }
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
   * @throws org.silverpeas.kernel.exception.MultipleCandidateException if there is more than one
   * bean matching the given type and qualifiers as there is an ambiguous decision in selecting the
   * bean to return.
   * @see BeanContainer#getBeanByType(Class, Annotation...)
   */
  @NonNull
  public <T> T getManagedBean(Class<T> type, Annotation... qualifiers) {
    Mutable<T> bean = Mutable.empty();
    T cachedBean = cacheAccessor.getCache().computeIfAbsent(cacheKey(type, qualifiers), type,
        () -> {
      bean.set(beanContainer()
          .getBeanByType(type)
          .orElseThrow(() -> {
            String q = qualifiers.length > 0 ? " and qualifiers " +
                Stream.of(qualifiers).map(a -> a.getClass().getName()).collect(Collectors.joining(", ")) : "";
            return new NotFoundException("No such bean satisfying type " + type.getName() + q);
          }));
      return bean.get().getClass().isAnnotationPresent(Singleton.class) ? bean.get() : null;
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
   * @throws org.silverpeas.kernel.exception.MultipleCandidateException if there is more than one
   * bean with the given name as the name must be unique for each bean.
   * @see BeanContainer#getBeanByName(String)
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public <T> T getManagedBean(String name) {
    Mutable<T> bean = Mutable.empty();
    T cachedBean = (T) cacheAccessor.getCache().computeIfAbsent(CACHE_KEY_PREFIX + name,
        Object.class, () -> {
          bean.set(beanContainer()
              .getBeanByName(name)
              .map(b -> (T) b)
              .orElseThrow(() -> new NotFoundException("No such bean named " + name)));
          return bean.get().getClass().isAnnotationPresent(Singleton.class) ? bean.get() : null;
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
}
