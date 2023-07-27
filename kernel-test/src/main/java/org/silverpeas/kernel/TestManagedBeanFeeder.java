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

package org.silverpeas.kernel;

import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.cache.service.ThreadCacheService;
import org.silverpeas.kernel.test.TestBeanContainer;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * A feeder of beans to manage in the underlying IoC subsystem. The subsystem is implemented here by
 * {@link TestBeanContainer}. This feeder is to be used by the unit tests to register the beans
 * required by them for the test passes successfully. For more information about are managed the
 * beans in the IoC container used in the unit tests, please see {@link TestBeanContainer}
 *
 * @see TestBeanContainer
 */
public final class TestManagedBeanFeeder {

  private final ManagedBeanProvider provider = ManagedBeanProvider.getInstance();

  /**
   * Manages explicitly the specified bean under the given unique name.
   *
   * @param bean the object to manage into the IoC container dedicated to the unit tests.
   * @param name the name under which the bean will be register in the container.
   * @param <T> the concrete type of the bean to manage.
   */
  public <T> void manageBeanWithName(@NonNull T bean, @NonNull String name) {
    Objects.requireNonNull(bean);
    Objects.requireNonNull(name);
    getBeanContainer().putBean(bean, name);
  }

  /**
   * Manages the beans of the specified class for the given type and with the specified qualifiers.
   * The life-cycle of the beans, and thus their instantiation, will be taken in charge by the
   * underlying IoC container.
   *
   * @param beanClass the class of the beans to be managed by the IoC container dedicated to the
   * unit tests
   * @param type the type for which the beans will be managed.
   * @param qualifiers zero, one or more annotations qualifying the beans. Those must satisfy the
   * {@link javax.inject.Qualifier} annotation.
   * @param <T> the concrete type of the beans to register.
   */
  public <T> void manageBeanForType(@NonNull Class<T> beanClass, @NonNull Class<? super T> type,
      @NonNull Annotation... qualifiers) {
    Objects.requireNonNull(beanClass);
    Objects.requireNonNull(type);
    Objects.requireNonNull(qualifiers);
    getBeanContainer().putBean(beanClass, type, qualifiers);
  }

  /**
   * Manages explicitly the specified bean for the given type and with the specified qualifiers.
   *
   * @param bean the object to manage into the IoC container dedicated to the unit tests.
   * @param type the type for which the bean is registered.
   * @param qualifiers zero, one or more annotations qualifying the beans. Those must satisfy the
   * {@link javax.inject.Qualifier} annotation.
   * @param <T> the actual type of the bean.
   */
  public <T> void manageBean(@NonNull T bean, @NonNull Class<? super T> type,
      @NonNull Annotation... qualifiers) {
    Objects.requireNonNull(bean);
    Objects.requireNonNull(type);
    Objects.requireNonNull(qualifiers);
    getBeanContainer().putBean(bean, type, qualifiers);
  }

  /**
   * Removes all the beans that were fed into the IoC container. As the {@link ManagedBeanProvider}
   * wrapping the {@link BeanContainer} uses also a tread local cache to cache some beans to enhance
   * performance, those are also cleared.
   */
  public void removeAllManagedBeans() {
    getBeanContainer().clear();
    ThreadCacheService service = new ThreadCacheService();
    service.clearAllCaches();
  }

  private TestBeanContainer getBeanContainer() {
    return (TestBeanContainer) provider.beanContainer();
  }

}
