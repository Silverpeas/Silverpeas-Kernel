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
package org.silverpeas.kernel.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is for specifying the logger to use for a given package or class. It overrides
 * the logger mapped to to the package.
 * <p>
 * By default, the logger to use is identified by the package name to which the object that logs
 * messages belongs. The convention mapping between the namespace and a package name is that all
 * packages rooted to the package
 * <code>org.silverpeas</code> are mapped, each of them, to a namespace of identical name rooted to
 * the Silverpeas root
 * namespace <code>silverpeas</code>. For others packages, the namespace is the package fully
 * qualified name. Using this annotation, this rule is bypassed, but not the convention, and the
 * namespace specified in the annotation's value is then used to get the logger to use for logging
 * messages.
 *
 * @author mmoquillon
 */
@Target({PACKAGE, TYPE})
@Retention(RUNTIME)
@Documented
public @interface Logger {
  /**
   * @return the namespace of the logger.
   */
  String value();
}
