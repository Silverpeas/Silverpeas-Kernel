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
import java.util.Set;

/**
 * The bean container is a wrapper of an IoC subsystem (Inversion of Control). Only one IoC
 * subsystem should be available and used in the whole Silverpeas runtime, meaning only one
 * implementation of this interface will be used. The IoC subsystem must provide an IoD mechanism
 * (Injection of Dependencies); examples of such subsystems are Spring and CDI.
 * <p>
 * The goal of the bean container is to manage the life-cycle of some objects and to resolve and to
 * satisfy automatically and in a transparent way their dependencies. For doing, the classes for
 * which the instances have to be managed by the container should be elective and registered into
 * this container. The way the election and the registration of theses classes are performed is the
 * responsibility of the underlying IoC subsystem.
 *
 * @author mmoquillon
 * @implSpec the {@link BeanContainer} has to be bound to its implementation by the Java SPI (Java
 * Service Provider Interface)
 */
public interface BeanContainer {

  /**
   * Gets a bean managed in this container by its name. If no such bean exists in the bean
   * container, then an IllegalStateException exception is thrown.
   *
   * @param name the name of the bean.
   * @param <T>  the type of the bean to return.
   * @return the bean matching the specified name.
   * @throws IllegalStateException if no bean can be found with the specified name.
   */
  <T> T getBeanByName(String name) throws IllegalStateException;

  /**
   * Gets a bean managed in this container by its type and optionally by some qualifiers. If no such
   * bean exists in the bean container, then an IllegalStateException exception is thrown.
   *
   * @param type       the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T>        the type of the bean to return.
   * @return the bean satisfying the expected type and, if any, the expected qualifiers.
   * @throws IllegalStateException if no bean of the specified type and with the specified
   *                               qualifiers can be found.
   */
  <T> T getBeanByType(Class<T> type, Annotation... qualifiers) throws IllegalStateException;

  /**
   * Gets a bean managed in this container by its name. If no such bean exists in the bean
   * container, then an empty set is returned.
   *
   * @param type       the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T>        the type of the bean to return.
   * @return a set of beans satisfying the expected type and, if any, the expected qualifiers, or an
   * empty set otherwise.
   */
  <T> Set<T> getAllBeansByType(Class<T> type, Annotation... qualifiers);
}
