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

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

/**
 * The bean container is a wrapper of an IoC (Inversion of Control) by IoD (Inversion of Dependency) system. Only one
 * IoC system should be available and used in the whole Silverpeas runtime, meaning only one implementation of this
 * interface will be used. The expectation here is the IoC subsystem should support JSR-365 and as such it should be
 * supports IoD; examples of such subsystems are Spring and CDI. Moreover, an additional constrain is the dependency
 * shouldn't be resolved through the constructor parameters but instead by using the {@link javax.inject.Inject}
 * annotation or by using a {@link ManagedBeanProvider} instance.
 * <p>
 * The goal of the bean container is to manage the life-cycle of some objects (named here beans) and to resolve and to
 * satisfy automatically and in a transparent way their dependencies. For doing, the classes for which the instances
 * have to be managed by the container should be elective and registered into this container. The way the election and
 * the registration of theses classes are performed is the responsibility of the underlying IoC subsystem.
 *
 * @author mmoquillon
 * @apiNote the {@link BeanContainer} isn't to be used directly by codes. It is just a wrapper of a true IoC/IoD
 * solution for the {@link ManagedBeanProvider}. Latter has to be used instead.
 * @implSpec the {@link BeanContainer} has to be bound to its implementation by the Java SPI (Java Service Provider
 * Interface)
 */
public interface BeanContainer {

  /**
   * Gets a bean managed in this container by its name. If no such bean or exists in the bean container, nothing is
   * returned. If there is more than one bean with the given name, then an {@link IllegalStateException} exception
   * should be thrown as the name of beans must be unique, being a kind of identifier.
   *
   * @param name the name of the bean.
   * @param <T> the type of the bean to return.
   * @return the bean matching the specified name.
   */
  <T> Optional<T> getBeanByName(String name);

  /**
   * Gets a bean managed in this container by its type and optionally by some qualifiers. If there is more than one bean
   * satisfying the specified type then an {@link IllegalStateException} exception is thrown as there is an ambiguous
   * decision in selecting the bean to return.
   *
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return the bean satisfying the expected type and, if any, the expected qualifiers.
   */
  <T> Optional<T> getBeanByType(Class<T> type, Annotation... qualifiers) throws IllegalStateException;

  /**
   * Gets a bean managed in this container by its name. If no such bean exists in the bean container, then an empty set
   * is returned.
   *
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return a set of beans satisfying the expected type and, if any, the expected qualifiers, or an empty set
   * otherwise.
   */
  <T> Set<T> getAllBeansByType(Class<T> type, Annotation... qualifiers);
}
