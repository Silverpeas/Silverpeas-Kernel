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

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;
import java.util.Set;


/**
 * A provider of objects whose the life-cycle is managed by an underlying container of beans whose
 * the implementation satisfies the {@link BeanContainer} interface. For doing, the class of these
 * objects must be elective for management and registered into the {@link BeanContainer} instance.
 * This provider is just an access point to the objects of these classes in the container.
 * <p>
 * Usually, the dependencies between beans managed by the container are resolved and satisfied by
 * the container itself when it instantiates the registered classes and it initializes the spawned
 * object. So, the providing of their dependencies on other managed objects is automatically
 * performed by the container. Nevertheless, some objects non managed by the container (like objects
 * got from data source) can require to access some of the beans in the container. This provider is
 * for them; it allows them to get the expected bean in the container.
 * </p>
 * <p>
 * For managed beans, according to the IoC subsystem used, their dependency resolution should be
 * left to the underlying container because it is less expensive in time than using this provider to
 * access the dependencies. This is why this provider should be used only by unmanaged beans.
 * </p>
 *
 * @author mmoquillon
 * @implNote The {@link BeanContainer} interface is a wrapper of the underlying IoC subsystem
 * (Inversion of Control) used to manage the life-cycle of the some objects as well as the
 * resolution of their dependencies. (The IoC subsystem is also an IoD subsystem for Injection of
 * Dependencies.) The bind between the {@link BeanContainer} interface and its implementation is
 * performed by the Java SPI (Java Service Provider Interface). Only the first available bean
 * container implementation is loaded by the {@code ManagedBeanProvider} class.
 * </p>
 */
public class ManagedBeanProvider {

  private static ManagedBeanProvider instance;

  private static final String CACHE_KEY_PREFIX = "ManagedBeanProvider:CacheKey:";
  private final ThreadCacheService cacheService = ThreadCacheService.getInstance();
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
   * Gets an instance of the single implementation of the specified type and satisfying the given
   * qualifiers if any.
   *
   * @param type       the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T>        the type of the bean to return.
   * @return the bean satisfying the expected type and, if any, the expected qualifiers.
   * @throws IllegalStateException if no bean of the specified type and with the specified
   *                               qualifiers can be found.
   * @see BeanContainer#getBeanByType(Class, Annotation...)
   */
  public <T> T getManagedBean(Class<T> type, Annotation... qualifiers) {
    return beanContainer().getBeanByType(type, qualifiers);
  }

  /**
   * Gets the single instance of the single implementation of the specified type and satisfying the
   * given qualifiers if any. The specified class has to be a singleton, otherwise unexpected
   * behavior can occur.
   * <p>
   * By using this method, the caller is knowingly requesting a singleton. This is why it has to be
   * <em>sure</em> the class is a singleton.
   *
   * @param type       the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T>        the type of the bean to return.
   * @return the singleton bean satisfying the expected type and, if any, the expected qualifiers.
   * @throws IllegalStateException if no bean of the specified type and with the specified
   *                               qualifiers can be found.
   * @implNote The bean provided by this method is the single instance of the specified class within
   * the current thread.
   * @implSpec for singletons, their single bean is cached in the cache of the current thread so
   * that it can be latter retrieved without having to ask the {@link BeanContainer} for it. This
   * improves the time to fetch a bean.
   * @see #getManagedBean(Class, Annotation...)
   */
  public <T> T getSingleInstance(Class<T> type, Annotation... qualifiers) {
    final StringBuilder cacheKey = new StringBuilder(CACHE_KEY_PREFIX + type.getName());
    for (final Annotation qualifier : qualifiers) {
      cacheKey.append(":").append(qualifier.annotationType().getName());
    }
    return cacheService.getCache()
        .computeIfAbsent(cacheKey.toString(), type, () -> getManagedBean(type, qualifiers));
  }

  /**
   * Gets an object of the single type implementation that is qualified by the specified name.
   *
   * @param name the name of the bean.
   * @param <T>  the type of the bean to return.
   * @return the bean matching the specified name.
   * @throws IllegalStateException if no bean can be found with the specified name.
   * @see BeanContainer#getBeanByName(String)
   */
  public <T> T getManagedBean(String name) {
    return beanContainer().getBeanByName(name);
  }


  /**
   * Gets the single instance of a single type implementation that is qualified by the specified
   * name.
   * <p>
   * By using this method, the caller is knowingly requesting a singleton. This is why it has to be
   * <em>sure</em> the class is a singleton.
   * <p>
   *
   * @param name the name of the bean.
   * @param <T>  the type of the bean to return.
   * @return the bean matching the specified name.
   * @throws IllegalStateException if no bean can be found with the specified name.
   * @implNote The bean provided by this method is the single instance of the class that is
   * qualified by the given name and within the current thread.
   * @implSpec for singletons, their single bean is cached in the cache of the current thread so
   * that it can be latter retrieved without having to ask the {@link BeanContainer} for it. This
   * improves the time to fetch a bean.
   * @see #getManagedBean(String)
   */
  @SuppressWarnings("unchecked")
  public <T> T getSingleInstance(String name) {
    return (T) cacheService.getCache()
        .computeIfAbsent(CACHE_KEY_PREFIX + name, Object.class, () -> getManagedBean(name));
  }

  /**
   * Gets an instance of each implementations of the specified type and satisfying the given
   * qualifiers if any.
   *
   * @param type       the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T>        the type of the bean to return.
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

}
