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
/**
 * The technical foundation of all of the Silverpeas projects. It provides all the technical bricks
 * upon which a more functional foundation or solution can be built for Silverpeas. All the
 * interfaces and abstract classes have to be implemented by choosing the more appropriate software
 * solution and the implementations should be available through the Java Service Provider Interface
 * (SPI). Utility classes are also provided for common tasks or needs.
 * <p>
 * The base key of Silverpeas Kernel is its API abstracting an IoC (Inversion of Control) by IoD
 * (Injection of Dependencies) solution. The idea here is to grant the control flow to frameworks
 * which then delegate functional details to the implementations of some of theirs APIs. These
 * implementations are custom codes provided by the Silverpeas projects. The IoC mechanism allows
 * then the Silverpeas projects to expose their own business or functional interfaces to others
 * projects, to propose one or more implementations of them, and to get transparently an instance of
 * an implementation of a given interface. All the details are taken in charge under the hood by the
 * IoC/IoD solution. Because the code of Silverpeas Kernel must be agnostic to all technical
 * implementation choices, and because there are several and incompatible solutions of an IoC by IoD
 * mechanism, the API is built only upon the JSR-330 and provides a simple way to get
 * programmatically an object satisfying a given type and some qualifiers. The IoC/IoD solution has
 * to be provided through the Java SPI by implementing the
 * {@link org.silverpeas.kernel.BeanContainer} interface.
 * </p>
 *
 * @author mmoquillon
 */
package org.silverpeas.kernel;