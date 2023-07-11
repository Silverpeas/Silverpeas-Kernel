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

package org.silverpeas.kernel.annotation;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierNickname;
import javax.annotation.meta.When;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Type qualifier annotation to mark a parameter, an attribute or a return value might be null. An
 * element annotated with @{@link Nullable} claims null value is perfectly valid to return (for
 * methods), pass to (parameters) or hold in (local variables and fields). By convention, this
 * annotation applied only when the value should always be checked against null because the
 * developer could do nothing to prevent null from happening. Otherwise, too eager nullable usage
 * could lead to too many false positives from static analysis tools. For example,
 * java.util.Map.get(Object key) should not be annotated @{@link Nullable} because someone may have
 * put not-null value in the map by this key and is expecting to find this value there ever since.
 * On the other hand, the java.lang.ref.Reference.get() should be annotated @{@link Nullable}
 * because it returns null if object got collected which can happen at any time completely
 * unexpectedly.
 * <p>
 * Apart from documentation purposes this annotation is intended to be used by static analysis tools
 * to validate against probable runtime errors or element contract violations.
 * </p>
 * <p>
 * By defining our own annotations we ensure to be agnostic from any checking library or framework
 * because, despite of the JSR-305, a lot of tiers provide its own incompatible solution.
 * </p>
 *
 * @author mmoquillon
 */
@Documented
@TypeQualifierNickname
@Nonnull(when = When.MAYBE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Nullable {
}
