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
package org.silverpeas.kernel.annotation;

import java.lang.annotation.*;

/**
 * This annotation has to be used to tag a package to define the Silverpeas module the package
 * belongs to.
 * <p>
 * Silverpeas is made up of several modules, each of them defining both a given business model and a
 * functional service on it. A module is uniquely identified by a simple name. Each module in
 * Silverpeas provides a set of interfaces that can be used by others modules to realize their
 * responsibilities; codes in modules shouldn't use the classes in others modules but the
 * interfaces.
 * <p>
 * The annotation can be used by some transverse services to perform some dedicated operations based
 * upon the module a code belongs to. For example, the Silverpeas Logging API uses this annotation
 * to figure out the module a given object or class belongs to.
 *
 * @author mmoquillon
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Module {

  String value();
}
