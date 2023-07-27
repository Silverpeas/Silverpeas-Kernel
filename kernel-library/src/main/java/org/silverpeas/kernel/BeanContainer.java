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
 * The bean container is a wrapper of an IoC (Inversion of Control) by IoD (Inversion of Dependency)
 * system. Only one IoC system should be available and used in the whole Silverpeas runtime, meaning
 * only one implementation of this interface will be used. The expectation here is the IoC subsystem
 * should implement the JSR-330 and as such it should support IoD; examples of such subsystems are
 * Spring, Micronaut or CDI (JSR-365). Moreover, the bean container imposes an additional constrain
 * to ease the use of IoD in Silverpeas whatever the underlying implementation: the dependencies
 * shouldn't be resolved through constructor or method parameters but instead by using the
 * {@link javax.inject.Inject} annotation on fields or by using a {@link ManagedBeanProvider}
 * instance. Keep in mind to leave your code agnostic from any specific code of an IoC solution so
 * it should be more easy later to change such a solution.
 * <p>
 * The goal of the bean container is to manage the life-cycle of some objects (named here beans), to
 * resolve and to satisfy automatically and in a transparent way their dependencies. For doing, the
 * classes for which the instances have to be managed by the container should be elective for
 * life-cycle management and registered into this container. The way the election and the
 * registration of theses classes are performed is the responsibility of the underlying IoC
 * subsystem.
 *
 * @author mmoquillon
 * @apiNote The {@link BeanContainer} isn't to be used directly by codes. It is just a wrapper of a
 * true IoC/IoD solution for the {@link ManagedBeanProvider}. Latter has to be used instead.
 * @implSpec The {@link BeanContainer} has to be bound to its implementation by the Java SPI (Java
 * Service Provider Interface)
 */
public interface BeanContainer {

  /**
   * Gets a bean managed in this container by its name. If no such bean or exists in the bean
   * container, nothing is returned.
   *
   * @param name the name of the bean.
   * @param <T> the type of the bean to return.
   * @return the bean matching the specified name or nothing
   * @throws org.silverpeas.kernel.exception.MultipleCandidateException if there is more than one
   * bean with the given name as the name must be unique for each bean.
   */
  <T> Optional<T> getBeanByName(String name);

  /**
   * Gets a bean managed in this container by its type and optionally by some qualifiers.
   *
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return the bean satisfying the expected type and, if any, the expected qualifiers, or nothing.
   * @throws org.silverpeas.kernel.exception.MultipleCandidateException if there is more than one
   * bean matching the given type and qualifiers as there is an ambiguous decision in selecting the
   * bean to return.
   */
  <T> Optional<T> getBeanByType(Class<T> type, Annotation... qualifiers);

  /**
   * Gets a bean managed in this container by its name. If no such bean exists in the bean
   * container, then an empty set is returned.
   *
   * @param type the type of the bean.
   * @param qualifiers zero, one or more qualifiers annotating the bean to look for.
   * @param <T> the type of the bean to return.
   * @return a set of beans satisfying the expected type and, if any, the expected qualifiers, or an
   * empty set otherwise.
   */
  <T> Set<T> getAllBeansByType(Class<T> type, Annotation... qualifiers);
}
