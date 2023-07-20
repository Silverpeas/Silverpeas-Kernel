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

import org.silverpeas.kernel.cache.service.ThreadCacheService;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A provider of objects whose the life-cycle is managed by an underlying IoC/IoD container whose the implementation
 * satisfies the {@link BeanContainer} interface. For doing, the class of these objects must be elective for management
 * and registered into the {@link BeanContainer} instance. This provider is just an access point to the objects of these
 * classes in the container. The election and the registration of the classes in the bean container are left to the
 * implementation of the IoC/IoD solution.
 * <p>
 * Usually, the dependencies between beans managed by the container are resolved during their instantiation and this
 * mechanism is usually done transparently by the IoC/IoD solution itself. Nevertheless, some IoC/IoD solutions resolves
 * them at compilation time instead of at runtime. And some supports a lazy dependency resolution whereas others not.
 * So, the resolution of the dependencies on other managed objects is expected to be automatically performed by the
 * container. Nevertheless, some objects non managed by the container (like objects got from data source) can require to
 * access some of the beans in the container. This provider is for them; it allows them to get the expected bean in the
 * container.
 * </p>
 * <p>
 * For managed beans, according to the IoC system used, their dependency resolution should be left to the underlying
 * container because it is less expensive in time than using this provider to access the dependencies. This is why this
 * provider should be used only by unmanaged beans. Anyway, to improve lightly the performance, the thread local cache
 * is used to store single bean of singleton, avoiding then to invoke explicitly the underlying bean container.
 * </p>
 *
 * @author mmoquillon
 * @implNote The {@link BeanContainer} interface is a wrapper of the underlying IoC subsystem (Inversion of Control)
 * used to manage the life-cycle of the some objects as well as the resolution of their dependencies. (The IoC subsystem
 * is built upon an IoD (Injection of Dependencies) mechanism.) The bind between the {@link BeanContainer} interface and
 * its implementation is performed by the Java SPI (Java Service Provider Interface). Only the first available bean
 * container implementation is loaded by the {@code ManagedBeanProvider} class.
 * </p>
 */
public class ManagedBeanProvider {

  private static ManagedBeanProvider instance;

  static final String CACHE_KEY_PREFIX = "ManagedBeanProvider:CacheKey:";
  private final ThreadCacheService cacheService = new ThreadCacheService();
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
   * Gets an instance of the single implementation of the specified type and satisfying the given qualifiers if any.
   *
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return the bean satisfying the expected type and, if any, the expected qualifiers.
   * @throws IllegalStateException if no bean of the specified type and with the specified qualifiers can be found.
   * @see BeanContainer#getBeanByType(Class, Annotation...)
   */
  public <T> T getManagedBean(Class<T> type, Annotation... qualifiers) {
    return beanContainer().getBeanByType(type, qualifiers)
        .orElseThrow(() -> {
          String q = qualifiers.length > 0 ? " and qualifiers " +
              Stream.of(qualifiers).map(a -> a.getClass().getName()).collect(Collectors.joining(", ")) : "";
          return new IllegalStateException("No such bean satisfying type " + type.getName() + q);
        });
  }

  /**
   * Gets the single instance of the single implementation of the specified type and satisfying the given qualifiers if
   * any. The class satisfying the expectation has to be a singleton, otherwise an {@link IllegalStateException} is
   * thrown.
   * <p>
   * By using this method, the caller is knowingly requesting a singleton. This is why it has to be
   * <em>sure</em> the class is a singleton.
   *
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return the singleton bean satisfying the expected type and, if any, the expected qualifiers.
   * @throws IllegalStateException if no bean of the specified type and with the specified qualifiers can be found or if
   * the class satisfying the give type and with the specified qualifiers isn't a singleton.
   * @implNote The bean provided by this method is the single instance of the specified class within the current
   * thread.
   * @implSpec for singletons, their single bean is cached in the cache of the current thread so that it can be latter
   * retrieved without having to ask the {@link BeanContainer} for it. This improves the time to fetch a bean.
   * @see #getManagedBean(Class, Annotation...)
   */
  public <T> T getSingleInstance(Class<T> type, Annotation... qualifiers) {
    final StringBuilder cacheKey = new StringBuilder(CACHE_KEY_PREFIX + type.getName());
    for (final Annotation qualifier : qualifiers) {
      cacheKey.append(":").append(qualifier.annotationType().getName());
    }
    return cacheService.getCache()
        .computeIfAbsent(cacheKey.toString(), type, () -> ensureIsSingleBean(getManagedBean(type, qualifiers)));
  }

  /**
   * Gets an object of the single type implementation that is qualified by the specified name.
   *
   * @param name the unique name identifying the bean in the container.
   * @param <T> the type of the bean to return.
   * @return the bean matching the specified name.
   * @throws IllegalStateException if no bean can be found with the specified name.
   * @see BeanContainer#getBeanByName(String)
   */
  public <T> T getManagedBean(String name) {
    //noinspection unchecked
    return beanContainer()
        .getBeanByName(name)
        .map(b -> (T) b)
        .orElseThrow(() -> new IllegalStateException("No such bean named " + name));
  }


  /**
   * Gets the single instance of a single type implementation that is qualified by the specified name. If the class of
   * the bean registered under the given name isn't a singleton, an {@link IllegalStateException} is thrown.
   * <p>
   * By using this method, the caller is knowingly requesting a singleton. This is why it has to be
   * <em>sure</em> the class is a singleton.
   * <p>
   *
   * @param name the unique name identifying the bean in the container.
   * @param <T> the type of the bean to return.
   * @return the bean matching the specified name.
   * @throws IllegalStateException if no bean can be found with the specified name  or if the class satisfying the give
   * type and with the specified qualifiers isn't a singleton.
   * @implNote The bean provided by this method is the single instance of the class that is qualified by the given name
   * and within the current thread.
   * @implSpec for singletons, their single bean is cached in the cache of the current thread so that it can be latter
   * retrieved without having to ask the {@link BeanContainer} for it. This improves the time to fetch a bean.
   * @see #getManagedBean(String)
   */
  @SuppressWarnings("unchecked")
  public <T> T getSingleInstance(String name) {
    return (T) cacheService.getCache()
        .computeIfAbsent(CACHE_KEY_PREFIX + name, Object.class, () -> ensureIsSingleBean(getManagedBean(name)));
  }

  /**
   * Gets an instance of each implementations of the specified type and satisfying the given qualifiers if any.
   *
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return a set of beans satisfying the expected type and, if any, the expected qualifiers, or an empty set
   * otherwise.
   * @see BeanContainer#getAllBeansByType(Class, Annotation...)
   */
  public <T> Set<T> getAllManagedBeans(Class<T> type, Annotation... qualifiers) {
    return beanContainer().getAllBeansByType(type, qualifiers);
  }

  protected BeanContainer beanContainer() {
    return currentContainer;
  }

  protected <T> T ensureIsSingleBean(final T bean) {
    if (!bean.getClass().isAnnotationPresent(Singleton.class)) {
      throw new IllegalStateException(bean.getClass() + " isn't a singleton");
    }
    return bean;
  }

}
