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
/**
 * The technical foundation of all of the Silverpeas projects. It provides all the technical bricks
 * upon which a more functional foundation can be built for Silverpeas. All the interfaces and
 * abstract classes have to be implemented by choosing the more appropriate software solution.
 * Utility classes are also provided for common tasks or needs.
 * <p>
 * The Silverpeas kernel is built upon an IoC (Inversion of Control) and IoD (Injection of
 * Dependencies) solution that has to be provided by an external framework. The code of Silverpeas
 * Kernel must be agnostic of all technical implementation choices so all of the classes defined
 * here must not be managed by the IoC and IoD solution.
 * </p>
 *
 * @author mmoquillon
 */
package org.silverpeas.kernel;